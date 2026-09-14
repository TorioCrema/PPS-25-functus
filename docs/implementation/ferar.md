# Implementazione - Luca Ferar

## Panoramica dei contributi

Il mio contributo al progetto è stato:

- **Card**: implementazione della singola carta.
- **Deck**: implementazione del mazzo di gioco.
- **Field**: implementazione del campo di gioco utilizzato dal giocatore.
- **Board**: implementazione del tavolo da gioco.
- **DSLs**: implementazione delle DSL delle entità sopra citate.
- **Game**: implementazione della singola partita.
- **Showcase**: implementazione delle board tramite le dsl per gli showcases.
- **Testing**: scrittura dei test per tutti i componenti implementati.

## Card

Una `Card` è l’unità più piccola del dominio, una carta viene composta da un valore e da un seme. Il valore è rappresentato 
da un intero, mentre il seme è rappresentato da una `enum` `Suit`, che definisce i quattro semi delle carte da gioco
italiane: Pentacles, Cups, Swords e Wands.

L'elemento di Scala più rilevante utilizzato:

- Enum per rappresentare i possibili semi della carta:

```scala 3
enum Suit:
  case Pentacles, Cups, Swords, Wands
```

## Deck
Un `Deck` rappresenta un mazzo di carte ed è costituito da una sequenza ordinata di `Card`. Il mazzo permette di estrarre 
la carta in cima e di essere mischiato.
Il `Deck` è definito tramite un `sealed trait`, che specifica le operazioni disponibili sul mazzo, mentre `DeckImpl`, 
definita come `case class`, ne fornisce l’implementazione tramite un `Vector[Card]`.

I metodi principali del Deck sono:

- `draw(): Option[(Card, Deck)]` estrae la carta in cima al mazzo e restituisce la carta estratta insieme al nuovo `Deck`.
Se il mazzo è vuoto viene restituito `None`.

- `shuffle(): Deck` restituisce un nuovo mazzo contenente le stesse carte in ordine casuale.

Gli elementi di Scala più rilevanti utilizzati sono:

- Utilizzo di tail recursion per l'implementazione del metodo `shuffle`:

```scala 3
  def shuffle(): Deck =
    @tailrec
    def shuffleOn(cards: Vector[Card], shuffledCards: Vector[Card]): Vector[Card] = cards match
      case Vector() => shuffledCards
      case _        =>
        val index = scala.util.Random.nextInt(cards.size)
        val randomCard = cards(index)
        shuffleOn(cards.patch(index, Nil, 1), shuffledCards.appended(randomCard))
    DeckImpl(shuffleOn(this.cards, Vector.empty))
```

- Utilizzo di pattern matching all'interno del metodo `shuffleOn`:

```scala 3
  def shuffleOn(cards: Vector[Card], shuffledCards: Vector[Card]): Vector[Card] = cards match
    case Vector() => shuffledCards
    case _        =>
    ...
```

- Utilizzo di `Option` per gestire il caso in cui si tenti di estrarre una carta da un mazzo vuoto. L’implementazione sfrutta `headOption` e `map`:

```scala 3
  def draw(): Option[(Card, Deck)] = cards.headOption.map(card => (card, DeckImpl(cards.tail)))
```

- Utilizzo di un companion object con factory per la creazione di un mazzo standard che genera una carta per ogni
combinazione tra i quattro semi e i valori da asso a re, ottenendo così un mazzo composto da 40 carte. 
(La costruzione delle carte sfrutta la sintassi fornita dalla `CardDSL`): 

```scala 3
  object DeckFactory:
      def apply(): Deck =
        val cards = for
          suit <- Suit.values
          value <- 0 to 9
        yield value of suit
        DeckImpl(cards.toVector)
```

