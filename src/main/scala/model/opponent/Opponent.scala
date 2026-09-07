package org.pps.functus
package model.opponent

import model.playable.turn.{Action, Turn}
import model.deck.card.Card
import model.playable.turn.Action.*

/** Class that represents a virtual opponent, capable of remembering the cards it observes and playing accordingly. */
class Opponent:
  private var knownCards: Map[Int, Card] = Map()
  private var adversaryCards: Map[Int, Card] = Map()
  private val cactusThreshold: Int = 5
  private var kingDrawnByAdversary: Option[Card] = None

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
      swapKnowledge(playerIndex, adversaryIndex)
      (turn.act(Swap(playerIndex, adversaryIndex)), Swap(playerIndex, adversaryIndex))
    case ChooseReplace(index) =>
      knownCards = knownCards.updated(index, turn.hand.head)
      (turn.act(ChooseReplace(index)), ChooseReplace(index))
    case chosenAction => (turn.act(chosenAction), chosenAction)

  private def getChosenAction(turn: Turn): Action =
    val favourableActions = turn.actions
      .filter(isDiscardable(_, turn))
      .appendedAll(turn.actions.filter(unknownObservePlayer))
      .appendedAll(turn.actions.filter(unknownObserveOpponent))
      .appendedAll(turn.actions.filter(favourableSwap))
      .appendedAll(turn.actions.filter(unknownReplace(turn)))
    if favourableActions.nonEmpty then favourableActions.head
    else if canReplace(turn.actions) then ChooseReplace(knownCards.maxBy((index, card) => card.value)._1)
    else
      turn.actions match
        case `Draw` :: `DrawKing` :: Nil  => DrawKing
        case `Draw` :: _                  => Draw
        case `Cactus` :: `EndTurn` :: Nil => checkCactus(turn)
        case action :: Nil                => action
        case _ => throw new NotImplementedError("No choice of action implemented for given actions.")

  /** Reacts an action performed by the adversary based on the [[Opponent]]'s current knowledge.
    * @param action
    *   the adversary's [[Action]]
    * @param turn
    *   the [[Turn]] the [[Action]] is being performed on
    */
  def react(action: Action, turn: Turn): Unit =
    require(turn.actions.contains(action))
    action match
      case ChooseDiscard(index)
          if knows(adversaryCards)(index)
            && turn.board.getTopDiscardStack.value == getKnownAdversaryCard(index).get.value =>
        forgetAndUpdate(index)
      case ChooseReplace(index) if kingDrawnByAdversary.isDefined =>
        adversaryCards = adversaryCards.updated(index, kingDrawnByAdversary.get)
        kingDrawnByAdversary = None
      case ChooseReplace(index) if knows(adversaryCards)(index) => adversaryCards = adversaryCards.removed(index)
      case Swap(adversaryIndex, ownIndex)                       => swapReaction(adversaryIndex, ownIndex)
      case DrawKing => kingDrawnByAdversary = Some(turn.board.getTopDiscardStack)
      case _        => ()

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

  private def forgetAndUpdate(index: Int): Unit =
    adversaryCards = adversaryCards.removed(index)
    adversaryCards = adversaryCards.map((i, card) => if i > index then (i - 1, card) else (i, card))

  private def swapReaction(adversaryIndex: Int, ownIndex: Int): Unit =
    (knows(adversaryCards)(adversaryIndex), knows(knownCards)(ownIndex)) match
      case (true, true)  => swapKnowledge(ownIndex, adversaryIndex)
      case (true, false) =>
        val adversaryCard = adversaryCards(adversaryIndex)
        adversaryCards = adversaryCards.removed(adversaryIndex)
        knownCards = knownCards.updated(ownIndex, adversaryCard)
      case (false, true) =>
        val ownCard = knownCards(ownIndex)
        knownCards = knownCards.removed(ownIndex)
        adversaryCards = adversaryCards.updated(adversaryIndex, ownCard)
      case (_, _) => ()

  private def swapKnowledge(ownIndex: Int, adversaryIndex: Int): Unit =
    val ownSwappedCard = knownCards(ownIndex)
    knownCards = knownCards.updated(ownIndex, adversaryCards(adversaryIndex))
    adversaryCards = adversaryCards.updated(adversaryIndex, ownSwappedCard)

  private def drawAndUpdateKnownCards(turn: Turn, action: Action): (Turn, Action) =
    val drawn = turn.act(action)
    action match
      case ObserveOpponent(index) => adversaryCards = adversaryCards.updated(index, drawn.hand.head)
      case ObservePlayer(index)   => knownCards = knownCards.updated(index, drawn.hand.head)
      case _                      => throw new IllegalArgumentException(s"Action not allowed: $action")
    (drawn, action)

  private def isDiscardable(action: Action, turn: Turn): Boolean = action match
    case ChooseDiscard(index) if knows(knownCards)(index) =>
      knownCards(index).value == turn.board.getTopDiscardStack.value
    case _ => false

  private def canReplace(actions: List[Action]): Boolean =
    actions.exists {
      case ChooseReplace(_) => true
      case _                => false
    }

  private def unknownObserveOpponent: Action => Boolean = {
    case ObserveOpponent(index) if !knows(adversaryCards)(index) => true
    case _                                                       => false
  }
  private def unknownObservePlayer: Action => Boolean = {
    case ObservePlayer(index) if !knows(knownCards)(index) => true
    case _                                                 => false
  }

  private def unknownReplace(turn: Turn): Action => Boolean = {
    case ChooseReplace(index) if turn.hand.nonEmpty && !knows(knownCards)(index) => true
    case _                                                                       => false
  }

  private def checkCactus(turn: Turn): Action =
    def checkKnownCards: Boolean =
      knownCards.toList.length >= turn.board.getField(turn.player).length / 2
    def checkKnownFieldValue: Boolean =
      knownCards.values.foldLeft(0)((acc, card) => acc + card.value) <= cactusThreshold
    if checkKnownCards && checkKnownFieldValue then Cactus else EndTurn

  private def favourableSwap(action: Action): Boolean = action match
    case Swap(ownIndex, adversaryIndex) if knows(knownCards)(ownIndex) && knows(adversaryCards)(adversaryIndex) =>
      knownCards(ownIndex).value > adversaryCards(adversaryIndex).value
    case _ => false

  private def mapFromHand(hand: List[Card]): Map[Int, Card] =
    hand.zipWithIndex.foldLeft(Map())((map, pair) => map.updated(pair._2, pair._1))

  private def getKnownCardFrom(cardMap: Map[Int, Card])(index: Int): Option[Card] =
    if knows(cardMap)(index) then Some(cardMap(index)) else None

  private def knows(knownMap: Map[Int, Card])(index: Int): Boolean = knownMap.contains(index)
