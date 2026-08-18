package org.pps.functus
package model.opponent

import model.turn.{Action, Turn}
import model.deck.card.Card
import model.turn.Action.*

class Opponent:
  private var knownCards: Map[Int, Card] = Map()
  private val cactusThreshold = 5

  def play(turn: Turn): (Turn, Action) = getChosenAction(turn) match
    case Observe =>
      knownCards = mapFromHand(turn.act(Observe).hand)
      (turn.act(Observe), Observe)
    case ChooseDiscard(index) =>
      for x <- index until knownCards.size - 1 do knownCards = knownCards.updated(x, knownCards(x + 1))
      knownCards = knownCards.removed(knownCards.size - 1)
      (turn.act(ChooseDiscard(index)), ChooseDiscard(index))
    case chosenAction => (turn.act(chosenAction), chosenAction)

  private def getChosenAction(turn: Turn): Action =
    if canDiscard(turn.actions) && checkKnownDiscard(turn) then
      val chosenDiscard = knownCards.map((index, card) => (card.value, index))(turn.board.getTopDiscardStack.value)
      ChooseDiscard(chosenDiscard)
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

  private def canDiscard(actions: List[Action]): Boolean = checkActions(actions)(_ match
    case ChooseDiscard(_) => true
    case _                => false)

  private def canReplace(actions: List[Action]): Boolean = checkActions(actions)(_ match
    case ChooseReplace(_) => true
    case _                => false)

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

  private def mapFromHand(hand: List[Card]): Map[Int, Card] =
    hand.zipWithIndex.foldLeft(Map())((map, pair) => map.updated(pair._2, pair._1))

  private def knows(index: Int): Boolean = knownCards.contains(index)
  def getKnownCard(index: Int): Option[Card] = if knows(index) then Some(knownCards(index)) else None
