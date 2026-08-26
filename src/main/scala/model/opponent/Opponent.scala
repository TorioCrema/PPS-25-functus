package org.pps.functus
package model.opponent

import model.turn.{Action, Turn}
import model.deck.card.Card
import model.turn.Action.*

/** Class that represents a virtual opponent, capable of remembering the cards it observes and playing accordingly. */
class Opponent:
  private var knownCards: Map[Int, Card] = Map()
  private var adversaryCards: Map[Int, Card] = Map()
  private val cactusThreshold = 5

  /** Selects the appropriate action from the available ones and applies it to the current turn.
    * @param turn
    *   the turn to play.
    * @return
    *   a [[Tuple]] of the next phase of the [[Turn]] and the chosen [[Action]].
    */
  def play(turn: Turn): (Turn, Action) = getChosenAction(turn) match
    case Observe =>
      knownCards = mapFromHand(turn.act(Observe).hand)
      (turn.act(Observe), Observe)
    case ChooseDiscard(index) =>
      for x <- index until knownCards.size - 1 do knownCards = knownCards.updated(x, knownCards(x + 1))
      knownCards = knownCards.removed(knownCards.size - 1)
      (turn.act(ChooseDiscard(index)), ChooseDiscard(index))
    case ObserveOpponent(index)            => drawAndUpdateKnownCards(turn, ObserveOpponent(index))
    case ObservePlayer(index)              => drawAndUpdateKnownCards(turn, ObservePlayer(index))
    case Swap(playerIndex, adversaryIndex) =>
      knownCards = knownCards.updated(playerIndex, adversaryCards(adversaryIndex))
      forgetAdversary(adversaryIndex)
      (turn.act(Swap(playerIndex, adversaryIndex)), Swap(playerIndex, adversaryIndex))
    case chosenAction => (turn.act(chosenAction), chosenAction)

  /** Returns [[Option]] of the card within the [[Opponent]] field if known, [[None]] otherwise.
    * @param index
    *   the index of the card in the field.
    */
  def getKnownCard(index: Int): Option[Card] = getKnownCardFrom(knownCards)(index)

  /** Returns [[Option]] of the card within the adversary's field if known, [[None]] otherwise.
    * @param index
    *   the index of the card in the field.
    */
  def getKnownAdversaryCard(index: Int): Option[Card] = getKnownCardFrom(adversaryCards)(index)

  private def getKnownCardFrom(cardMap: Map[Int, Card])(index: Int): Option[Card] =
    if knows(cardMap)(index) then Some(cardMap(index)) else None

  /** Removes the card at the given index from known cards.
    * @param index
    *   the index of the card to forget.
    */
  def forgetOwn(index: Int): Unit = if knows(knownCards)(index) then knownCards = knownCards.removed(index)

  /** Removes the card at the given index from known adversary cards.
    * @param index
    *   the index of the card to forget.
    */
  def forgetAdversary(index: Int): Unit =
    if knows(adversaryCards)(index) then adversaryCards = adversaryCards.removed(index)

  private def drawAndUpdateKnownCards(turn: Turn, action: Action): (Turn, Action) =
    val drawn = turn.act(action)
    action match
      case ObserveOpponent(index) => adversaryCards = adversaryCards.updated(index, drawn.hand.head)
      case ObservePlayer(index)   => knownCards = knownCards.updated(index, drawn.hand.head)
    (drawn, action)

  private def getChosenAction(turn: Turn): Action =
    if canDiscard(turn.actions) && checkKnownDiscard(turn) then
      val chosenDiscard = knownCards.map((index, card) => (card.value, index))(turn.board.getTopDiscardStack.value)
      ChooseDiscard(chosenDiscard)
    else if canObserveAdversary(turn.actions) then turn.actions.find(unknownObserveOpponent(adversaryCards)).get
    else if canObservePlayer(turn.actions) then turn.actions.find(unknownObservePlayer(knownCards)).get
    else if canSwap(turn.actions) then turn.actions.find(favourableSwap).get
    else if canReplace(turn.actions) then
      val unknownCards = for
        x <- 0 until turn.board.getField(turn.player).length
        if !knownCards.contains(x)
      yield x
      val chosenReplace =
        if unknownCards.nonEmpty then unknownCards.head
        else knownCards.maxBy((i, card) => card.value)._1
      knownCards = knownCards.updated(chosenReplace, turn.hand.head)
      ChooseReplace(chosenReplace)
    else
      turn.actions match
        case `Draw` :: `DrawKing` :: Nil  => DrawKing
        case `Cactus` :: `EndTurn` :: Nil => checkCactus(turn)
        case action :: Nil                => action
        case _ => throw new NotImplementedError("Not choice of action implemented for given actions.")

  private def canDiscard(actions: List[Action]): Boolean = checkActions(actions) {
    case ChooseDiscard(_) => true
    case _                => false
  }

  private def canReplace(actions: List[Action]): Boolean = checkActions(actions) {
    case ChooseReplace(_) => true
    case _                => false
  }

  private def unknownObserveOpponent(knownMap: Map[Int, Card]): Action => Boolean = {
    case ObserveOpponent(index) if !knows(knownMap)(index) => true
    case _                                                 => false
  }
  private def unknownObservePlayer(knownMap: Map[Int, Card]): Action => Boolean = {
    case ObservePlayer(index) if !knows(knownMap)(index) => true
    case _                                               => false
  }

  private def canObserveAdversary(actions: List[Action]): Boolean =
    checkActions(actions)(unknownObserveOpponent(adversaryCards))
  private def canObservePlayer(actions: List[Action]): Boolean =
    checkActions(actions)(unknownObservePlayer(knownCards))

  private def checkActions(action: List[Action])(p: Action => Boolean): Boolean = action.exists(p)

  private def checkCactus(turn: Turn): Action =
    def checkKnownCards: Boolean =
      knownCards.toList.length >= turn.board.getField(turn.player).length / 2
    def checkKnownFieldValue: Boolean =
      knownCards.values.foldLeft(0)((acc, card) => acc + card.value) <= cactusThreshold
    if checkKnownCards && checkKnownFieldValue then Cactus else EndTurn

  private def checkKnownDiscard(turn: Turn): Boolean =
    if turn.board.discardPile.nonEmpty then
      knownCards.values.toList.map(_.value).contains(turn.board.getTopDiscardStack.value)
    else false

  private def favourableSwap(action: Action): Boolean = action match
    case Swap(ownIndex, adversaryIndex) if knows(knownCards)(ownIndex) && knows(adversaryCards)(adversaryIndex) =>
      knownCards(ownIndex).value > adversaryCards(adversaryIndex).value
    case _ => false

  private def canSwap(actions: List[Action]): Boolean = checkActions(actions)(favourableSwap)

  private def mapFromHand(hand: List[Card]): Map[Int, Card] =
    hand.zipWithIndex.foldLeft(Map())((map, pair) => map.updated(pair._2, pair._1))

  private def knows(knownMap: Map[Int, Card])(index: Int): Boolean = knownMap.contains(index)
