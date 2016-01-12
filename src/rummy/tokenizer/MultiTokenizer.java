package rummy.tokenizer;

import java.util.ArrayList;
import java.util.List;

import rummy.core.Hand;
import rummy.parts.Part;

public class MultiTokenizer implements PartsTokenizer {

  private final PartsTokenizer rummyTokenizer;
  private final PartsTokenizer setTokenizer;
  private final PartsTokenizer singlesTokenizer;

  public MultiTokenizer() {
    rummyTokenizer = new RummyTokenizer();
    setTokenizer = new SetTokenizer();
    singlesTokenizer = new SinglesTokenizer();
  }

  @Override
  public List<Part> tokenize(Hand hand) {
    List<Part> parts = new ArrayList<>();
    parts.addAll(rummyTokenizer.tokenize(hand));
    parts.addAll(setTokenizer.tokenize(hand));
    parts.addAll(singlesTokenizer.tokenize(hand));
    return parts;
  }
}
