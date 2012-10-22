package edu.uccs.ecgs.play;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JTextField;
import edu.uccs.ecgs.players.AbstractPlayer;

public class CashFieldActionListener implements ActionListener {
  PlayerPanel pp;

  public CashFieldActionListener(PlayerPanel pp) {
    super();
    this.pp = pp;
  }

  @Override
  public void actionPerformed(ActionEvent arg0)
  {
    JTextField source = (JTextField) arg0.getSource();
    AbstractPlayer player = pp.getPlayer();
    player.cash = Integer.parseInt(source.getText());
    source.setText("" + player.cash);
  }
}
