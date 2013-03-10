package edu.uccs.ecgs.ga;

import java.util.Properties;

/**
 * Represents one of the utilities in the game, either Electric Company or Water
 * Works.
 */
public class UtilityLocation extends Location {

  private int cost;
  private PropertyGroups group = PropertyGroups.UTILITIES;

  public UtilityLocation(String key2, Properties properties) {
    super(key2, properties);

    cost = getInteger(key + ".cost", properties);

    _string = "Name         : " + name + "\n  index        : " + index
        + "\n  group        : " + group + "\n  type         : " + type
        + "\n  cost         : " + cost;
  }

  @Override
  public int getCost()
  {
    return cost;
  }

  @Override
  public int getRent(int diceRoll)
  {
    assert !isMortgaged() : "Location is mortgaged in getRent";

    if (arrivedFromChance) {
      setRentMultiplier(10);
      // and if arrived from chance, roll the dice again
      int[] newRoll = Dice.getDice().roll();
      diceRoll = newRoll[0] + newRoll[1];
      getOwner().logInfo(
          "Arrived from Chance; roll for rent is " + newRoll[0] + "+"
              + newRoll[1]);
    } else {
      setRentMultiplier(getOwner().getNumUtilities() == 1 ? 4 : 10);
    }

    int rent = 0;

    rent = diceRoll * multiple;

    resetRentMultiplier();
    arrivedFromChance = false; // this value no longer matters, so reset to
                               // default

    return rent;
  }

  @Override
  public PropertyGroups getGroup()
  {
    return group;
  }

  @Override
  public void setMortgaged()
  {
    setMortgaged(true);
  }

  @Override
  public void setMortgaged(boolean b)
  {
    isMortgaged = b;
  }

  @Override
  public int getPotentialRent(int numHouses, int diceRoll)
  {
    // rent with a monopoly of utilities is 10 * dice roll
    return diceRoll * 10;
  }

  @Override
  public String getFormattedString()
  {
    StringBuilder result = new StringBuilder();

    result.append(name).append("<p><p>");
    result.append("If one utility is owned rent is 4 times amount shown ");
    result.append("on dice.<p><p>");
    result.append("If both utilities are owned rent is 10 times amount shown ");
    result.append("on dice.<p>");
    result.append("<table width=100% border=0>");
    result.append("<tr><td>");
    result.append("Mortgage Value</td><td>$").append(cost/2).append("</td></tr>");
    result.append("</table>");

    return result.toString();
  }
}
