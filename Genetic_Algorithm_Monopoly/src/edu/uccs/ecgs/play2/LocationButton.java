package edu.uccs.ecgs.play2;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import edu.uccs.ecgs.ga.Location;
import edu.uccs.ecgs.ga.PropertyGroups;

@SuppressWarnings("serial")
public class LocationButton extends JButton implements ActionListener {

  private Location location;

  public LocationButton(Location location) {
    super(location.name);
    this.location = location;
    this.setPreferredSize(new Dimension(60, 60));

    ImageIcon icon = createImageIcon(location);
    this.setIcon(icon);

    this.addActionListener(this);
  }

  @Override
  public void actionPerformed(ActionEvent arg0)
  {
    JTextPane msgpane = new JTextPane();
    msgpane.setText(location.getFormattedString());
    JOptionPane op = new JOptionPane();
    msgpane.setBackground(op.getBackground());
    JOptionPane.showMessageDialog(null, msgpane);
  }

  /** Returns an ImageIcon, or null if the path was invalid. */
  protected static ImageIcon createImageIcon(Location location)
  {
    PropertyGroups group = location.getGroup();
    String path = "";
    switch (location.index) {
    case 0:
      path = "../icons/monopoly_icon_arrow_2_bw_sm.gif";
      break;
    case 2:
    case 17:
    case 33:
      path = "../icons/monopoly_icon_chest_col_sm.gif";
      break;
    case 4:
      path = "../icons/monopoly_icon_tax_bw_sm.gif"; // income tax
      break;
    case 5:
    case 15:
    case 25:
    case 35:
      path = "../icons/monopoly_icon_train_bw_sm.gif";
      break;
    case 7:
      path = "../icons/monopoly_icon_chance_3_col_sm.gif";
      break;
    case 10:
      path = "../icons/monopoly_icon_jail_sm.gif";
      break;
    case 12:
      path = "../icons/monopoly_icon_electric_col_sm.gif";
      break;
    case 20:
      path = "../icons/monopoly_icon_parking_col_sm.gif";
      break;
    case 22:
      path = "../icons/monopoly_icon_chance_1_col_sm.gif";
      break;
    case 28:
      path = "../icons/monopoly_icon_water_bw_sm.gif";
      break;
    case 30:
      path = "../icons/monopoly_icon_go_jail_bw_sm.gif";
      break;
    case 36:
      path = "../icons/monopoly_icon_chance_2_col_sm.gif";
      break;
    case 38:
      path = "../icons/monopoly_icon_tax_col_sm.gif";
      break;
    default:
      path = group.name();
      path += ".GIF";
      break;
    }

    java.net.URL imgURL = LocationButton.class.getResource(path);

    if (imgURL != null) {
      ImageIcon icon = new ImageIcon(imgURL);
      return icon;
    } else {
      System.err.println("Couldn't find file: " + path);
      return null;
    }
  }
}
