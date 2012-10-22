package edu.uccs.ecgs.play;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import edu.uccs.ecgs.ga.*;
import edu.uccs.ecgs.players.AbstractPlayer;

public class BuyButtonListener implements ActionListener {
  JComboBox<Location> list;
  PlayerPanel pp;
  
  public BuyButtonListener(PlayerPanel pp, JComboBox<Location> list) {
    super();
    this.pp = pp;
    this.list = list;
  }

  @Override
  public void actionPerformed(ActionEvent actionevent) {
    AbstractPlayer player = pp.getPlayer();
    Location lot = (Location) list.getSelectedItem();
    if (lot.getGroup() != PropertyGroups.SPECIAL) {
      if (player.buyProperty(lot)) {
        JOptionPane.showMessageDialog(null, "Player decides to buy property",
            "Buy Property Decision", JOptionPane.INFORMATION_MESSAGE);
      } else {
        JOptionPane.showMessageDialog(null, "Player decides to NOT buy property",
            "Buy Property Decision", JOptionPane.WARNING_MESSAGE);
      }
    }
  }

}
