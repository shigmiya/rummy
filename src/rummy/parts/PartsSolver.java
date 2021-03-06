package rummy.parts;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import rummy.core.Card;
import rummy.core.Card.Face;
import rummy.scorer.ScoreUtil;
import rummy.scorer.ScorerFactory;

/**
 * Combines a list of part tokens (eg rummys, sets, partial rummys, single cards, etc) into a hand
 * that optimizes a score function.  Goes through each possible combination of parts that forms a
 * valid hand via backtracking. Applies various pruning mechanisms to lower the search space, at
 * the cost of missing some (hopefully rare) optimal hand configurations.
 */
public class PartsSolver {

  private static final int DEFAULT_HAND_SIZE = 13;

  // See description of #initializeBitMaps() to see how these are used.
  private final BiMap<Integer, Part> bitIdxToPart;
  private final Map<Part, BitSet> partToBitSet;

  private List<Part> parts;
  private final int handSize;
  private final boolean extraCard;
  private final ScorerFactory scorerFactory;

  // Helper variable allocated once rather than in the recursive search method, to prevent GC
  // overhead.
  private BitSet usedPartSet;
  int searchIterations = 0;

  private static final Comparator<Part> PARTS_BY_ORDINAL = new Comparator<Part>() {
    @Override
    public int compare(Part p1, Part p2) {
      // First enums to last enums
      return p1.type.ordinal() - p2.type.ordinal();
    }
  };

  // Allows for a different handSize just for testing purposes
  PartsSolver(int handSize, Set<Part> parts, boolean extraCard, ScorerFactory scorerFactory) {
    this.parts = new ArrayList<>();
    this.parts.addAll(parts);
    this.handSize = handSize;
    this.extraCard = extraCard;
    this.scorerFactory = scorerFactory;
    this.bitIdxToPart = HashBiMap.create();
    this.partToBitSet = new HashMap<>();

    preparePartsForSearch();
    initializeBitMaps();
  }

  public PartsSolver(Set<Part> parts, boolean extraCard, ScorerFactory scorerFactory) {
    this(DEFAULT_HAND_SIZE, parts, extraCard, scorerFactory);
  }

  private void preparePartsForSearch() {
    this.parts.sort(PARTS_BY_ORDINAL);
    this.parts = pruneParts(parts);
  }

  // Create the maps which will be used in the backtracking algorithm.
  //
  // Eg, suppose we have parts NatRummy[3H-4H-5H], Set[3H-3S-3C], Rummy[5H-jk-7H], Single[3H],
  // Single[4H], Single[5H].  The follow map associations will be created:
  // a.) BitIdxToPart contains a map of a bitIdx to a part (and partToBitIdx the reverse).
  //   1 <-> NatRummy[3H-4H-5H]
  //   2 <-> Set[3H-3S-3C]
  //   3 <-> Rummy[5H-jk-7H]
  //   4 <-> Single[3H]
  //   5 <-> Single[4H]
  //   6 <-> Single[5H]
  // b.) PartToBitSet contains for each part, the BitIdxs of all parts containing the same cards.
  //   NatRummy[3H-4H-5H] -> [1,2,3,4,5,6]
  //   Set[3H-3S-3C] -> [1,2,4]
  //   Rummy[5H-jk-7H] -> [1,3,6]
  //   Single[3H] -> [1,2,4]
  //   Single[4H] -> [1,5]
  //   Single[5H] -> [1,3,6]
  private void initializeBitMaps() {
    // Register a BitIndex for each part
    int nextBitIdx = 1;
    for (Part part : parts) {
      bitIdxToPart.put(nextBitIdx, part);
      nextBitIdx += 1;
    }

    // Create a helper BitSet for each card, indicating which parts it is used in.
    Map<Card, BitSet> cardToBitSet = new HashMap<>();
    for (Part part : parts) {
      for (Card card : part.cards) {
        if (cardToBitSet.get(card) == null) {
          cardToBitSet.put(card, new BitSet());
        }
        BitSet cardBitSet = cardToBitSet.get(card);
        int bitIdx = bitIdxToPart.inverse().get(part);
        cardBitSet.set(bitIdx);
      }
    }

    // Create a BitSet for each part, indicated which parts its cards are used in.
    for (Part part : parts) {
      BitSet bitSet = new BitSet();
      for (Card card : part.cards) {
        BitSet cardBitSet = cardToBitSet.get(card);
        bitSet.or(cardBitSet);
      }
      partToBitSet.put(part, bitSet);
    }
  }

