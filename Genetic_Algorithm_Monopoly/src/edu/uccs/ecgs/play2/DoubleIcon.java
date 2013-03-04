package edu.uccs.ecgs.play2;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class DoubleIcon extends ImageIcon {

  private Icon icon1;
  private Icon icon2;

  public DoubleIcon(ImageIcon i1, ImageIcon i2) {
    if (i1 != null)
      setImage(i1.getImage());
    icon1 = i1;
    icon2 = i2;
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y)
  {
    if (icon1 != null)
      icon1.paintIcon(c, g, x, y);
    if (icon2 != null)
      icon2.paintIcon(c, g, x, y);
  }

  public void setIcon2(ImageIcon icon)
  {
    icon2 = icon;
  }
}