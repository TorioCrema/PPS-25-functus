# Implementazione - Simone Zama

## Panoramica dei contributi

Il mio contributo al progetto è focalizzato nelle seguenti aree:

- **Actions**: implementazione delle azioni che i giocatori possono eseguire durante il proprio turno.
- **Turn**: implementazione del turno di un giocatore.
- **Effects**: implementazione degli effetti delle carte.
- **Match**: implementazione di un match.
- **Opponent**: implementazione dell'avversario virtuale.
- **Testing**: scrittura dei test per tutti i sistemi implementati

## Actions

Una `Action` rappresenta una possibile azione che un giocatore puo' eseguire durante il proprio turno.
Nel contesto del turno, esse rappresentano transizioni di stato, infatti è possibile considerare un turno
come una macchina a stati finiti le cui transizioni sono le azioni disponibili in ogni stato.
Ogni `Action` possiede un metodo `next` che restituisce le azioni disponibili dopo l'azione corrente.

## Turn

Come detto in precedenza, un turno è interpretabile come una macchina a stati finiti.
I suoi stati possibli sono formati dal prodotto cartesiano di: giocatore, board, mano del giocatore, azioni
disponibili e un valore che indica se durante il turno è stato chiamato Cactus.
Da questa osservazione deriva l'implementazione tramite `case class` di nome `Turn` con campi di tipi corrispondenti
agli elementi sopraelencati. Questa classe estende `Playable[Turn]` e possiede i due metodi pubblici: `act` e `isOver`.
Esistono tre "tipi" di turno possibili, a seconda della fase della partita in cui essi vengono giocati, questi sono: il
primo turno di ogni giocatore, l'ultimo turno della partita, e i turni semplici ottenuti per esclusione. Questi "tipi" si
differenziano per le azioni disponibili alla loro creazione e, nel caso dell'ultimo turno, dall'assenza dell'azione `Cactus`.
Il companion object `Turns` contiene i factory method per ogni tipologia di turno.

- `act(action: Action): Turn` esegue l'azione passata come argomento sullo stato corrente del turno
    e restituisce un nuovo turno il cui stato è quello risultante dalle modifiche ottenute dall'esecuzione
    dell'azione, e le sue azioni disponibili sono quelle fornite da `action.next`. Per evitare che l'azione
    `Cactus` sia disponibile durante l'ultimo turno della partita (`Game`), essa viene rimossa qualora la flag
    `cactus` del turno sia `true`.
    Nel caso in cui l'azione passata come argomento non appartenga alle azioni disponibli nell'attuale stato
    del turno, viene lanciata una `IllegalArgumentException`. Tramite un match case viene individuato il tipo
    dell'azione ed essa viene eseguita modificando i campi dell'istanza di `Turn` su cui è stato chiamato il metodo,
    ad esempio per l'azione `Observe` che raccoglie le prime due carte dal campo del giocatore e le inserisce nella sua mano:
    ```scala 3
    action match
      case Observe =>
        List
          .fill(observableCards)(0)
          .foldLeft(this)((turn, index) => turn.drawnFromField(index))
          .withActions(action.next)
    ```
- `isOver: Boolean` restituisce `true` qualora il turno sia completato, ovvero quando non sono disponibili azioni.

Elementi rilevanti di Scala all'interno di questa implementazione sono:
- Companion object con factory method per creare il turno.
  Sono presenti tre factory, una per ogni tipologia di turno da creare:
  ```scala 3
  object Turns:
    object FirstTurn:
      def apply(...): Turn = ...
    object SimpleTurn:
      def apply(...): Turn = ...
    object LastTurn:
      def apply(...): Turn = ...
  ```
  
## Effects

Quando pescate, le carte sei, sette, e fante permettono di effettuare azioni aggiuntive rispetto
al resto delle carte nel mazzo. La generazione di queste azioni è ottenuta tramite il metodo `effect`
dell'object `Effects`. Questo metodo prende in ingresso il contesto in cui l'effetto viene eseguito,
ovvero la carta attivata e il turno in cui essa viene attivata e, tramite un match case, individua il valore della carta
attivata e le azioni che ottenute dalla sua attivazione nel contesto del turno attuale:
```scala 3
def actionsFromFieldLength(fieldLength: Int)(action: Int => Action) =
      replaceActions.appendedAll(for i <- 0 until fieldLength yield action(i))

activated.value match
  case `six`   => actionsFromFieldLength(getFieldLength(turn.player.other))(ObserveOpponent(_)) 
  case `seven` => actionsFromFieldLength(getFieldLength(turn.player))(ObservePlayer(_))
  case `jack`  =>
    val swapActions =
      for
        playerIndex <- 0 until getFieldLength(turn.player)
        opponentIndex <- 0 until getFieldLength(turn.player)
      yield Swap(playerIndex, opponentIndex)
    replaceActions.appendedAll(swapActions)
  case _ => replaceActions
```

Elementi rilevanti di Scala in questa implementazione sono:

- Contextual programming tramite i parametri `using` del metodo `effect`:
  ```scala 3
  def effect(using Option[Card], Option[Turn]): List[Action] =
    require(summon[Option[Card]].isDefined && summon[Option[Turn]].isDefined)
    val (activated, turn) = (summon[Option[Card]].get, summon[Option[Turn]].get)
    val getFieldLength: Player => Int = turn.board.getField(_).length
    val replaceActions = (for i <- 0 until getFieldLength(turn.player) yield ChooseReplace(i)).toList
  ```

