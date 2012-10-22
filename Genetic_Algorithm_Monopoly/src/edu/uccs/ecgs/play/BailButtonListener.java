package edu.uccs.ecgs.play;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import edu.uccs.ecgs.players.AbstractPlayer;

public class BailButtonListener implements ActionListener {
  PlayerPanel pp;
  
  public BailButtonListener(PlayerPanel pp) {
    super();
    this.pp = pp;
  }

  @Override
  public void actionPerformed(ActionEvent actionevent) {
    AbstractPlayer player = pp.getPlayer();
    if (player == null) {
      JOptionPane.showMessageDialog(null, "No Active Player!",
          "Pay Bail Decision", JOptionPane.WARNING_MESSAGE);
      return;
    }
    if (player.payBailP()) {
      JOptionPane.showMessageDialog(null, "Player decides to pay bail",
          "Pay Bail Decision", JOptionPane.INFORMATION_MESSAGE);
    } else {
      JOptionPane.showMessageDialog(null, "Player decides to NOT pay bail",
          "Pay Bail Decision", JOptionPane.INFORMATION_MESSAGE);
    }
  }

}
