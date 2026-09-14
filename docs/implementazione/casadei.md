# Implementazione - Casadei Alex

## Panoramica dei contributi

Il mio contributo ha riguardato la progettazione dei `Controller`, dell'interfaccia `CLI` e della comunicazione tra View e Model:

- Controller e Game Loop: `MenuController`, `MatchController`, `GameController`, gestione della navigazione dei menu, del ciclo di gioco, dei turni, dell'integrazione con il Bot e di partite multiple.

- Input e Terminal Raw Mode: Gestione dell'input con JLine e mappatura multipiattaforma dei tasti tramite `Key`, `Utils`.

- View-Model: Disaccoppiamento tra Model e View tramite la snapshot immutabile `GameState` e gli stati di `InputMode`.

- Rendering e Scala Extensions: Motore grafico ASCII Art per il terminale sviluppato tramite gli Extension Methods di Scala (`CardRenderExtension`, `CLIView`, `CLIMenu`).

# Architettura dei Controller
Il flusso dell'applicazione è gestito da una gerarchia di controller che orchestrano l'esperienza utente dal menu iniziale fino al termine di una partita.

- `MenuController`: È il punto di ingresso dell'interfaccia. Sfrutta una logica a stati interni (tramite il trait `Menu` e i suoi object/class derivati come `MainMenu`, `TargetScoreMenu`, `ShowCaseMenu`) per gestire la navigazione. Tramite un ciclo di polling dell'input, mappa le frecce direzionali alla selezione delle voci (modellate tramite gli enum in `MenuItem`) e il tasto invio all'avvio del gioco o del sottomenu corrispondente.

    ```scala
    private trait Menu:
        def itemCount: Int
        def render(selectedIndex: Int): Unit
        def onConfirm(selectedIndex: Int): Unit
        def onBack(): Unit = currentMenu = MainMenu

    private object MainMenu extends Menu:
        private val menuItems: List[MenuItem] = MenuItem.values.toList
        override def itemCount: Int = menuItems.length
        override def render(selectedIndex: Int): Unit = menu.renderMainMenu(selectedIndex)

    override def onConfirm(selectedIndex: Int): Unit = menuItems(selectedIndex)match
        case SingleGame        => GameController(Game(BoardFactory.BoardWithPopulatedFields()), isVsBot = false, inputReader = readInput).start()
        case MenuMatch         => openTargetScoreMenu(isVsBot = false)
        case SinglePlayerGame  => GameController(Game(BoardFactory.BoardWithPopulatedFields()), isVsBot = true, inputReader = readInput).start()
        case SinglePlayerMatch => openTargetScoreMenu(isVsBot = true)
        case ShowCase          => openShowCaseMenu()
        case Rules             => openRulePage()
    ```

- `MatchController`: Gestisce le partite a punteggio prolungate. Istanzia sequenzialmente i GameController necessari finché uno dei giocatori non raggiunge il punteggio massimo (es. 50, 100 punti), occupandosi di mostrare le schermate di transizione e i risultati finali.

    ```scala
    def start(): Unit =
    while !matchRecord.isOver do
      val gameController = GameController(matchRecord, isVsBot = isVsBot, inputReader = readInput)
      gameController.start()

      matchRecord = gameController.getPlayable
      // ... gestione del termine della singola partita
    ```


- `GameController`: È il Game Loop principale del gioco. Le sue responsabilità includono:

     - Sincronizzare costantemente il modello di dominio con la vista tramite la generazione di un GameState.

    - Gestire le iterazioni dei turni sia contro giocatori in locale che contro un bot, delegando in quest'ultimo caso la mossa alla classe `Opponent`.

    - Pilotare l'input dell'utente basato sull'enum `InputMode`. A seconda della modalità (ActionMenu, SelectCardOnBoard, ecc.), i tasti direzionali e l'Invio innescano comportamenti diversi, come navigare tra le azioni disponibili o selezionare carte fisiche sul tabellone.

    ```scala
    def start(): Unit =
    while running do
      if isVsBot && turn.player == Player2 && running && state != WaitingRoom && !game.isOver then botTurn()
      else
        view.render(state)
        inputReader() match
          case Key.UP | Key.LEFT    => moveSelection(delta = STEP_NEXT)
          case Key.DOWN | Key.RIGHT => moveSelection(delta = STEP_PREVIOUS)
          case Key.ENTER            => confirmAction()
          case Key.ESCAPE           => running = false
          case _                    => ()
    ```

