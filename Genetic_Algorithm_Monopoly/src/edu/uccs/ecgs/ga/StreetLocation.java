package edu.uccs.ecgs.ga;

import java.util.Properties;

/**
 * Represents one of the properties that can have houses or hotels in Monopoly,
 * that is, a property of PropertyType STREET.
 */
public class StreetLocation extends Location {

  final private PropertyGroups group;
  final private int houseCost;
  final private int hotelCost;
  final private int rentUnimproved;
  final private int rentOneHouse;
  final private int rentTwoHouses;
  final private int rentThreeHouses;
  final private int rentFourHouses;
  final private int rentHotel;
  final private int cost;

  public StreetLocation(String key, Properties properties) {
    super(key, properties);

    cost = getInteger(key + ".cost", properties);
    houseCost = getInteger(key + ".cost.house", properties);
    hotelCost = getInteger(key + ".cost.hotel", properties);
    rentUnimproved = getInteger(key + ".rent.unimproved", properties);
    rentOneHouse = getInteger(key + ".rent.one_house", properties);
    rentTwoHouses = getInteger(key + ".rent.two_houses", properties);
    rentThreeHouses = getInteger(key + ".rent.three_houses", properties);
    rentFourHouses = getInteger(key + ".rent.four_houses", properties);
    rentHotel = getInteger(key + ".rent.hotel", properties);

    String grp = properties.getProperty(key + ".group");
    group = PropertyGroups.valueOf(grp.toUpperCase());

    _string = "Name           : " + name + "\n  index        : " + index
        + "\n  group        : " + group + "\n  type         : " + type
        + "\n  cost         : " + cost + "\n" + "  house cost   : " + houseCost
        + "\n" + "  hotel cost   : " + hotelCost + "\n" + "  rent         : "
        + rentUnimproved + "\n" + "  rent 1 houses: " + rentOneHouse + "\n"
        + "  rent 2 houses: " + rentTwoHouses + "\n" + "  rent 3 houses: "
        + rentThreeHouses + "\n" + "  rent 4 houses: " + rentFourHouses + "\n"
        + "  rent hotel   : " + rentHotel;
  }

  @Override
  public int getCost()
  {
    return cost;
  }

  @Override
  public int getHotelCost()
  {
    return hotelCost;
  }

  @Override
  public int getHouseCost()
  {
    return houseCost;
  }

  @Override
  public int getRent(int diceRoll)
  {
    int rent = 0;

    assert !isMortgaged() : "Location is mortgaged in getRent";

    int buildCount = getNumHouses() + (getNumHotels() * 5);
    // Unimproved properties that are part of a monopoly receive double rent.
    if (partOfMonopoly) {
      setRentMultiplier(2);
    }

    rent = getRentForHouses(buildCount);
    
    resetRentMultiplier();

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
  public String toString()
  {
    return super.toString() + (isMortgaged() ? " (mortgaged)" : "") + " ("
        + getNumHouses() + " houses/" + getNumHotels() + " hotels)";

  }

  @Override
  public int getPotentialRent(int numHouses, int diceRoll)
  {
    setRentMultiplier(2);
    int rent = getRentForHouses(numHouses);
    resetRentMultiplier();
    return rent;
  }

  private int getRentForHouses(int numHouses)
  {
    int rent = 0;

    switch (numHouses) {
    case 0:
      rent = rentUnimproved * multiple;
      break;
    case 1:
      rent = rentOneHouse;
      break;
    case 2:
      rent = rentTwoHouses;
      break;
    case 3:
      rent = rentThreeHouses;
      break;
    case 4:
      rent = rentFourHouses;
      break;
    case 5:
      rent = rentHotel;
      break;
    }
    return rent;
  }
}