## Field 
Un `Field` rappresenta la mano di un giocatore ed è costituito da una sequenza ordinata di `Card`. Il campo permette di 
conoscere il numero di carte presenti, accedere alle carte e modificarne il contenuto tramite operazioni di aggiunta, 
rimozione e sostituzione.
Questa struttura è definita tramite un `sealed trait`, che specifica le operazioni disponibili, mentre
`FieldImpl` ne fornisce l’implementazione concreta utilizzando un `Vector[Card]`.

I metodi principali sono:

- `cardsList: List[Card]` restituisce tutte le carte presenti nel campo convertendole in una `List`.

- `getCard(index: Int): (Card, Field)` recupera e rimuove la carta presente all’indice specificato, restituendo
la carta estratta e il nuovo `Field`. Se l’indice non è valido viene lanciata una `IndexOutOfBoundsException`.

- `replace(index: Int, card: Card): (Card, Field)` sostituisce la carta presente all’indice specificato con la carta 
fornita come argomento e restituisce la carta precedentemente presente insieme al nuovo `Field`.

- `addCard(card: Card): Field` aggiunge una carta alla fine del campo e restituisce il nuovo `Field`.

- `addCardAtIndex(card: Card, index: Int): Field` inserisce una carta nella posizione indicata dall’indice, spostando 
verso destra le carte successive. Se l’indice non è valido viene lanciata una `IndexOutOfBoundsException`.

L'elemento di Scala più rilevante:

- Immutabilità e copy: le operazioni che modificano il campo non alterano l’istanza corrente, ma producono una nuova 
istanza di `FieldImpl` tramite `copy`. Questo permette di mantenere invariato lo stato precedente:

```scala 3
  def addCard(card: Card): Field = copy(cards :+ card)
```

## Board
Una `Board` rappresenta lo stato del tavolo di gioco ed è composto dal mazzo di pesca, dai campi dei due giocatori e 
dalla pila degli scarti. È inoltre responsabile delle operazioni che modificano questi elementi, come la pesca e lo 
scarto delle carte, la sostituzione delle carte nei campi dei giocatori e la gestione della pila degli scarti.
La `Board` è definita tramite un `sealed trait`, che specifica le operazioni disponibili sul tavolo, mentre `BoardImpl`, 
definita come `case class`, ne fornisce l’implementazione. Lo stato dei giocatori è rappresentato tramite una
`Map[Player, Field]`, mentre la pila degli scarti è rappresentata da una `List[Card]`, con la carta in testa corrispondente
alla carta in cima alla pila.

L’enum `Player` rappresenta i due giocatori della partita, `Player1` e `Player2`, e fornisce il metodo `other`,
che permette di ottenere il giocatore avversario.

```scala 3
  enum Player:
    case Player1, Player2

    def other: Player = this match
    case Player1 => Player2
    case _       => Player1
```

I metodi principali del Board sono:

- `draw(): Option[(Card, Board)]` estrae una carta dal mazzo. Nel caso in cui il mazzo sia vuoto, le carte presenti 
nella pila degli scarti vengono utilizzate per creare un nuovo mazzo mischiato. Se anche la pila degli scarti è vuota,
la pesca restituisce `None`.

- `discard(card: Card): Board` aggiunge una carta in cima alla pila degli scarti.

- `replace(player: Player, cardIndex: Int, card: Card): Board` sostituisce una carta presente nel campo del giocatore 
con quella fornita, inserendo la carta sostituita in cima alla pila degli scarti.

- `kingTopDiscardStack(): Option[(Card, Board)]` permette di recuperare il Re presente in cima alla pila degli 
scarti, rimuovendolo dalla pila. Se la carta in cima non è un Re, restituisce `None`.

- `drawPlayerCard(player: Player, index: Int): (Card, Board)` rimuove una carta dal campo del giocatore indicato e 
restituisce la carta estratta insieme al nuovo stato del Board.

- `placeCardInField(card: Card, player: Player, index: Option[Int]): Board` inserisce una carta nel campo del giocatore. 
Se viene fornito un indice valido, la carta viene inserita nella posizione indicata, altrimenti viene aggiunta 
in fondo al campo.

Aspetti rilevanti di Scala all'interno di questa implementazione sono:

