package edu.uccs.ecgs.ga;

import java.util.Properties;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import edu.uccs.ecgs.players.AbstractPlayer;

/**
 * Class the implements a location (property or special location) in the game.
 * Location can be an actual property like Vermont or Marvin Gardens, a utility,
 * a railroad, or a special location like Go or Chance.
 * 
 */
public abstract class Location implements Comparable<Location> {
  /**
   * A short string that identifies the values for a given property in the
   * locations.properties file.
   */
  final String key;

  /**
   * The name of this property.
   */
  public final String name;

  /**
   * The hyphenated name of this property, for displays on buttons, etc.
   */
  public final String hname;

  /**
   * The type of this location: Property, Railroad, Utility, or Special.
   */
  protected final String type;

  /**
   * The position of this location on the game board. Locations are numbered
   * sequentially starting at Go with an index of 0, and proceeding clockwise
   * around the board to Boardwalk with an index of 39.
   */
  public final int index;

  /**
   * A string that gives information about the property. Access using
   * {@link #getInfo()}.
   */
  protected String _string;

  /**
   * A reference to the player that owns this location. Only has meaning for
   * locations of type Property, Railroad, or Utility.
   */
  private AbstractPlayer owner = null;

  /**
   * Whether this location is part of a monopoly. Only has meaning for locations
   * of type Property.
   */
  public boolean partOfMonopoly = false;

  /**
   * Whether the player who is on this space arrived by drawing a Chance card
   * that directed the player to this location. Used only for utilities.
   */
  boolean arrivedFromChance = false;

  /**
   * Number of houses on this location. Only has meaning for locations of type
   * Property.
   */
  private int numHouses = 0;

  /**
   * Number of hotels on this location. Only has meaning for locations of type
   * Property.
   */
  private int numHotels = 0;

  /**
   * Whether this location is mortgaged. Only has meaning for locations of type
   * Property, Railroad, or Utility.
   */
  protected boolean isMortgaged = false;

  /**
   * Rent multiplier. See {@link #setRentMultiplier(int)}
   */
  protected int multiple = 1;

  /**
   * Object that wants to be notified of changes to this location
   */
  private ChangeListener changeListener;

  public Location(String key2, Properties properties) {
    key = key2;

    index = getInteger(key + ".index", properties);
    name = properties.getProperty(key + ".name");
    type = properties.getProperty(key + ".type");
    
    String temp = properties.getProperty(key + ".hname");
    hname = temp == null ? name : temp;
  }

  /**
   * Get an integer from the locations.properties file. This method does not
   * check for key existence. It is the caller's responsibility to ensure that
   * the key is valid and the property can be parsed as an integer.
   * 
   * @param aKey
   *          A string that identifies the integer, such as "Go.index"
   * @param properties
   *          The properties object that contains key-value pairs to be
   *          retrieved.
   * @return An integer
   * @throws java.lang.NumberFormatException
   *           If the properties file does not contain the given key.
   */
  protected int getInteger(String aKey, Properties properties)
  {
    return Integer.parseInt(properties.getProperty(aKey));
  }

  @Override
  public String toString()
  {
    return name;
  }
  
  public String getFullInfoString () {
    return getGroup().toString() + "/" + name
        + (isMortgaged() ? " (mortgaged)" : "");
  }

  /**
   * @return A String formatted like the property card in the physical game.
   */
  public abstract String getFormattedString();

  /**
   * @return A String formatted to be the title of the card in the physical 
   * game.
   */
  public String getFormattedTitle()
  {
    return "Title Deed";
  }

  /**
   * @return Information about the location.
   */
  public String getInfo()
  {
    return _string;
  }

  /**
   * @return A reference to the player that owns the property
   */
  public AbstractPlayer getOwner()
  {
    return owner;
  }

  /**
   * @return The cost of purchasing the property
   */
  public abstract int getCost();

  /**
   * @return The cost to buy a house for this property. For Utilities,
   *         Railroads, and special locations, this method returns 0.
   */
  public int getHouseCost()
  {
    return 0;
  }

  /**
   * @return The cost to buy a hotel for this property. For Utilities,
   *         Railroads, and special locations, this method returns 0.
   */
  public int getHotelCost()
  {
    return 0;
  }

  /**
   * Set the owner for the property to the given player.
   * 
   * @param player
   *          The player that owns the property.
   */
  public void setOwner(AbstractPlayer player)
  {
    owner = player;
    fireChangeEvent(new ChangeEvent(this));
  }

  /**
   * Temporarily set the owner for the property to the given player, for the 
   * purposes of evaluating a trade. Since this is temporary, do not fire the
   * change event. The caller must reset the owner before proceeding with
   * play.
   * 
   * @param player
   *          The player that could own the property after a trade, and for
   *          which the caller wants to evaluate the trade result.
   */
  public void setOwnerForTradeEvaluation(AbstractPlayer player)
  {
    owner = player;
  }

