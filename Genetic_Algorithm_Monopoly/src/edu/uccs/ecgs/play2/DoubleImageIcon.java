package edu.uccs.ecgs.play2;

import java.awt.Component;
import java.awt.Graphics;
import java.net.URL;
import javax.swing.ImageIcon;

public class DoubleImageIcon extends ImageIcon {
  private static final int ICONSPACING = 4;

  private ImageIcon i1;
  private ImageIcon i2;

  /**
   * @param arg0
   */
  public DoubleImageIcon(URL arg0) {
    i1 = new ImageIcon(arg0);
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y)
  {
    if (i1 != null)
      i1.paintIcon(c, g, x, y + (getIconHeight() - i1.getIconHeight()) / 2);
    if (i2 != null)
      i2.paintIcon(c, g, x + ICONSPACING + i1.getIconWidth(), y
          + (getIconHeight() - i2.getIconHeight()) / 2);
  }

  @Override
  public int getIconWidth()
  {
    int i1Width = i1 != null ? i1.getIconWidth() : 0;
    int i2Width = i2 != null ? i2.getIconWidth() : 0;
    return i1Width + ICONSPACING + i2Width;
  }

  @Override
  public int getIconHeight()
  {
    int i1Height = i1 != null ? i1.getIconHeight() : 0;
    int i2Height = i2 != null ? i2.getIconHeight() : 0;
    return Math.max(i1Height, i2Height);
  }
}
