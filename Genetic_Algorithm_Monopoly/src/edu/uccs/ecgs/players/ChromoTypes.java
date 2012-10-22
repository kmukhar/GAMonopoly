package edu.uccs.ecgs.players;

import java.io.DataInputStream;
import java.io.IOException;

public enum ChromoTypes {
  RGA, SGA, TGA, HUM;

  public AbstractPlayer getPlayer(int index) {
    switch (this) {
    case RGA: return new RGAPlayer(index);
    case SGA: return new SGAPlayer(index);
    case TGA: return new TGAPlayer(index);
    case HUM: return new HumanPlayer(index);
    default: return null;
    }
  }

  public AbstractPlayer getPlayer(int index, DataInputStream dis) throws IOException {
    switch (this) {
    case RGA: return new RGAPlayer(index, dis);
    case SGA: return new SGAPlayer(index, dis);
    case TGA: return new TGAPlayer(index, dis);
    case HUM: return new HumanPlayer(index, dis);
    default: return null;
    }
  }
}
