package edu.uccs.ecgs.play2;

import java.io.DataOutputStream;
import java.io.IOException;
import javax.swing.JOptionPane;
import edu.uccs.ecgs.ga.*;
import edu.uccs.ecgs.players.AbstractPlayer;
import edu.uccs.ecgs.players.ChromoTypes;

public class HumanPlayer extends AbstractPlayer {

  public HumanPlayer(int index, ChromoTypes chromoType) {
    super(index, chromoType);
    // TODO Auto-generated constructor stub
  }

  @Override
  public boolean payBailP()
  {
    int result = JOptionPane.showConfirmDialog(null,
        "Do you want to pay bail to get out of Jail?", "Pay Bail?",
        JOptionPane.YES_NO_OPTION);
    return result == JOptionPane.YES_OPTION;
  }

  @Override
  public boolean buyProperty()
  {
    return buyProperty (location);
  }

  @Override
  public boolean buyProperty(Location location)
  {
    int result = JOptionPane.showConfirmDialog(null, "Do you want to buy "
        + location.name + "?", "Buy Property?", JOptionPane.YES_NO_OPTION);
    return result == JOptionPane.YES_OPTION;
  }

  @Override
  public void dumpGenome(DataOutputStream out) throws IOException {}

  @Override
  public void printGenome() {}

  @Override
  public AbstractPlayer[] createChildren(AbstractPlayer parent2, int index)
  {
    return null;
  }

  @Override
  public AbstractPlayer copyAndMutate()
  {
    return null;
  }

  @Override
  protected Object clone() throws CloneNotSupportedException
  {
    return null;
  }
}