  /**
   * @return The number of houses on the property. For Utilities, Railroads, and
   *         special locations, this method returns 0.
   */
  public int getNumHouses()
  {
    return numHouses;
  }

  /**
   * @return The number of hotels on the property. For Utilities, Railroads, and
   *         special locations, this method returns 0.
   */
  public int getNumHotels()
  {
    return numHotels;
  }

  /**
   * @param diceRoll
   *          The total value of the dice roll of the player who landed on this
   *          property.
   * @return The rent owed.
   */
  public abstract int getRent(int diceRoll);

  /**
   * @return The PropertyGroup value that the property belongs to.
   */
  public abstract PropertyGroups getGroup();

  /**
   * @return True if the property is mortgaged, false otherwise.
   */
  public boolean isMortgaged()
  {
    return isMortgaged;
  }

  /**
   * @return True if the property can be mortgaged (property has no houses or
   * hotels and is not already mortgaged. 
   */
  public boolean canBeMortgaged() {
    return getNumHotels() == 0 && getNumHouses() == 0 && !isMortgaged();
  }

  /**
   * Set multiplier for rent. For example, unimproved properties in a monopoly
   * receive double rent so multiplier would be 2 in this case. Railroads
   * receive a multiple of the base rent depending on how many railroads are
   * owned by a player. Utilities also have a rent multiplier based on how many
   * utilities are owned by a player.
   * 
   * @param multiple
   *          The amount to multiply the rent by.
   */
  protected void setRentMultiplier(int multiple)
  {
    this.multiple = multiple;
  }

  /**
   * Reset rent multiplier to 1.
   */
  public void resetRentMultiplier()
  {
    multiple = 1;
  }

  /**
   * Remove a house from the property.
   */
  public void removeHouse()
  {
    assert numHouses > 0 : "Illegal house count: " + numHouses;
    --numHouses;
    fireChangeEvent(new ChangeEvent(this));
  }

  /**
   * Add a house to this property.
   */
  public void addHouse()
  {
    ++numHouses;
    assert numHouses < 5 : "Illegal house count: " + numHouses;
    fireChangeEvent(new ChangeEvent(this));
  }

  /**
   * Remove a hotel from this property (also sets number of houses on this
   * property to 4).
   */
  public void removeHotel()
  {
    --numHotels;
    assert numHotels == 0 : "Illegal hotel count: " + numHotels;
    fireChangeEvent(new ChangeEvent(this));
  }

  /**
   * Add a hotel to this property by removing 4 houses from property and adding
   * hotel. Property must have 4 houses prior to calling this method; caller is
   * responsible for returning the 4 houses to the game inventory.
   */
  public void addHotel()
  {
    assert numHouses == 4 : "Not enough houses to buy hotel: " + numHouses;
    ++numHotels;
    assert numHotels == 1 : "Illegal hotel count: " + numHotels;
    numHouses = 0;
    fireChangeEvent(new ChangeEvent(this));
  }

  /**
   * Set the property to be mortgaged or not, based on the input parameter.
   * 
   * @param b
   *          True if property is mortgaged, false otherwise.
   */
  public abstract void setMortgaged(boolean b);

  /**
   * Set the property to be mortgaged.
   */
  public abstract void setMortgaged();

  /**
   * Set number of houses on property to 0.
   */
  public void resetNumHouses()
  {
    numHouses = 0;
    fireChangeEvent(new ChangeEvent(this));
  }

//  /**
//   * Set number of hotels on property to 0.
//   */
//  public void resetNumHotels()
//  {
//    numHotels = 0;
//    fireChangeEvent(new ChangeEvent(this));
//  }
//
  @Override
  public int compareTo(Location arg0)
  {
    return Integer.valueOf(index).compareTo(Integer.valueOf(arg0.index));
  }

  /**
   * Is the location fully developed.
   * 
   * @return True if the location has 3 or more houses or a hotel, false
   *         otherwise
   */
  public boolean isFullyBuilt()
  {
    return getNumHouses() >= 3 || getNumHotels() > 0;
  }

  /**
   * Is any property in the same group mortgaged
   * 
   * @return True if any property in the same group as this property is
   *         mortgaged, false otherwise.
   */
  public boolean groupIsMortgaged(String gamekey)
  {
    return PropertyFactory.getPropertyFactory(gamekey).groupIsMortgaged(
        this.getGroup());
  }

  /**
   * Get the rent for this location for the given number of houses (0-4, 5 =
   * hotel)
   * 
   * @param numHouses The number of houses for which rent is desired
   * @return The rent for this location with the given number of houses
   */
  public abstract int getPotentialRent(int numHouses, int diceRoll);

  public String getHyphenatedName() {
    return hname;
  }

  public void addChangeListener(ChangeListener cl) {
    this.changeListener = cl;
  }

  public void fireChangeEvent(ChangeEvent event) {
    if (changeListener != null)
      changeListener.stateChanged(event);
  }
}