## Match

Un `Match` rappresenta un insieme di partite (`Game`) consecutive il cui punteggio finale viene accumulato
fino al superamento del punteggio limite. È implemenetato dalla `case class` `Match`, le cui possibili istanze
sono date dal prodotto cartesiano del punteggio massimo, il `Game` in corso, e gli attuali punteggi cumulativi dei
giocatori. La classe estende il trait `Playable[Match]`, fornendo i metodi `act` e `isOver`:

- `act(action: Action): Match` delega l'esecuzione dell'azione a `Game`, nel caso in cui esso sia terminato
    aggiorna i punteggi dei giocatori.
- `isOver: Boolean`: restituisce `true` qualora il `Match` sia completato, ovvero quando almeno uno dei punteggi
    cumulativi dei giocatori supera il punteggio limite impostato alla creazione del `Match`.

La classe fornisce inoltre il metodo `nextGame` per ottenere l'istanza di `Match` da cui iniziare il prossimo `Game`:
```scala 3
def nextGame: Match =
  if !game.isOver then throw new IllegalStateException("Cannot start new game while current game has not ended.")
  copy(game = Game(newBoard from default))
```

Elementi rilevanti di Scala in questa implementazione sono:

- Utilizzo di `export` per esporre accesso alle informazioni relative al `Game` in corso all'interno del `Match`:
    ```scala 3
    export game.{act as _, isOver as isGameOver, *}
    ```
  il metodo `act` di `Game` viene nascosto dato che il suo utilizzo è dettato dalla delegazione all'interno del metodo
  `act` di `Match`, e il metodo `isOver` è rinominato in `isGameOver`.
- Companion object con factory method per creare il `Match`:
    ```scala 3
    object Match:
      def apply(maxScore: Int, board: Board = newBoard from default): Match =
        Match(maxScore, Game(board), Map((Player1, 0), (Player2, 0)))
    ```

## Opponent

L'implementazione dell'avversario virtuale è contenuta nella classe `Opponent`. Le sue funzionalità principali sono
i metodi `play` e `react`, che contengono rispettivamente le implementazioni del ruolo attivo e passivo che `Opponent`
ricopre durante il corso di un `Game`.

- Tramite il metodo `play(turn: Turn): (Turn, Action)` l'`Opponent` osserva le azioni disponibili e, a seconda della sua 
  attuale conoscenza delle carte in campo, sceglie quella piu' vantaggiosa. Una volta determinata l'azione, la conoscenza
  dell'`Opponent` viene aggiornata secondo le conseguenze che essa ha sul campo attuale:
  ```scala 3
  def play(turn: Turn): (Turn, Action) = getChosenAction(turn) match
    case Observe                           => ...
    case ChooseDiscard(index)              => ...
    case ObserveOpponent(index)            => drawAndUpdateKnownCards(turn, ObserveOpponent(index))
    case ObservePlayer(index)              => drawAndUpdateKnownCards(turn, ObservePlayer(index))
    case Swap(playerIndex, adversaryIndex) => ...
    case ChooseReplace(index)              => ...
    case chosenAction                      => (turn.act(chosenAction), chosenAction)
  ```
- Il metodo `react(action: Action, turn: Turn): Unit` permette a `Opponent` di aggiornare la propria conoscenza delle
  carte sul campo in base all'azione eseguita dall'utente:
  ```scala 3
  def react(action: Action, turn: Turn): Unit = action match
    case ChooseDiscard(index)
        if knows(adversaryCards)(index)
          && turn.board.getTopDiscardStack.value == getKnownAdversaryCard(index).get.value =>
      forgetAndUpdate(index)
    case ChooseReplace(index) if knows(adversaryCards)(index) => adversaryCards = adversaryCards.removed(index)
    case Swap(adversaryIndex, ownIndex)                       => swapReaction(adversaryIndex, ownIndex)
    case _                                                    => ()
  ```

Elementi rilevanti di Scala in questa implementazione sono:

- Pattern matching per la gestione della reazione all'azione `Swap`:
  ```scala 3
  private def swapReaction(adversaryIndex: Int, ownIndex: Int): Unit =
    (knows(adversaryCards)(adversaryIndex), knows(knownCards)(ownIndex)) match
      case (true, true)  => ...
      case (true, false) => ...
      case (false, true) => ...
      case (_, _) => ()
  ```
- Utilizzo di tipi funzionali per definire i predicati con cui filtrare le azioni favorevoli, ad esempio:
  ```scala 3
  private def getChosenAction(turn: Turn): Action =
    val actions = turn.actions
      .filter(isDiscardable(_, turn))
      .appendedAll(turn.actions.filter(unknownObservePlayer))
    ...
  
  private def unknownObservePlayer: Action => Boolean = {
    case ObservePlayer(index) if !knows(knownCards)(index) => true
    case _                                                 => false
  }
  ```
- Companion object per creare una nuova istanza di `Opponent` senza usare `new`:
  ```scala 3
  object Opponent:
  def apply(): Opponent = new Opponent()
  ```
