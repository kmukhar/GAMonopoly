package edu.uccs.ecgs.ga;

import java.awt.Color;

public enum PropertyGroups {
  BROWN, LIGHT_BLUE, PURPLE, RED, ORANGE, YELLOW, GREEN, DARK_BLUE, RAILROADS, UTILITIES, SPECIAL;

  static {
    System.setProperty("BROWN", "0x8b4513");
    System.setProperty("LIGHT_BLUE", "0x00ced1");
    System.setProperty("PURPLE", "0xa020f0");
    System.setProperty("ORANGE", "0xffa500");
    System.setProperty("RED", "0xff0000");
    System.setProperty("YELLOW", "0xffff00");
    System.setProperty("GREEN", "0x00FF00");
    System.setProperty("DARK_BLUE", "0x0000ff");
  }

  public String toString()
  {
    return name().substring(0, 1) + name().substring(1).toLowerCase();
  }

  public Color getColor()
  {
    switch (this) {
    case RAILROADS:
    case UTILITIES:
    case SPECIAL:
      return null;
    default:
      break;
    }
    return Color.getColor(name());
  }

  /**
   * Compute how many houses could be built on a group given cash and assuming
   * the same number of houses on each property in the group. First compute the
   * total number of houses that can be built for the given amount of cash, then
   * divide by the number of properties in the group to get the number of houses
   * for each property. Integer arithmetic is used to ensure that each property
   * gets the same number of houses.
   * 
   * @param cash
   *          The amount of cash to be used to build houses
   * @return The number of houses that can be built, assuming same number of
   *         houses on each property in the group
   */
  public int getNumHouses(int cash)
  {
    int houseCost = 0;

    switch (this) {
    case BROWN:
    case LIGHT_BLUE:
      houseCost = 50;
      break;

    case PURPLE:
    case RED:
      houseCost = 100;
      break;

    case ORANGE:
    case YELLOW:
      houseCost = 150;
      break;

    case GREEN:
    case DARK_BLUE:
      houseCost = 200;
      break;

    default:
      houseCost = Integer.MAX_VALUE;
    }

    int numHouses = cash / houseCost;

    switch (this) {
    case BROWN:
    case DARK_BLUE:
      numHouses /= 2;
      break;
    default:
      numHouses /= 3;
    }

    if (numHouses > 5) 
      numHouses = 5;

    return numHouses;
  }
}
