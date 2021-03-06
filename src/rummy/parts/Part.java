package rummy.parts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import rummy.core.Card;
import rummy.core.Card.Face;

/**
 * A part (or token) represents a unit that a hand breaks down to. For instance, a 3H in a hand
 * could fall into a rummy parts 3H-4H-5H, and/or a partial set 3H-3C.
 */
public class Part {

  public final PartType type;
  public final List<Card> cards;
  public boolean containsAce;

  Part(PartType type, List<Card> cards) {
    this.type = type;
    this.cards = cards;
    this.containsAce = hasAce(cards);
  }

  public String toString() {
    String result = "";
    result += type.name() + ": " + cards.toString();
    return result;
  }

  public static Part naturalRummy(List<Card> cards) {
    ArrayList<Card> cardsCopy = new ArrayList<>(cards);
    return new Part(PartType.NATURAL_RUMMY, cardsCopy);
  }

  public static Part rummyWithJoker(List<Card> cards, Card... jokers) {
    ArrayList<Card> cardsCopy = new ArrayList<>(cards);
    cardsCopy.addAll(Arrays.asList(jokers));
    return new Part(PartType.RUMMY, cardsCopy);
  }

  public static Part partialRummy(List<Card> cards) {
    ArrayList<Card> cardsCopy = new ArrayList<>(cards);
    return new Part(PartType.PARTIAL_RUMMY, cardsCopy);
  }

  public static Part set(List<Card> cards) {
    ArrayList<Card> cardsCopy = new ArrayList<>(cards);
    return new Part(PartType.SET, cardsCopy);
  }

  public static Part setWithJoker(List<Card> cards, Card... jokers) {
    ArrayList<Card> cardsCopy = new ArrayList<>(cards);
    cardsCopy.addAll(Arrays.asList(jokers));
    return new Part(PartType.SET, cardsCopy);
  }

  public static Part partialSet(List<Card> cards) {
    ArrayList<Card> cardsCopy = new ArrayList<>(cards);
    return new Part(PartType.PARTIAL_SET, cardsCopy);
  }

  public static Part single(Card card) {
    return new Part(PartType.SINGLE, Arrays.asList(card));
  }

  private static boolean hasAce(List<Card> cards) {
    for (Card card : cards) {
      if (card.face == Face.ACE) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Part)) {
      return false;
    }
    Part other = (Part) o;
    return type == other.type && Objects.equals(cards, other.cards);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, cards);
  }
}