# Gestione dello Stato Visivo (View-Model)
Per evitare un accoppiamento stretto tra la View e il Model, il progetto implementa:

`GameState`: È una case class immutabile che funge da "fotografia" dello stato della partita in un preciso istante. Contiene dati pre-processati e formattati (carte del giocatore, carte in mano, punteggi, ecc.) che la `CLIView` deve semplicemente limitarsi a stampare.

```scala
case class GameState(
    adversaryCard: List[Option[Card]],
    playerCard: List[Option[Card]],
    remainingCardInDeck: Int,
    lastDiscardedCard: Option[Card],
    cardsInHand: List[Option[Card]],
    possibleAction: List[ViewAction],
    inputMode: InputMode = ActionMenu,
    selectedAction: Int = 0,
    selectedCardOnBoard: Int = 0,
    winner: Option[Player] = None,
    // ...
    )
```
```scala
    // GameController: Costruzione ed estrazione dello stato per la View
    private def syncState(
                            inputMode: InputMode,
                            selectedAction: Int = 0,
                            selectedCardOnBoard: Int = 0
                        ): GameState =
        val board = turn.board
        val (modelActions, viewActions) = prepareActions(turn.actions)
        currentModelActions = modelActions
        val isEndgame = inputMode.equals(EndGame)

        GameState(
        [...]
        )
```

`ViewAction`: Le complesse azioni di dominio (Action del Model) vengono tradotte in oggetti ViewAction tramite la funzione di utility mapSingleAction. Questo processo converte istruzioni computazionali in label facilmente comprensibili dall'utente finale ("Pesca dal mazzo", "Usa effetto", ecc.).

```scala
case class ViewAction(id: String, label: String)

def mapSingleAction(action: Action): ViewAction = action match
    case Action.Observe  => ViewAction("observe", "Peek at the first two cards")
    case Action.Draw     => ViewAction("draw", "Draw from deck")
    case Action.Activate => ViewAction("activate", "Use card effect or replace")
    // ... altre mappature
```

# Interazione dell'Utente (InputMode)
L'interazione con la tastiera cambia contesto a seconda della fase di gioco. Questo comportamento è stato modellato attraverso l'enum `InputMode` e gestito nel `GameController` mediante pattern matching:  
- `ActionMenu`: L'utente sceglie un'azione generale dall'elenco.
- `SelectCardOnBoard`: L'utente usa le frecce per evidenziare una carta sul proprio tabellone.  
- `SelectAdversaryCardOnBoard`: L'utente seleziona una carta sul tabellone dell'avversario. 
- `WaitingRoom`: Schermata di transizione tra un giocatore e l'altro nei match locali.
- `Endgame`: Mostra la schermata di fine partita mostrando i punteggi e le carte sul campo

```scala
// InputMode: Modalità di input disponibili
enum InputMode:
  case ActionMenu
  case SelectCardOnBoard
  case SelectAdversaryCardOnBoard
  case WaitingRoom
  case EndGame
```
```scala
// GameController: Navigazione dinamica tra le carte o le opzioni in base all'InputMode
  private def moveSelection(delta: Int): Unit = state.inputMode match
    case InputMode.ActionMenu => [...]

    case InputMode.SelectCardOnBoard => [...]

    case InputMode.SelectAdversaryCardOnBoard => [...]

    case _ => ()
```


