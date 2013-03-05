package edu.uccs.ecgs.play2;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import edu.uccs.ecgs.ga.Location;
import edu.uccs.ecgs.players.AbstractPlayer;

@SuppressWarnings("serial")
public class LocationButton extends JButton implements ActionListener,
    ChangeListener 
{

  private Location location;
  boolean[] players = new boolean[] { false, false, false, false, false };
  private static Hashtable<String, ImageIcon> playerIcons;

public LocationButton(Location location) {
    this.location = location;

    playerIcons = new Hashtable<String, ImageIcon>();
    createPlayerIcons();

    if (location.index == 0)
      players = new boolean[] { true, true, true, true, true };

    setPreferredSize(new Dimension(75, 75));
    setHorizontalTextPosition(JButton.CENTER);
    setVerticalTextPosition(JButton.CENTER);
    
    ImageIcon icon = createImageIcon(location);
    if (icon != null) {
      DoubleIcon dicon = new DoubleIcon(icon, null); 
      setMargin(new Insets(1, 1, 1, 1));
      setIcon(dicon);
    } else {
      Color color = location.getGroup().getColor();
      setMargin(new Insets(1, 1, 1, 1));
      this.setBackground(color);
      this.setText("<html><body><center>" + location.name
          + "</center></body></html>");
    }

    this.addActionListener(this);
  }

  @Override
  public void actionPerformed(ActionEvent arg0)
  {
    JTextPane msgpane = new JTextPane();
    msgpane.setText(location.getFormattedString());
    JDialog dialog = new JOptionPane(msgpane).createDialog("");
    msgpane.setBackground(dialog.getRootPane().getBackground());
    dialog.setTitle(location.getFormattedTitle());
    dialog.setVisible(true);
  }

  /**
   * Returns an ImageIcon for a railroad, utility, or special location; returns
   * null if the location is a street.
   */
  protected static ImageIcon createImageIcon(Location location)
  {
    String path = "";
    switch (location.index) {
    case 0:
      path = "/edu/uccs/ecgs/icons/monopoly_icon_arrow_2_bw_sm.gif";
      break;
    case 2:
    case 17:
    case 33:
      path = "/edu/uccs/ecgs/icons/monopoly_icon_chest_col_sm.gif";
      break;
    case 4:
      path = "/edu/uccs/ecgs/icons/monopoly_icon_tax_bw_sm.gif"; // income tax
      break;
    case 5:
    case 15:
    case 25:
    case 35:
      path = "/edu/uccs/ecgs/icons/monopoly_icon_train_bw_sm.gif";
      break;
    case 7:
      path = "/edu/uccs/ecgs/icons/monopoly_icon_chance_3_col_sm.gif";
      break;
    case 10:
      path = "/edu/uccs/ecgs/icons/monopoly_icon_jail_sm.gif";
      break;
    case 12:
      path = "/edu/uccs/ecgs/icons/monopoly_icon_electric_col_sm.gif";
      break;
    case 20:
      path = "/edu/uccs/ecgs/icons/monopoly_icon_parking_col_sm.gif";
      break;
    case 22:
      path = "/edu/uccs/ecgs/icons/monopoly_icon_chance_1_col_sm.gif";
      break;
    case 28:
      path = "/edu/uccs/ecgs/icons/monopoly_icon_water_bw_sm.gif";
      break;
    case 30:
      path = "/edu/uccs/ecgs/icons/monopoly_icon_go_jail_col_sm.gif";
      break;
    case 36:
      path = "/edu/uccs/ecgs/icons/monopoly_icon_chance_2_col_sm.gif";
      break;
    case 38:
      path = "/edu/uccs/ecgs/icons/monopoly_icon_tax_col_sm.gif";
      break;
    default:
      path = null;
      break;
    }

    if (path != null) {
      java.net.URL imgURL = LocationButton.class.getResource(path);

      if (imgURL != null) {
        ImageIcon icon = new ImageIcon(imgURL);
        return icon;
      } else {
        System.err.println("Couldn't find file: " + path);
        return null;
      }
    } else {
      return null;
    }
  }

  @Override
  public void stateChanged(ChangeEvent e)
  {
    if (e instanceof LocationChangedEvent) {
      AbstractPlayer player = (AbstractPlayer) e.getSource();
      Location previous = ((LocationChangedEvent) e).getPreviousLocation();
      if (this.location == previous)
        removePlayer(player);

      Location current = player.getCurrentLocation();
      if (this.location == current)
        addPlayer(player);

      String key = getStringForPlayers();
      DoubleIcon icon = (DoubleIcon) getIcon();
      if (icon == null) 
        icon = new DoubleIcon(null, null);

      if ("".equals(key)) {
        icon.setIcon2(null);
        setIcon(icon);
      } else {
        icon.setIcon2(playerIcons.get(key)); 
        setIcon(icon);
      }
      this.fireStateChanged();
    }
  }

  /**
   * Set a flag for the player, so the button knows which players are at the
   * location
   * 
   * @param player
   *          A player at the location represented by this button
   */
  private void addPlayer(AbstractPlayer player)
  {
    players[player.playerIndex] = true;
  }

  /**
   * Clear the flag for the player, so the button knows which players are at the
   * location
   * 
   * @param player
   *          A player who was previously at the location represented by this
   *          button, but who has now left
   */
  private void removePlayer(AbstractPlayer player)
  {
    players[player.playerIndex] = false;
  }

  private String getStringForPlayers()
  {
    StringBuilder sb = new StringBuilder();
    for (int i = 1; i < 5; i++)
      if (players[i])
        sb.append(i);
    sb.append(".png");
    return sb.toString();
  }

  private void createPlayerIcons()
  {
    String[] iconNames = new String[] { "1.png", "12.png", "123.png",
        "1234.png", "124.png", "13.png", "134.png", "14.png", "2.png",
        "23.png", "234.png", "24.png", "3.png", "34.png", "4.png" };

    for (String name : iconNames) {
      java.net.URL imgURL = LocationButton.class.getResource(name);

      if (imgURL != null) {
        ImageIcon icon = new ImageIcon(imgURL);
        playerIcons.put(name, icon);
      } else {
        System.err.println("Couldn't find file: " + name);
      }
    }
  }
}
