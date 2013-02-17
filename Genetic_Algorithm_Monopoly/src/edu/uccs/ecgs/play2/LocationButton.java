package edu.uccs.ecgs.play2;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import edu.uccs.ecgs.ga.Location;

@SuppressWarnings("serial")
public class LocationButton extends JButton implements ActionListener {

  private Location location;

  public LocationButton(Location location) {    
    this.location = location;
    this.setPreferredSize(new Dimension(60, 60));

    ImageIcon icon = createImageIcon(location);
    if (icon != null) {
      this.setIcon(icon);
    } else {
      Color color = location.getGroup().getColor();
      setMargin(new Insets(1, -8, 1, 1));
      this.setBackground(color);
      this.setText("<html><body><center><p>"+location.name+"</p></center></body></html>");
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