# Gestione dell'Input e del Terminale (`Utils` e `Key`)
Tramite l'oggetto `Utils` e la libreria JLine, il terminale viene configurato in Raw Mode. Questo permette di intercettare dinamicamente le pressioni dei tasti senza dover attendere il carriage return (tasto Invio) di sistema.

- Le sequenze escape ANSI vengono mappate nell'enum `Key`, rendendo il codice nei Controller pulito e indipendente rispetto al sistema operativo dell'utente (Windows/Linux/macOS).

    ```scala
    def init(): Unit =
        terminal.enterRawMode()
        terminal.puts(Capability.cursor_invisible)
        terminal.puts(Capability.keypad_xmit)

        // Key binding multipiattaforma
        // Key binding for     Linux/macOS     ZSH              WINDOWS
        keyMap.bind(Key.UP, "   \u001b[A", "\u001bOA", KeyMap.key(terminal, Capability.key_up))
        keyMap.bind(Key.ENTER, "\r", "\n")
        // ...

    def readInput(): Key =
        Try {
            val key = bindingReader.readBinding(keyMap)
            if key == null then Key.UNKNOWN else key
        }.getOrElse(Key.UNKNOWN)
    ```

- `Utils` si occupa anche del posizionamento visivo tramite calcoli dinamici della larghezza/altezza del terminale (terminalWidth e terminalHeight) garantendo testo centrato e formattazione responsiva.
    ```scala
    def centerText(text: String, targetLength: Int = terminalWidth): String =
        // regex per eliminare tutti gli ANSI code dai calcoli di centratura
        val visualLen = text.replaceAll("\u001B\\[[;\\d]*m", "").length
        val space = Math.max(0, (targetLength - visualLen) / 2)
        " " * space + text
    ```

# Rendering delle Carte in ASCII Art
La parte di UI vera e propria è fortemente coadiuvata dal file `CardRenderExtension`.

Sono satti utilizzati gli Extension Methods per Option[Card] e List[Option[Card]] per metodi di rendering specifici (come drawSingleCard e drawCardRows). Questo rende la sintassi estremamente fluida, permettendo di chiamare direttamente .drawCardRows(...) su una lista di carte.

```scala
extension (cardOpt: Option[Card])
    def drawSingleCard(borderStyle: BorderStyle = BorderStyle.Normal, label: Option[String] = None): List[String] =
      // [...] Configurazione colori ANSI e bordi
      
      cardOpt match
        case Some(card) =>
          val (value, suit) = extractCardDetails(card)
          val sColor = suitColor(card.suit)
          // Costruzione delle stringhe ASCII che compongono la carta
          List(
            top,
            s"$bColor│$reset$sColor$leftRank$reset         $bColor│$reset",
            blank,
            s"$bColor│$reset$sColor$centeredSuit$reset$bColor│$reset",
            // ...
          )
        case None =>
          // Rendering del dorso della carta nascosta
```
Le carte vengono generate unendo segmenti di stringhe arricchiti con costanti cromatiche (es. Ciano per le Spade, Rosso per le Coppe). Il sistema gestisce automaticamente la responsività: calcola quante carte riescono ad entrare in una singola riga del terminale e, se necessario, le distribuisce su più righe multilinea (grazie ai metodi helper come joinCardsHorizontally).


I codici colore ANSI aggiungono caratteri invisibili alle stringhe, falsando il calcolo standard della lunghezza (string.length). Per evitare che le carte ASCII risultino deformate a schermo, è stata implementata la funzione visualLength, che rimuove le sequenze ANSI via Regex prima di effettuare il padding (padLeft, padRight, padCenter):    

```scala
private def visualLength(str: String): Int =
  str.replaceAll("\u001B\\[[;\\d]*m", "").length

private def padCenter(s: String, len: Int): String =
  val total = Math.max(0, len - visualLength(s))
  val left = total / 2
  (" " * left) + s + (" " * (total - left))
```
