package edu.uccs.ecgs.play2;

import java.awt.GridLayout;
import java.awt.LayoutManager;

import javax.swing.JPanel;
import javax.swing.JTextArea;

import edu.uccs.ecgs.players.AbstractPlayer;

public class PlayerPanel extends JPanel {

  private JTextArea filler;
  private AbstractPlayer player;
  
  public PlayerPanel() {
    new JPanel(false);
    filler = new JTextArea();
    filler.setEditable(false);
    setLayout(new GridLayout(1, 1));
    add(filler);
  }
  
  public void setPlayer(AbstractPlayer player) {
    this.player = player;
  }
  
  public void updatePlayerStatus() {
    filler.setText(player.toString());
  }
}
