package rummy.computer;

import java.util.ArrayList;
import java.util.List;

import rummy.computer.Computer.PickupResult;
import rummy.core.Card;
import rummy.core.Deck;

/**
 * Have a set of computers play a game of rummy against each other.
 */
public class ComputerSimulationMain {

  public static void main(String args[]) {
    int numComputers = 4;
    int numDecks = 3;
    int numJokers = 6;

    Deck deck = new Deck(numDecks, numJokers);
    deck.shuffle();

    List<Computer> computers = new ArrayList<>(numComputers);
    for (int i = 0; i < numComputers; i++) {
      Computer computer = new Computer();
      computer.drawNewHand(deck);
      computers.add(computer);
    }

    Card top = deck.draw();
    int turn = 1;
    while (true) {
      for (int i = 0; i < computers.size(); i++) {
        Computer computer = computers.get(i);
        System.out.println("T" + turn++ + " Computer: " + (i + 1) + ": " + computer.hand + " top:" + top);

        PickupResult pickupResult = computer.checkPickup(top);
        if (pickupResult.keepCard) {
          top = pickupResult.freeCard;
          System.out.println("drew top, discared " + top);
        } else {
          Card deckCard = deck.draw();
          top = computer.drawAndDiscard(deckCard);
          System.out.println("drew from deck " + deckCard + ", discarded " + top);
        }

        if (top == null) {
          System.out.println("winner!");
          break;
        }
      }
      if (top == null) {
        break;
      }
    }
  }
}