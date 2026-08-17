package org.pps.functus
package model.opponent

import model.turn.{Action, Turn}
import model.deck.card.Card
import model.turn.Action.*

class Opponent(private var knownCards: Map[Int, Card] = Map()):
  private def getChosenAction(availableActions: List[Action], turn: Turn): Action =
    if canDiscard(availableActions) && checkKnownDiscard(turn) then
      val chosenDiscard = knownCards.map((index, card) => (card.value, index))(turn.board.getTopDiscardStack.value)
      ChooseDiscard(chosenDiscard)
    else if canReplace(availableActions) then
      val unknownCards = for
        x <- 0 until turn.board.getField(turn.player).length
        if !knownCards.contains(x)
      yield x
      val chosenReplace =
        if unknownCards.nonEmpty then unknownCards.head
        else knownCards.maxBy((i, card) => card.value)._1
      ChooseReplace(chosenReplace)
    else
      availableActions match
        case `Draw` :: `DrawKing` :: Nil => DrawKing
        case action :: Nil               => action

  private def canDiscard(actions: List[Action]): Boolean = checkActions(actions)(_ match
    case ChooseDiscard(_) => true
    case _                => false)

  private def canReplace(actions: List[Action]): Boolean = checkActions(actions)(_ match
    case ChooseReplace(_) => true
    case _                => false)

  private def checkActions(action: List[Action])(p: Action => Boolean): Boolean = action.exists(p)

  private def checkKnownDiscard(turn: Turn): Boolean =
    if turn.board.discardPile.nonEmpty then
      knownCards.values.toList.map(_.value).contains(turn.board.getTopDiscardStack.value)
    else false

  private def mapFromHand(hand: List[Card]): Map[Int, Card] =
    hand.zipWithIndex.foldLeft(Map())((map, pair) => map.updated(pair._2, pair._1))

  private def knows(index: Int): Boolean = knownCards.contains(index)
  def getKnownCard(index: Int): Option[Card] = if knows(index) then Some(knownCards(index)) else None

  def play(turn: Turn): (Turn, Action) = getChosenAction(turn.actions, turn) match
    case Observe =>
      knownCards = mapFromHand(turn.act(Observe).hand)
      (turn.act(Observe), Observe)
    case chosenAction => (turn.act(chosenAction), chosenAction)