- Utilizzo di `Map` per associare ogni `Player` al proprio `Field`, permettendo di recuperare e aggiornare il campo
di un giocatore attraverso la relativa chiave:

```scala 3
  val players: Map[Player, Field]
  players(player)
  players.updated(player, updatedField)
```

- Utilizzo di `Option` per rappresentare il risultato della pesca:

```scala 3
  def draw(): Option[(Card, BoardImpl)] =
    val checked = checkDeck()
    checked.deck.draw().map((card, remainingDeck) => (card, checked.copy(deck = remainingDeck)))
```

- Utilizzo di pattern matching nel metodo `placeCardInField` per distinguere il caso in cui venga fornito 
un indice da quello in cui non venga fornito:

```scala 3
  def placeCardInField(card: Card, player: Player, index: Option[Int]): Board =
    index match
      case Some(i) if i < players(player).length && i >= 0 =>
        copy(players = players.updated(player, players(player).addCardAtIndex(card, i)))
      case _ => copy(players = players.updated(player, players(player).addCard(card)))
```

- Utilizzo di `copy` e delle strutture dati immutabili per aggiornare lo stato del `Board` senza modificare
l’istanza corrente. Le operazioni producono infatti un nuovo `BoardImpl` con i soli elementi interessati 
dall’operazione aggiornati.

## DSL
Sono state create DSL per le entità `Deck` e `Board`.

### Deck
Per agevolare la creazione e l'utilizzo delle entità `Deck`(soprattutto all’interno delle classi di test) è stato
realizzato l’object `DeckDSL`.
`DeckDSL` permette di creare un `Deck` attraverso una sintassi più semplice e vicina al linguaggio naturale.
Mette a disposizione diversi metodi per creare un mazzo standard, costruirne uno a partire da una
sequenza di Card oppure ottenere un mazzo mischiato. La DSL introduce inoltre il `CardBuilder`, che permette di 
concatenare più carte attraverso l’operatore `|`, e una given Conversion che consente di convertire automaticamente
un `CardBuilder` in un `Deck`.

Ecco un esempio di un `Deck` con due carte creato con la DSL:

```scala 3
val deck = deck from (ace of Cups | two of Swords)
```

Gli elementi di Scala più rilevanti utilizzati sono:

- Extension method: viene definito un metodo di estensione `|` per `Card`, 
che permette di utilizzare una carta come punto di partenza per la costruzione di un `CardBuilder`:

```scala 3
  extension (card: Card)
    infix def |(other: Card): CardBuilder = CardBuilder(Vector(card, other))
```

- Case class CardBuilder: rappresenta una struttura intermedia contenente le carte che verranno utilizzate 
per costruire il mazzo. L’operatore `|` permette di aggiungere ulteriori carte alla sequenza:

```scala 3
  case class CardBuilder(cards: Vector[Card]):
      infix def |(other: Card): CardBuilder = CardBuilder(cards :+ other)
```

- Given Conversion: permette di convertire implicitamente un `CardBuilder` in un `Deck`, rendendo possibile utilizzare 
direttamente una catena di carte nei contesti in cui è richiesto un mazzo:

```scala 3
  given Conversion[CardBuilder, Deck] = b => DeckImpl(b.cards)
```

### Board
Per agevolare la creazione e la configurazione dell’entità `Board`, è stato realizzato l’object `BoardDSL`. 
La DSL permette di creare una configurazione di board predefinita oppure di personalizzare i campi dei giocatori, 
il mazzo e la pila degli scarti.

BoardDSL mette a disposizione tre entry point principali:
* `default board` per ottenere direttamente un Board con i campi dei giocatori già popolati.
* `board from default` per ottenere un BoardBuilder configurabile.
* `lockedBoard from default` per ottenere un BoardBuilder configurabile in modalità bloccata.

L’entry point default board crea direttamente una board con un campo di quattro carte
per ciascun giocatore a partire dal deck di default con 40 carte italiane e pila degli scarti vuota:

