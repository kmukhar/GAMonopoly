package edu.uccs.ecgs.play;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import edu.uccs.ecgs.players.AbstractPlayer;
import edu.uccs.ecgs.players.RGAPlayer;

public class IdFieldActionListener implements ActionListener {
  PlayerPanel pp;

  public IdFieldActionListener(PlayerPanel pp) {
    super();
    this.pp = pp;
  }

  @Override
  public void actionPerformed(ActionEvent arg0)
  {
    AbstractPlayer player = pp.getPlayer();

    if (player == null) {
      player = new RGAPlayer(0);
      pp.cashField.setText("" + player.cash);
      pp.cashField.setEditable(true);
      pp.cashField.addFocusListener(new CashFieldFocusListener(pp));
      pp.cashField.addActionListener(new CashFieldActionListener(pp));
      pp.disableLoadButton();
    }
  }
}