  /**
   * Shortens a list of parts, in order to lower the computation time for finding optimal part
   * configurations. This will make it impossible to find the optimal hand in certain (hopefully
   * rare) cases. It will remove parts that belong to some 3-card rummys (eg, those cards are stuck
   * to the rummy, can't consider any other usages).
   */
  static List<Part> pruneParts(List<Part> parts) {
    List<Part> pruned = new ArrayList<>();
    Set<Card> blackListed = new HashSet<>();
    for (Part part : parts) {
      boolean skip = false;
      if (part.type == PartType.NATURAL_RUMMY) {
        if (part.cards.size() == 3
            && blackListed.size() <= 3
            && part.cards.get(0).face != Face.ACE) {
          blackListed.addAll(part.cards);
        }
      } else if (part.type != PartType.RUMMY) {
        for (Card card : part.cards) {
          if (blackListed.contains(card)) {
            skip = true;
          }
        }
      }

      if (!skip) {
        pruned.add(part);
      }
    }
    return pruned;
  }

  public Solution findBestHand() {
    // Add all parts to set
    BitSet availableParts = new BitSet();
    Set<Card> allCards = new HashSet<>();
    for (Part part : parts) {
      availableParts.set(bitIdxToPart.inverse().get(part));
      allCards.addAll(part.cards);
    }

    // Find which parts to use that optimizes the score
    Solution best = new Solution();
    search(
        availableParts,
        0 /* startBitIdx */,
        new LinkedHashSet<>() /* running parts */,
        allCards,
        new LinkedHashSet<>() /* used cards */,
        best);
    return best;
  }

  private void search(
      BitSet availableParts,
      int startIdx,
      Set<Part> runningParts,
      Set<Card> availableCards,
      Set<Card> usedCards,
      Solution best) {
    if (best.isWinning) {
      // Found a winning solution, end the search.
      return;
    }

    searchIterations++;
    if (usedCards.size() == handSize && availableCards.size() == (extraCard ? 1 : 0)) {
      // Found a solution, record it if its the best one so far
      // System.out.println(runningParts);
      int score = computeScore(runningParts);
      int points = ScoreUtil.calculatePoints(runningParts);
      if (points <= best.points && score > best.score) {
      //if (points <= best.points) {
        best.parts = new ArrayList<Part>(runningParts);
        best.score = score;
        best.points = points;
        best.isWinning = best.points == 0;
        best.freeCards = new ArrayList<Card>(availableCards);
      }
      return;
    }

    if (availableCards.size() + usedCards.size() < handSize || usedCards.size() >= handSize) {
      // No possible solution in this path
      return;
    }

    BitSet original = new BitSet();
    original.or(availableParts);
    Part nextPart;
    for (int bitIdx = availableParts.nextSetBit(startIdx);
        bitIdx >= 0;
        bitIdx = availableParts.nextSetBit(bitIdx+1)) {
      // Get the next available part to use for forming a hand
      nextPart = bitIdxToPart.get(bitIdx);

      // No use in continuing if first/second part is a single, there must be a better hand
      // previously.
      if (nextPart.type == PartType.SINGLE && runningParts.size() <= 1 && parts.size() > 25) {
        return;
      }

      // Use this part.
      runningParts.add(nextPart);
      usedCards.addAll(nextPart.cards);

      // Mark which other parts are no longer available for use, as their cards will overlap with
      // the newly used part.
      availableCards.removeAll(nextPart.cards);
      usedPartSet = partToBitSet.get(nextPart);
      availableParts.andNot(usedPartSet);

      // Recursively search through remaining cards to form a hand
      search(availableParts, bitIdx + 1, runningParts, availableCards, usedCards, best);

      // Restore hand to original state as if the part was not used.
      availableParts.or(original);
      runningParts.remove(nextPart);
      usedCards.removeAll(nextPart.cards);
      availableCards.addAll(nextPart.cards);
    }
  }

  private int computeScore(Set<Part> parts) {
    return scorerFactory.get().scoreParts(parts);
  }

  /**
   * Holder for information about a particular set of parts that formed a hand.
   */
  public static class Solution {
    public List<Part> parts = null;
    public int score = -999999;
    public int points = ScoreUtil.FULL_HAND_POINTS;
    public List<Card> freeCards = null;
    public boolean isWinning = false;
  }
}