``` scala 3
  import BoardDSL.*
  val board: Board = default board
```

Per ottenere invece una configurazione personalizzabile si utilizza `board from default`,
che restituisce un `BoardBuilder`. Le personalizzazioni possono essere aggiunte attraverso
il metodo `withCustom`:

```scala 3
  import BoardDSL.*
  import CardDSL.*
  import FieldDSL.*

  val c1 = ace of Cups
  val c2 = two of Swords
  val board: Board = (board from default)
    .withCustom(playerOne(c1 and c2))
```

Le personalizzazioni possibili sono rappresentate dal trait Customisation e dalle
relative implementazioni `PlayerOneCards`, `PlayerTwoCards`, `CustomDeck` e
`CustomDiscard`:

```scala 3
  sealed trait Customisation
  case class PlayerOneCards(field: Field) extends Customisation
  case class PlayerTwoCards(field: Field) extends Customisation
  case class CustomDeck(deck: Deck) extends Customisation
  case class CustomDiscard(cards: List[Card]) extends Customisation
```

Una caratteristica importante della DSL è la distinzione tra modalità `Unlocked` e
`Locked`. La modalità unlocked viene ottenuta tramite `board from default` e lascia
inalterato il mazzo rispetto alle carte assegnate manualmente ai giocatori. La modalità
locked viene invece ottenuta tramite `lockedBoard from default` e rimuove dal mazzo
le carte assegnate ai giocatori e alla pila degli scarti.
La configurazione viene accumulata all’interno di `BoardBuilder` attraverso il metodo
`withCustom`, che non modifica il builder esistente ma ne restituisce uno nuovo contenente
la personalizzazione aggiunta:

```scala 3
  private def removeCards(deck: Deck, toRemove: List[Card]): Deck =
    val remaining = toRemove.foldLeft(deck.cards.toList) { (cards, target) =>
      val id = cards.indexWhere(c => c.value == target.value && c.suit == target.suit)
      if id >= 0 then cards.patch(id, Nil, 1) else cards
    }
    DeckImpl(remaining.toVector)
```

Infine, la conversione implicita da `BoardBuilder` a `Board` permette di utilizzare
direttamente il builder in un contesto in cui è richiesto un `Board`, senza dover
richiamare esplicitamente build. 
Questa DSL, può essere combinata con CardDSL, DeckDSL e FieldDSL per costruire
una configurazione completa del Board:

```scala 3
  val board =
    (board from default)
      .withCustom(playerOne(ace of Cups and two of Swords))
      .withCustom(playerTwo(three of Cups))
      .withCustom(customDeck(myDeck))
      .withCustom(discardPile(four of Cups and five of Cups))
```

## Game
`Game` rappresenta lo stato complessivo di una partita e gestisce l’avanzamento del gioco tra i diversi turni 
e le diverse fasi. Contiene il `Turn` corrente, la fase della partita e, quando presente, il giocatore che 
ha chiamato cactus.

La fase della partita è rappresentata dall’`enum` `GamePhase`, che distingue quattro possibili stati:

```scala 3
  enum GamePhase:
    case FirstTurns
    case Playing
    case LastTurn
    case Over
```

I metodi principali di Game sono:

- `act(action: Action): Game` delega l’esecuzione dell’azione al `Turn` corrente. Se il turno non è ancora terminato, 
aggiorna solamente il turno corrente. Se invece il turno termina, gestisce il passaggio alla fase o al giocatore successivo. 
Se la partita è già terminata, lancia `IllegalStateException`. 
- `playerScore: Map[Player, Int]` calcola il punteggio di ciascun giocatore sommando il valore delle carte 
presenti nel rispettivo campo.

L’object `Game` fornisce inoltre il metodo `apply(board: Board): Game`, che permette di creare una nuova partita a partire 
da un `Board`. La partita viene inizializzata nella fase `FirstTurns`, assegnando il primo turno a `Player1`:

