package rummy.computer;

import java.util.Set;

import rummy.core.Card;
import rummy.core.Card.Face;
import rummy.core.Deck;
import rummy.core.Hand;
import rummy.parts.Part;
import rummy.parts.PartsSolver;
import rummy.parts.PartsSolver.Solution;
import rummy.scorer.ScoreUtil;
import rummy.scorer.ScorerFactory;
import rummy.tokenizer.AggregateTokenizer;
import rummy.tokenizer.PartsTokenizer;

/**
 * Represents a AI-controller player (eg computer or bot), than uses a back-tracking algorithm
 * to decide whether to draw a card from the stack and which to discard.
 */
public class Computer {

  Hand hand;
  int currentHandScore = -99999;
  int currentPoints = ScoreUtil.FULL_HAND_POINTS;
  final Face faceJoker;
  final ScorerFactory scorerFactory;

  public Computer(Face faceJoker, ScorerFactory scorerFactory) {
    this.faceJoker = faceJoker;
    this.scorerFactory = scorerFactory;
  }

  public void drawNewHand(Deck deck) {
    hand = new Hand();
    for (int i = 0; i < 13; i++) {
      hand.cards.add(deck.draw());
    }

    Solution solution = computeScore(hand, false);
    currentHandScore = solution.score;
    currentPoints = solution.points;
  }

  public static class PickupResult {
    final boolean keepCard;
    final Card freeCard;
    public PickupResult(boolean keepCard, Card freeCard) {
      this.keepCard = keepCard;
      this.freeCard = freeCard;
    }
  }

  public PickupResult checkPickup(Card card) {
    Hand newHand = new Hand(hand);
    newHand.cards.add(card);
    Solution solution = computeScore(newHand, true);
    boolean keepCard = solution.isWinning || solution.score >= currentHandScore * 1.15;

    if (keepCard) {
      return new PickupResult(true, formHand(solution));
    } else {
      return new PickupResult(false, null);
    }
  }

  public Card drawAndDiscard(Card card) {
    hand.cards.add(card);
    Solution solution = computeScore(hand, true);
    return formHand(solution);
  }

  private Card formHand(Solution solution) {
    currentHandScore = solution.score;
    currentPoints = solution.points;
    Card freeCard = solution.freeCards.get(0);
    hand.cards.clear();
    for (Part part : solution.parts) {
      hand.cards.addAll(part.cards);
    }
    if (currentPoints == 0) {
      return null;
    }
    return freeCard;
  }

  public Solution computeScore(Hand hand, boolean extraCard) {
    PartsTokenizer tokenizer = new AggregateTokenizer();
    Set<Part> parts = tokenizer.tokenize(hand, faceJoker);
    PartsSolver solver = new PartsSolver(parts, extraCard, scorerFactory);
    return solver.findBestHand();
  }
}
