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
    
    System.setProperty("BROWN", "0x8b4513");
    System.setProperty("LIGHT_BLUE", "0x00ced1");
    System.setProperty("PURPLE", "0xa020f0");
    System.setProperty("ORANGE", "0xffa500");
    System.setProperty("RED", "0xff0000");
    System.setProperty("YELLOW", "0xffff00");
    System.setProperty("GREEN", "0x00FF00");
    System.setProperty("DARK_BLUE", "0x0000ff");
    
    ImageIcon icon = createImageIcon(location);
    if (icon != null) {
      setMargin(new Insets(1, 10, 1, 1));
      this.setIcon(icon);
    } else {
      String groupName = location.getGroup().name();
      Color color = Color.getColor(groupName);
      this.setBackground(color);
    }

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
      path=null;      
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
}
