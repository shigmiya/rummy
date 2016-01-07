package rummy.parts;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import rummy.core.Card;
import rummy.core.Hand;
import rummy.core.Card.Face;
import rummy.core.Card.Suit;
import rummy.parts.Part;
import rummy.parts.PartsBuilder;
import rummy.parts.PartsCombiner;
import rummy.parts.PartsCombiner.Solution;

public class PartsBuilderTest {

  @Test
  @Ignore
  public void testFullHand() {
    Hand hand = new Hand(
      Card.build(Face.TWO, Suit.HEARTS),
      Card.build(Face.THREE, Suit.HEARTS),
      Card.build(Face.FOUR, Suit.HEARTS),
      Card.build(Face.FIVE, Suit.HEARTS),
      Card.build(Face.SEVEN, Suit.HEARTS),
      Card.build(Face.SEVEN, Suit.SPADES),
      Card.build(Face.SEVEN, Suit.CLUBS),
      Card.build(Face.NINE, Suit.HEARTS),
      Card.build(Face.TEN, Suit.HEARTS),
      Card.build(Face.JACK, Suit.HEARTS),
      Card.build(Face.KING, Suit.HEARTS),
      Card.build(Face.KING, Suit.SPADES),
      Card.build(Face.KING, Suit.CLUBS),
      Card.build(Face.FOUR, Suit.SPADES)
    );

    List<Part> parts = new PartsBuilder().buildParts(hand);
    System.out.println("numParts:" + parts.size());
    assertTrue(parts.size() > 0);

    PartsCombiner combiner = new PartsCombiner(13, parts);
    Solution solution = combiner.combineParts();
    assertNotNull(solution.parts);
    System.out.println("solution");
    System.out.println(solution.parts);
    System.out.println(combiner.count);
  }

  @Test
  @Ignore
  public void testQkaRun() {
    Hand hand = new Hand(
      Card.build(Face.QUEEN, Suit.HEARTS),
      Card.build(Face.THREE, Suit.HEARTS),
      Card.build(Face.KING, Suit.HEARTS),
      Card.build(Face.SEVEN, Suit.HEARTS),
      Card.build(Face.ACE, Suit.HEARTS),
      Card.build(Face.TWO, Suit.HEARTS),
      Card.build(Face.TWO, Suit.SPADES)
    );

    List<Part> parts = new PartsBuilder().buildParts(hand);
    System.out.println("qka parts:" + parts.toString());
  }

  @Test
  @Ignore
  public void testSparseHand() {
    Hand hand = new Hand(
      Card.build(Face.TWO, Suit.HEARTS),
      Card.build(Face.THREE, Suit.HEARTS),
      Card.build(Face.FOUR, Suit.HEARTS),
      Card.build(Face.FOUR, Suit.SPADES),
      Card.build(Face.FIVE, Suit.HEARTS),
      Card.build(Face.FIVE, Suit.SPADES),
      Card.build(Face.SIX, Suit.HEARTS)
    );

    List<Part> parts = new PartsBuilder().buildParts(hand);
    System.out.println("numParts:" + parts.size());
    assertTrue(parts.size() > 0);

    Solution solution = new PartsCombiner(7, parts).combineParts();
    //assertNotNull(solution.parts);
    System.out.println("solution");
    System.out.println(solution.parts);
  }

  @Test
  public void testOptimalMidHand() {
    Hand hand = toHand("9H 9S 3H 3D 4D 5D 9D 9C AH AS AD 4H 5H");
    List<Part> parts = new PartsBuilder().buildParts(hand);
    Solution solution = new PartsCombiner(13, parts).combineParts();
    assertNotNull(solution.parts);
    System.out.println("solution:" + solution.parts);    
  }

  static Hand toHand(String in) {
    Hand hand = new Hand();
    for (String val : in.split(" ")) {
      char suitChar = val.charAt(val.length() - 1);
      String faceStr = val.substring(0, val.length() - 1);

      Suit suit;
      switch (suitChar) {
        case 'H': suit = Suit.HEARTS; break;
        case 'S': suit = Suit.SPADES; break;
        case 'D': suit = Suit.DIAMONDS; break;
        case 'C': suit = Suit.CLUBS; break;
        default: throw new IllegalArgumentException("bad hand string");
      }

      Face face;
      switch (faceStr) {
        case "A" : face = Face.ACE; break;
        case "2" : face = Face.TWO; break;
        case "3" : face = Face.THREE; break;
        case "4" : face = Face.FOUR; break;
        case "5" : face = Face.FIVE; break;
        case "6" : face = Face.SIX; break;
        case "7" : face = Face.SEVEN; break;
        case "8" : face = Face.EIGHT; break;
        case "9" : face = Face.NINE; break;
        case "10" : face = Face.TEN; break;
        case "J" : face = Face.JACK; break;
        case "Q" : face = Face.QUEEN; break;
        case "K" : face = Face.KING; break;
        default: throw new IllegalArgumentException("bad hand string");
      }

      hand.cards.add(new Card(face, suit));
    }
    return hand;
  }
}