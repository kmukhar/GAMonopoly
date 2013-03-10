package edu.uccs.ecgs.play2;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class DoubleIcon extends ImageIcon {

  private Icon icon1;
  private Icon icon2;
  private Icon icon3;

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
    if (icon3 != null)
      icon3.paintIcon(c, g, x, y);
  }

  /**
   * @see javax.swing.ImageIcon#getIconWidth()
   */
  @Override
  public int getIconWidth() {
    if (icon2 != null) return icon2.getIconWidth();
    if (icon1 != null) return icon1.getIconWidth();
    return super.getIconWidth();
  }

  /**
   * @see javax.swing.ImageIcon#getIconHeight()
   */
  @Override
  public int getIconHeight() {
    if (icon2 != null) return icon2.getIconHeight();
    if (icon1 != null) return icon1.getIconHeight();
    return super.getIconHeight();
  }

  public void setIcon2(ImageIcon icon)
  {
    icon2 = icon;
  }

  public void setIcon1(ImageIcon icon)
  {
    icon1 = icon;
  }

  public void setIcon3(ImageIcon icon) {
    icon3 = icon;
  }
}