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
    } else {
      setRentMultiplier(owner.getNumUtilities() == 1 ? 4 : 10);
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

    result.append(name).append("\n\n");
    result.append("If one utility is owned\nrent is 4 times amount shown\n");
    result.append("on dice.\n\n");
    result.append("If both utilities are owned\nrent is 10 times amount shown");
    result.append("\non dice.\n\n");
    result.append("Mortgage Value\t$").append(cost/2);

    return result.toString();
  }
}
