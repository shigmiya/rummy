package rummy.computer;

import java.util.List;

import rummy.core.Card;
import rummy.core.Deck;
import rummy.core.Hand;
import rummy.parts.Part;
import rummy.parts.PartsBuilder;
import rummy.parts.PartsCombiner;
import rummy.parts.PartsCombiner.Solution;
import rummy.parts.PartsScorer;

/**
 * Represents a AI-controller player (eg computer or bot), than uses a back-tracking algorithm
 * to decide whether to draw a card from the stack and which to discard.
 */
public class Computer {

  final PartsScorer scorer;

  Hand hand;
  int currentHandScore = -99999;

  public Computer() {
    scorer = new PartsScorer();
  }

  public void drawNewHand(Deck deck) {
    hand = new Hand();
    for (int i = 0; i < 13; i++) {
      hand.cards.add(deck.draw());
    }
    currentHandScore = computeScore(hand, false).score;
  }

  public boolean shouldPickup(Card card) {
    Hand newHand = new Hand(hand);
    hand.cards.add(card);
    int newScore = computeScore(newHand, true).score;
    return newScore > currentHandScore * 1.15;
  }

  public Card drawAndDiscard(Card card) {
    hand.cards.add(card);
    Solution sol = computeScore(hand, true);
    currentHandScore = sol.score;
    Card freeCard = sol.freeCards.get(0);
    hand.cards.clear();
    for (Part part : sol.parts) {
      hand.cards.addAll(part.cards);
    }
    if (scorer.isWinning(sol.parts)) {
      return null;
    }
    return freeCard;
  }

  public Solution computeScore(Hand hand, boolean extraCard) {
    PartsBuilder partsBuilder = new PartsBuilder();
    List<Part> parts = partsBuilder.buildParts(hand);
    PartsCombiner combiner = new PartsCombiner(parts, extraCard);
    Solution sol = combiner.findBestHand();
    return sol;
  }
}
