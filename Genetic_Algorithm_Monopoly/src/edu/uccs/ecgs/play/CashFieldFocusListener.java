package edu.uccs.ecgs.play;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JTextField;
import edu.uccs.ecgs.ga.AbstractPlayer;

public class CashFieldFocusListener implements FocusListener {
  PlayerPanel pp;

  public CashFieldFocusListener(PlayerPanel pp) {
    super();
    this.pp = pp;
  }

  @Override
  public void focusGained(FocusEvent arg0)
  {
    pp.cashField.selectAll();
  }

  @Override
  public void focusLost(FocusEvent arg0)
  {
    JTextField source = (JTextField) arg0.getSource();
    AbstractPlayer player = pp.getPlayer();

    player.cash = Integer.parseInt(source.getText());
    source.setText("" + player.cash);
    // System.out.println(""+pp.player.cash);
  }

}
