package edu.uccs.ecgs.players;

import java.io.DataInputStream;
import java.io.IOException;
import edu.uccs.ecgs.play2.HumanPlayer;

public enum ChromoTypes {
  RGA, SGA, TGA;

  public AbstractPlayer getPlayer(int index) {
    switch (this) {
    case RGA: return new RGAPlayer(index);
    case SGA: return new SGAPlayer(index);
    case TGA: return new TGAPlayer(index);
    default: return null;
    }
  }

  public AbstractPlayer getPlayer(int index, DataInputStream dis) throws IOException {
    switch (this) {
    case RGA: return new RGAPlayer(index, dis);
    case SGA: return new SGAPlayer(index, dis);
    case TGA: return new TGAPlayer(index, dis);
    default: return null;
    }
  }
}
