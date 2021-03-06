package edu.uccs.ecgs.players;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import edu.uccs.ecgs.ga.Edges;
import edu.uccs.ecgs.ga.PropertyFactory;

//player that uses a genome with continuous values in the chromosome
//same as RGA but jail chromosome is only 4x4 (RGA is 64x64)
public class TGAPlayer extends CGPlayer {

  // Jail Chromosome dimension
  private static final int JAIL_LENGTH = 4;

  public TGAPlayer(int index) {
    super(index, ChromoTypes.TGA);
    jailLength = JAIL_LENGTH;

    chrNoOwners = new double[lotLength];
    chrPlayerOwns = new double[lotLength];
    chrOpponentOwns = new double[lotLength];
    chrTwoOpponentOwns = new double[lotLength];
    chrJail = new double[jailLength][jailLength];

    for (int i = 0; i < chrNoOwners.length; i++) {
      chrNoOwners[i] = r.nextDouble();
      chrPlayerOwns[i] = r.nextDouble();
      chrOpponentOwns[i] = r.nextDouble();
      chrTwoOpponentOwns[i] = r.nextDouble();
    }

    for (int i = 0; i < jailLength; i++) {
      for (int j = 0; j < jailLength; j++) {
        chrJail[i][j] = r.nextDouble();
      }
    }
  }

  public TGAPlayer(int index, DataInputStream dis) throws IOException {
    super(index, ChromoTypes.TGA);
    jailLength = JAIL_LENGTH;

    chrNoOwners = new double[lotLength];
    chrPlayerOwns = new double[lotLength];
    chrOpponentOwns = new double[lotLength];
    chrTwoOpponentOwns = new double[lotLength];
    chrJail = new double[jailLength][jailLength];

    addToFitness(dis.readInt());

    for (int i = 0; i < chrNoOwners.length; i++) {
      chrNoOwners[i] = dis.readDouble();
    }
    for (int i = 0; i < chrPlayerOwns.length; i++) {
      chrPlayerOwns[i] = dis.readDouble();
    }
    for (int i = 0; i < chrOpponentOwns.length; i++) {
      chrOpponentOwns[i] = dis.readDouble();
    }
    for (int i = 0; i < chrTwoOpponentOwns.length; i++) {
      chrTwoOpponentOwns[i] = dis.readDouble();
    }

    for (int i = 0; i < jailLength; i++) {
      for (int j = 0; j < jailLength; j++) {
        chrJail[i][j] = dis.readDouble();
      }
    }    
  }

  public TGAPlayer(int index, double[] chrNoOwners, double[] chrPlayerOwns,
      double[] chrOpponentOwns, double[] chrTwoOpponentOwns,
      double[][] chrJail) 
  {
    super(index, ChromoTypes.TGA);
    jailLength = JAIL_LENGTH;

    this.chrNoOwners = chrNoOwners;
    this.chrPlayerOwns = chrPlayerOwns;
    this.chrOpponentOwns = chrOpponentOwns;
    this.chrTwoOpponentOwns = chrTwoOpponentOwns;
    this.chrJail = chrJail;
  }

  // Predicate asking whether or not player wishes to pay bail
  // True --> player wishes to pay bail
  // False --> player wishes to attempt to roll doubles
  @Override
  public boolean payBailP() {
    if (!hasAtLeastCash(50) && !hasGetOutOfJailCard()) {
      return false;
    }

    PropertyFactory pf = PropertyFactory.getPropertyFactory(gameKey);
    int idx1 = pf.getIndexFromMonopolies(this, Edges.WEST);
    int idx2 = pf.getIndexFromMonopolies(this, Edges.NORTH);

    return r.nextDouble() < chrJail[idx1][idx2];
  }

  @Override
  public void dumpGenome(DataOutputStream out) throws IOException {
    out.writeChars("TGA");
    super.dumpGenome(out);
  }
}
