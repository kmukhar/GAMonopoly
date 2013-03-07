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

  private static final int GO_TO_JAIL = 30;
  private static final int PARK_PLACE = 37;
  private static final int BOARDWALK = 39;

  private Location location;
  boolean[] players = new boolean[] { false, false, false, false, false };
  private Hashtable<String, ImageIcon> playerIcons;
  private JDialog dialog;

public LocationButton(Location location) {
    this.location = location;

    playerIcons = new Hashtable<String, ImageIcon>();
    if (isDarkBlue())
      createPlayerIconsWhite();
    else
      createPlayerIcons();

    if (location.index == 0)
      players = new boolean[] { true, true, true, true, true };

    setPreferredSize(new Dimension(75, 75));
    setHorizontalTextPosition(SwingConstants.CENTER);
    setVerticalTextPosition(JButton.CENTER);
    
    String name = location.getHyphenatedName();

    ImageIcon icon = createImageIcon(location);
    if (icon != null) {
      DoubleIcon dicon = new DoubleIcon(icon, null); 
      setMargin(new Insets(0, 0, 0, 0));
      setIcon(dicon);
    } else {
      Color color = location.getGroup().getColor();
      setMargin(new Insets(0, 0, 0, 0));
      setBackground(color);
      if (isDarkBlue()) 
        setForeground(Color.white);
      setText("<html><body>" + name + "</body></html>");
    }

    this.addActionListener(this);
  }

  @Override
  public void actionPerformed(ActionEvent arg0)
  {
    int width = 250;
    if (location.index == 10)
      width=600;

    dialog = new JOptionPane("<html><body width=" + width + ">"
        + location.getFormattedString() + "</body></html>").createDialog(this,
        "");
    dialog.setModal(false);
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
      path = "/edu/uccs/ecgs/icons/monopoly_icon_arrow_col_sm.gif";
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
      path = "/edu/uccs/ecgs/icons/monopoly_icon_train_reading.gif";
      break;
    case 15:
      path = "/edu/uccs/ecgs/icons/monopoly_icon_train_penn.gif";
      break;
    case 25:
      path = "/edu/uccs/ecgs/icons/monopoly_icon_train_bo.gif";
      break;
    case 35:
      path = "/edu/uccs/ecgs/icons/monopoly_icon_train_short.gif";
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
    AbstractPlayer player = (AbstractPlayer) e.getSource();
    if (e instanceof LocationChangeEvent) {
      LocationChangeEvent lce = (LocationChangeEvent) e;
      Location previous = lce.getPreviousLocation();
      Location current = player.getCurrentLocation();
      locationChange(player, previous, current);
    } else if (e instanceof BankruptcyEvent) {
      Location previous = player.getCurrentLocation();
      Location current = null;
      locationChange(player, previous, current);
    }
  }

  /**
   * Update the button icons based on current and previous locations. If this
   * button is the previous location, remove the player icon. If this location
   * is the current location, add the player icon.
   * 
   * @param player
   *          The player that moved
   * @param previous
   *          The player's previous location
   * @param current
   *          The player's current location
   */
  private void locationChange(AbstractPlayer player, Location previous,
                              Location current)
  {
    if (this.location == previous) {
      removePlayer(player);
      updatePlayerIcons();
    }

    if (this.location == current) {
      addPlayer(player);
      updatePlayerIcons();
    }
  }

  /**
   * Update which icon is displayed on the button to show player location
   */
  private void updatePlayerIcons()
  {
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

  /**
   * Set a flag for the player, so the button knows which players are at the
   * location.
   * 
   * @param player
   *          A player at the location represented by this button
   */
  private void addPlayer(AbstractPlayer player)
  {
    // If location is Go To Jail, then players will never stay at that 
    // location, so all flags at that location are alway false
    if (location.index != GO_TO_JAIL)
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

  /**
   * @return The string name for the player icon file to display for this
   *         button.
   */
  private String getStringForPlayers()
  {
    StringBuilder sb = new StringBuilder();
    for (int i = 1; i < 5; i++)
      if (players[i])
        sb.append(i);
    if (isDarkBlue())
      sb.append("w");
    sb.append(".png");
    return sb.toString();
  }

  /**
   * @return True if this location is Park Place or Boardwalk, false otherwise
   */
  private boolean isDarkBlue() {
    return location.index == PARK_PLACE || location .index == BOARDWALK;
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

  private void createPlayerIconsWhite()
  {
    String[] iconNames = new String[] { "1w.png", "12w.png", "123w.png",
        "1234w.png", "124w.png", "13w.png", "134w.png", "14w.png", "2w.png",
        "23w.png", "234w.png", "24w.png", "3w.png", "34w.png", "4w.png" };

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

  public void closeDialog() {
    if (dialog != null) {
      dialog.dispose();
      dialog = null;
    }
  }
}