```scala 3
  object Game:
    def apply(board: Board): Game =
      val firstTurn = FirstTurn(board, Player1)
      Game(GamePhase.FirstTurns, firstTurn, cactusCaller = None)
```

Gli aspetti di scala principali sono:
- Pattern matching nel metodo `advancePhase` per gestire le diverse fasi della partita e il giocatore che ha appena 
concluso il turno:

```scala 3
  private def advancePhase(finishedTurn: Turn): Game =
    phase match
      case GamePhase.FirstTurns =>
        finishedTurn.player match
          case Player1 => ...
          case Player2 => ...
      case GamePhase.Playing => ...
      case GamePhase.LastTurn => ...
      case GamePhase.Over => this
```
- Utilizzo di Map e higher-order functions nel metodo `playerScore` che utilizza `map` per associare ogni giocatore 
al proprio punteggio e `toMap` per ottenere la struttura finale:

```scala 3
  def playerScore: Map[Player, Int] =
    Player.values.map(p => p -> currentTurn.board.getField(p).cardsList.map(_.value).sum).toMap
```
- Utilizzo di export, vengono esposti direttamente alcuni membri del `Turn` corrente, permettendo a `Game` di riutilizzarli 
senza dover accedere esplicitamente a `currentTurn`:

```scala 3
  case class Game(
    phase: GamePhase,
    currentTurn: Turn,
    cactusCaller: Option[Player]
  ) extends Playable[Game]:
  export currentTurn.{player as currentPlayer, isOver as isTurnOver, act as _, *}
```

## Showcase
Gli `Showcase` permettono di predisporre entità `Board` tramite le DSL, 
creando `Board` configurate per dimostrare specifiche meccaniche di gioco.

Sono stati dichiarati i seguenti Showcase:

* `SixShowcase`: effetto speciale delle carte di valore sei;
* `SevenShowcase`: effetto speciale delle carte di valore sette;
* `JackShowcase`: scambio di una carta tra i due giocatori;
* `KingDrawShowcase`: pesca di un re dalla pila degli scarti;
* `SuccessfulDiscard`: meccanica di scarto in caso di successo;
* `FailedDiscardShowcase`: meccanica di scarto in caso di insuccesso.

Per questi Showcase è stata realizzata la sola dichiarazione e configurazione tramite le DSL.
La loro effettiva integrazione e utilizzo nel gioco è stata invece lasciata a un altro membro del gruppo.

Esempio di `showcase` utilizzando concretamente tutte le DSL:
```scala 3
  private val deckForSixEffect = deck from
    (six of Swords) | (three of Cups) | (king of Cups) | (king of Swords)
  
  private val discardPileWithoutKing = (three of Cups) | (seven of Cups)
      
  def boardForSixEffect: BoardBuilder = (lockedBoard from default)
    .withCustom(customDeck(deckForSixEffect))
    .withCustom(discardPile(discardPileWithoutKing))
    .withCustom(playerOne((three of Pentacles) and (knight of Pentacles)))
    .withCustom(playerTwo((four of Pentacles) and (five of Pentacles)))
```

## Testing
I test sono stati sviluppati seguendo un approccio TDD (Test-Driven Development), 
con l’obiettivo di verificare il corretto funzionamento del sistema e ottenere un’elevata copertura del codice.

---

1. [Processo di sviluppo](processo.md)
    1. [Sprint 1](process/sprint01.md)
    2. [Sprint 2](process/sprint02.md)
    3. [Sprint 3](process/sprint03.md)
    4. [Sprint 4](process/sprint04.md)
2. [Requisiti](requisiti.md)
3. [Architettura](architettura.md)
4. [Design di dettaglio](design-di-dettaglio.md)
5. [Implementazione](implementazione.md)
    1. [Alex Casadei](implementation/casadei.md)
    2. [Luca Ferar](implementation/ferar.md)
    3. [**Simone Zama (prossimo)**](implementation/zama.md)
6. [Testing](testing.md)
7. [Retrospettiva](retrospettiva.md)