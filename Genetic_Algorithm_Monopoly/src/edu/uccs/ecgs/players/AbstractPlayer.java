package edu.uccs.ecgs.players;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.uccs.ecgs.ga.*;
import edu.uccs.ecgs.states.Events;
import edu.uccs.ecgs.states.PlayerState;

/**
 * A class that is the basis of all player classes in the simulation
 */
public abstract class AbstractPlayer implements Comparable<AbstractPlayer>,
    Cloneable {
  public int cash;
  private boolean rolledDoubles = false;

  // Set to 3 when entering jail, must leave when count reaches 0
  protected int jailSentence = 0;

  private boolean passedGo = false;
  private int locationIndex = 0;
  protected Location location;
  public int playerIndex;
  public int initialGeneration;

  public GameState currentState = GameState.INACTIVE;

  public Actions nextAction = Actions.NULL;

  PlayerState playerState = PlayerState.inactiveState;

  // build houses on property groups in this order
  // 1 Orange
  // 2 Light Blue
  // 3 Red
  // 4 Purple
  // 5 Dark Blue
  // 6 Yellow
  // 8 Green
  // 9 Brown
  private static final PropertyGroups[] groupOrder = new PropertyGroups[] {
      PropertyGroups.ORANGE, PropertyGroups.LIGHT_BLUE, PropertyGroups.RED,
      PropertyGroups.PURPLE, PropertyGroups.DARK_BLUE, PropertyGroups.YELLOW,
      PropertyGroups.GREEN, PropertyGroups.BROWN };

  public Random r = new Random();

  private TreeMap<Integer, Location> owned;
  public boolean inJail = false;
  private Chance chanceGOOJ; // chance get out of jail card
  private CommunityChest ccGOOJ; // community chest get out of jail card
  private int fitnessScore = 0;
  private int finishOrder = 0;

  private boolean isBankrupt = false;
  private int bankruptIndex = 0;
  protected Monopoly game;
  // the total net worth of all players in a single game
  private int gameNetWorth;
  protected ChromoTypes chromoType;

  PropertyNegotiator propertyTrader;
  // in the paper, w1 and w2 are used to balance risk taking against risk avoidance
  // w1 + w2 = 1.0
  // a high w1 means the player tries to get gains
  // a high w2 means the player tries to avoid losses
  public double w1 = 0.5;
  public double w2 = 0.5;
  // if the profit exceeds this threshold, the player accepts the trade.
  private int tradeThreshold = 100;
  protected String gameKey;
  private ChangeListener changeListener;

  /**
   * Constructor
   * 
   * @param index
   *          An id for the player
   * @param chromoType
   */
  public AbstractPlayer(int index, ChromoTypes chromoType) {
    this.chromoType = chromoType;
    long seed = 1241797664697L;
    if (Main.useRandomSeed) {
      seed = System.currentTimeMillis();
    }
    r.setSeed(seed);

    playerIndex = index;
    owned = new TreeMap<Integer, Location>();
    clearAllProperties();
    cash = 1500;
  }

  /**
   * @return A String that gives the name of the player in the form
   * "Player n" where n is the playerIndex.
   */
  public String getName() {
    return "Player " + playerIndex;
  }

  /**
   * Remove all properties from the player. This method simply clears the
   * player's list of owned properties. No other changes are made to the
   * player's state or to the properties' state.
   */
  public void clearAllProperties() {
    if (owned != null) {
      owned.clear();
    }
    fireChangeEvent();
  }

  /**
   * Does the player have at least as much cash as amount.
   * 
   * @param amount
   *          The amount that is being checked.
   * @return True --> if the player's cash is greater than or equal to amount.<br>
   *         False --> otherwise.
   */
  public boolean hasAtLeastCash(int amount) {
    if (cash >= amount) {
      return true;
    }

    return false;
  }

  /**
   * Initialize the player's cash to amount.
   * 
   * @param amount
   *          The amount of cash the player should have.
   */
  public void initCash(int amount) {
    cash = amount;
  }

  /**
   * Reset the doubles counter for this player; should only be called at the
   * start of the player's turn.
   */
  public void resetDoubles() {
    rolledDoubles = false;
  }

  /**
   * Reset the player to the default state, ready to play a game. This resets
   * cash to 1500, removes all properties, resets location, resets bankruptcy
   * state, etc.
   */
  private void resetAll() {
    logFinest(getName() + " entering resetAll()");
    cash = 1500;
    rolledDoubles = false;
    jailSentence = 0;

    passedGo = false;
    locationIndex = 0;

    currentState = GameState.INACTIVE;

    nextAction = Actions.NULL;

    playerState = PlayerState.inactiveState;

    inJail = false;
    chanceGOOJ = null;
    ccGOOJ = null;

    isBankrupt = false;
    bankruptIndex = 0;

    clearAllProperties();
    fireChangeEvent();
  }

  /**
   * Set player's state to inactive.
   */
  public void setInactive() {
    setNewState(GameState.INACTIVE);
  }

  public Actions getNextActionEnum(Events event) {
    playerState = playerState.processEvent(game, this, event);
    return nextAction;
  }

  /**
   * Set player's state to gameState.
   * 
   * @param gameState
   *          State in which player is.
   */
  public void setNewState(GameState gameState) {
    currentState = gameState;
  }

  /**
   * Set the player's rolledDoubles flag to the input parameter. If the player
   * is in jail and the parameter is false (player did not roll doubles), this
   * method reduces the jail term counter.
   * 
   * @param rolledDoubles
   *          True if the player rolled doubles, false otherwise.
   */
  public void setDoubles(boolean rolledDoubles) {
    this.rolledDoubles = rolledDoubles;
    if (inJail && !rolledDoubles) {
      --jailSentence;

      logFinest(getName() + " jailSentence: " + jailSentence);
      assert jailSentence >= 0 : "Illegal jailSentence value: " + jailSentence;
    }
  }

  /**
   * Move the player's location by numSpaces, if the player passes Go, the
   * player receives $200.
   * 
   * @param numSpaces
   *          The number of spaces to move.
   */
  public void move(int numSpaces) {
    passedGo = false;
    locationIndex += numSpaces;
    if (locationIndex >= 40) {
      locationIndex -= 40;
      passedGo = true;
    }
    
    if (locationIndex < 0) {
      locationIndex += 40;
    }

    Location location = PropertyFactory.getPropertyFactory(this.gameKey)
        .getLocationAt(locationIndex);
    setCurrentLocation(location);

    if (passedGo) {
      if (locationIndex == 0) {
        logInfo(getName() + " landed on Go");
      } else {
        logInfo(getName() + " passed Go");
      }
      receiveCash(200);
    }
    fireChangeEvent();
  }

  /**
   * Go to jail. Go directly to Jail. Do not pass Go.
   */
  public void goToJail(Location jail) {
    enteredJail();
    setLocationIndex(jail.index);
    setCurrentLocation(jail);
    fireChangeEvent();
  }

  /**
   * @return The player's current location index.
   */
  public int getLocationIndex() {
    return locationIndex;
  }

  /**
   * Set the player's current location to the location parameter.
   * 
   * @param location
   *          The location where the player is currently located.
   */
  private void setCurrentLocation(Location location) {
    this.location = location;

    logInfo(getName() + " moving to " + location.name);
    if (location.owner != null) {
      logInfo(location.name + " is owned by " + location.owner.getName());
    }

    if (location.name.equals("Jail")) {
      if (inJail) {
        logInfo(getName() + " is in Jail");
        logFinest("Player sentence: " + jailSentence);
        logFinest("Player inJail flag: " + inJail);
        assert inJail : "Flag inJail is not valid";
        assert jailSentence == 3 : "JailSentence value is not correct";
      } else {
        logInfo(getName() + " is Just Visiting");
      }
    }
  }

  /**
   * @return True --> if the player passed Go or landed on Go during the most
   *         recent movement,<br>
   *         false --> otherwise.
   */
  public boolean passedGo() {
    return passedGo;
  }

  /**
   * Add cash to the player's current amount of cash.
   * 
   * @param amount
   *          The amount of cash to add the player's current amount of cash.
   */
  public void receiveCash(int amount) {
    cash += amount;
    logInfo(getName() + " received " + amount + " dollars.");
    fireChangeEvent();
  }

  /**
   * Take some cash from the player.
   * 
   * @param amount
   *          The amount of cash to take from the player
   * @throws BankruptcyException
   *           If player does not have the amount and cannot sell houses or
   *           hotels and cannot mortgage any properties to raise the amount.
   */
  public void getCash(int amount) throws BankruptcyException {
    raiseCash(amount);
    cash = cash - amount;
    logInfo(getName() + " paid " + amount + " dollars.");
    fireChangeEvent();
  }

  /**
   * @return The number of railroads that the player owns.
   */
  public int getNumRailroads() {
    int count = 0;
    for (Location property : owned.values()) {
      if (property.getGroup() == PropertyGroups.RAILROADS) {
        ++count;
      }
    }
    return count;
  }

  /**
   * @return The number of Utilities that the player owns.
   */
  public int getNumUtilities() {
    int count = 0;
    for (Location property : owned.values()) {
      if (property.getGroup() == PropertyGroups.UTILITIES) {
        ++count;
      }
    }
    return count;
  }

  /**
   * Add a property to the player's inventory, normally by buying a property or
   * receiving a property through another player's bankruptcy.
   * 
   * @param location
   *          The property to be added.
   */
  public void addProperty(Location location) {
    owned.put(location.index, location);
    location.setOwner(this);

    // mark all the properties that are part of monopolies
    PropertyFactory.getPropertyFactory(this.gameKey).checkForMonopoly();
    if (location.partOfMonopoly) {
      logInfo(getName() + " acquired monopoly with " + location.name);
    }
    fireChangeEvent();
  }

  /**
   * Remove a property from the list of properties owned by this player.
   * @param location The property to be removed from player
   */
  public void removeProperty(Location location) {
    owned.remove(location.index);
    location.setOwner(null);
  }

  /**
   * Set this player's location to the location at index.
   * 
   * @param index
   *          The index of the location, corresponds to board position with Go
   *          having index 0 and increasing sequentially counter-clockwise
   *          around the board.
   */
  public void setLocationIndex(int index) {
    locationIndex = index;
  }

  /**
   * @return True if the player is in jail, false otherwise.
   */
  public boolean inJail() {
    return inJail;
  }

  /**
   * @return A reference to the Location where the player current is.
   */
  public Location getCurrentLocation() {
    return location;
  }

  /**
   * @return True --> if the player rolled doubles on most recent dice roll,<br>
   *         False --> otherwise.
   */
  public boolean rolledDoubles() {
    return rolledDoubles;
  }

  /**
   * @return True if the player has either Get Out Of Jail Free card.
   */
  public boolean hasGetOutOfJailCard() {
    return chanceGOOJ != null || ccGOOJ != null;
  }

  /**
   * Use the player's Get Out Of Jail Free card by returning it to the Card
   * collection; modifying other state related to being in jail is not performed
   * by this method.
   */
  public void useGetOutOfJailCard() {
    if (chanceGOOJ != null) {
      game.getCards().returnChanceGetOutOfJail();
      chanceGOOJ = null;
    } else if (ccGOOJ != null) {
      game.getCards().returnCCGetOutOfJail();
      ccGOOJ = null;
    } else {
      throw new IllegalArgumentException(
          "Illegal attempt to use Get Out Of Jail Card");
    }
    fireChangeEvent();
  }

  /**
   * @return The total worth of the player including cash, value of all houses
   *         and hotels, and value of all property owned by the player.
   */
  public int getTotalWorth() {
    int totalWorth = cash;

    for (Location location : owned.values()) {
      totalWorth += location.getNumHouses() * location.getHouseCost();

      if (location.getNumHotels() != 0) {
        // for hotels, add the cost of the 4 houses that had to be built
        // before the hotel was built
        totalWorth += (location.getHotelCost() * 5);
      }

      if (location.isMortgaged()) {
        totalWorth += location.getCost() / 2;
      } else {
        totalWorth += location.getCost();
      }
    }

    return totalWorth;
  }

  /**
   * Add to the player's fitness score.
   * 
   * @param score
   *          The amount to add to the player's fitness.
   */
  public void addToFitness(int score) {
    fitnessScore += score;
    assert fitnessScore >= 0;
  }

  /**
   * Set the player's fitness score to the given value.
   * 
   * @param score
   *          The new value for the fitness score.
   */
  public void setFitness(int score) {
    fitnessScore = score;
  }

  /**
   * Reset the fitness value to 0
   */
  public void resetFitness() {
    fitnessScore = 0;
  }

  /**
   * @return The player's current fitness score.
   */
  public int getFitness() {
    return fitnessScore;
  }

  /**
   * @return the finishOrder
   */
  public int getFinishOrder() {
    return finishOrder;
  }

  /**
   * Sets the finish order of a game, 1 for winner (first place), 2 for second
   * place, etc.
   * 
   * @param finishOrder
   *          the finishOrder to set
   */
  public void setFinishOrder(int finishOrder) {
    this.finishOrder = finishOrder;
  }

  /**
   * @return True --> if the player is bankrupt,<br>
   *         False --> otherwise.
   */
  public boolean bankrupt() {
    return isBankrupt;
  }

  /**
   * Add the Get Out Of Jail Free Card to the player's inventory.
   * 
   * @param chanceJailCard
   *          The card to add.
   */
  public void setGetOutOfJail(Chance chanceJailCard) {
    chanceGOOJ = chanceJailCard;
    fireChangeEvent();
  }

  /**
   * Add the Get Out Of Jail Free Card to the player's inventory.
   * 
   * @param ccJailCard
   *          The card to add.
   */
  public void setGetOutOfJail(CommunityChest ccJailCard) {
    ccGOOJ = ccJailCard;
    fireChangeEvent();
  }

  /**
   * @return The number of houses that have been bought for all properties that
   *         are owned by this player
   */
  public int getNumHouses() {
    int result = 0;

    for (Location loc : owned.values()) {
      result += loc.getNumHouses();
    }

    return result;
  }

  /**
   * @return The number of hotels that have been bought for all properties that
   *         are owned by this player.
   */
  public int getNumHotels() {
    int result = 0;

    for (Location loc : owned.values()) {
      result += loc.getNumHotels();
    }

    return result;
  }

  /**
   * Calculate whether the player has or can sell enough things to have at least
   * the cash given by amount. If the player's cash is already greater than
   * amount then the method simply returns true. if the player's current cash is
   * less than amount, then the method determines if the player can sell enough
   * houses, hotels, and mortgage properties to have cash greater than or equal
   * to amount. This method does not actually sell any houses, hotels, or
   * properties; it just computes how much cash could be raised if the player
   * sold everything.
   * 
   * @param amount
   *          The amount the player needs to have in cash
   * @return True if the player has or can sell stuff to raise cash greater than
   *         amount, false otherwise.
   */
  public boolean canRaiseCash(int amount) {
    int totalWorth = cash;

    if (totalWorth >= amount) {
      return true;
    }

    for (Location location : owned.values()) {
      // add selling price for all houses
      totalWorth += location.getNumHouses() * location.getHouseCost() / 2;
      // add selling price for all hotels (hotels == 5 houses)
      totalWorth += location.getNumHotels() * 5 * location.getHotelCost() / 2;
      // add cash for mortgaging any unmortgaged properties
      if (!location.isMortgaged()) {
        totalWorth += location.getCost() / 2;
      }
    }

    if (totalWorth >= amount) {
      return true;
    }

    return false;
  }

  /**
   * Ask if the player wishes to pay bail.
   * 
   * @return True --> player wishes to pay bail to leave jail.<br>
   *         False --> player wishes to attempt to roll doubles to leave jail.
   */
  public abstract boolean payBailP();

  /**
   * Ask if the player wants to buy their current location.
   * 
   * @return True --> If the player wants to buy the property at their current
   *         location<br>
   *         False --> If the player does not want to buy the property at their
   *         current location.
   */
  public abstract boolean buyProperty();

  /**
   * Ask if the player wants to buy the given location.
   * 
   * @return True --> If the player wants to buy the given location <br>
   *         False --> If the player does not want to buy the given location.
   */
  public abstract boolean buyProperty(Location location);

  /**
   * Output the player genome to a data file.
   * 
   * @param out
   *          The output stream to which data should be written.
   * @throws IOException
   *           If there is a problem writing out the data.
   */
  public abstract void dumpGenome(DataOutputStream out) throws IOException;

  /**
   * Determine the amount that this player wants to bid in an auction for the
   * given location. A player can bid on a property in an auction even if the
   * player just decided not to buy it directly after landing on the property.
   * 
   * @param currentLocation
   *          The property being auctioned.
   * @return The amount of this player's bid.
   */
  public int getBidForLocation(Location currentLocation) {
    int bid = 0;

    if (cash < 50) {
      bid = 0;
    } else if (buyProperty(currentLocation)) {
      // player wants to buy, so start with current cost
      bid = currentLocation.getCost();

      double adjustFactor = Math.abs(r.nextGaussian());
      adjustFactor = adjustFactor * (double) (bid / 10);
      bid += (int) adjustFactor;
    } else {
      // otherwise, player does not want location
      if (currentLocation == location) {
        // if player is the one at the location, then bid some small
        // amount (cost/2 or cost/3 or cost/4)
        int factor = r.nextInt(3) + 2; // factor is 2,3,4
        bid = currentLocation.getCost() / factor;
      } else {
        // otherwise, other players bid half cost
        // plus some random fluctuation
        bid = (currentLocation.getCost() / 2)
            + (int) (Math.abs(r.nextGaussian()) * (double) (currentLocation
                .getCost() / 6));
      }
    }

    // ensure bid does not exceed cash
    if (bid > cash) {
      bid = cash;
    }

    assert bid >= 0 : "Invalid bid amount: " + bid;

    return bid;
  }

  /**
   * Called by game if the player lands in jail either through rolling doubles
   * three times, getting a Go To Jail card, or landing on the Go To Jail
   * location.
   */
  private void enteredJail() {
    inJail = true;
    jailSentence = 3;
  }

  /**
   * Ask whether player must leave jail or not. Player must leave jail when they
   * have declined to pay bail three times and have had 3 chances to roll
   * doubles but have not rolled doubles.
   * 
   * @return True if the player must pay bail and leave jail, false otherwise.
   */
  public boolean jailSentenceCompleted() {
    return jailSentence == 0;
  }

  /**
   * Change player state based on having paid bail to leave jail.
   */
  public void leaveJail() {
    inJail = false;
    jailSentence = 0;
  }

  /**
   * Ask if the player has a monopoly in any property group, ignoring whether
   * any of those properties are mortgaged; callers of this method should also
   * check for mortgaged properties before taking any action for a monopoly.
   * Mortgaged properties can impact what a player can do with a monopoly. For
   * example, even if the player has all the properties of a color group, the
   * player cannot build any houses for a color group if one or more properties
   * are mortgaged.
   * 
   * @return True if the player has at least one monopoly, false otherwise.
   */
  public boolean hasMonopoly() {
    boolean result = false;
    for (Location l : owned.values()) {
      if (l.partOfMonopoly) {
        result = true;
        break;
      }
    }

    return result;
  }

  /**
   * Attempt to mortgage properties, then sell hotels, then sell houses until
   * the player's cash is greater than or equal to amount.
   * 
   * @param amount
   *          The amount of cash that the player is trying to have on hand.
   * @throws BankruptcyException
   *           If the player cannot raise enough cash to equal or exceed amount.
   */
  public void raiseCash(int amount) throws BankruptcyException {
    logFinest(getName() + " has " + cash + " dollars");
    if (cash >= amount) {
      return;
    }
    if (canRaiseCash(amount)) {
      logInfo(getName() + " attempting to raise " + amount
          + " dollars");
      for (Location l : owned.values()) {
        // mortgage single street properties first
        if (!l.partOfMonopoly && !l.isMortgaged()
            && l.getGroup() != PropertyGroups.UTILITIES
            && l.getGroup() != PropertyGroups.RAILROADS) {
          // mortgage property if not part of monopoly
          logInfo(getName() + " will mortgage " + l.name);
          l.setMortgaged();
          receiveCash(l.getCost() / 2);
        }
        if (cash >= amount) {
          return;
        }
      }

      // then mortgage single utilities
      if (getNumUtilities() == 1) {
        for (Location l : owned.values()) {
          if (l.getGroup() == PropertyGroups.UTILITIES && !l.isMortgaged()) {
            logInfo(getName() + " will mortgage " + l.name);
            l.setMortgaged();
            receiveCash(l.getCost() / 2);
          }
        }
        if (cash >= amount) {
          return;
        }
      }

      // then mortgage railroads, and then utilities
      // sell railroads in order 1,4,2,3 or location index 5,35,15,25
      // sell utilities in order electric, water or location index 12, 28
      int[] index = new int[] { 5, 35, 15, 25, 12, 28 };
      for (int i : index) {
        Location l = owned.get(i);
        if (l != null && !l.isMortgaged()) {
          logInfo(getName() + " will mortgage " + l.name);
          l.setMortgaged();
          receiveCash(l.getCost() / 2);
        }
        if (cash >= amount) {
          return;
        }
      }

      // sell hotels
      for (Location l : owned.values()) {
        if (l.getNumHotels() > 0) {
          logInfo(getName() + " will sell hotel at " + l.name);
          game.sellHotel(this, l, owned.values());
        }
        if (cash >= amount) {
          return;
        }
      }

      // then sell houses
      int maxHouses = 4;
      while (maxHouses > 0) {
        for (Location l : owned.values()) {
          if (l.getNumHouses() == maxHouses) {
            logFinest(l.name + " has " + l.getNumHouses() + " houses");
            logInfo(getName() + " will sell house at " + l.name);
            game.sellHouse(l);
          }
        }

        if (cash >= amount) {
          return;
        }

        --maxHouses;
      }

      // then mortgage any remaining unmortgaged lots
      for (Location l : owned.values()) {
        if (!l.isMortgaged()) {
          logInfo(getName() + " will mortgage " + l.name);
          l.setMortgaged();
          receiveCash(l.getCost() / 2);
        }
        if (cash >= amount) {
          return;
        }
      }
    }

    // don't have cash and can't raise cash
    throw new BankruptcyException();
  }

  public TreeMap<Integer, Location> getAllProperties() {
    return owned;
  }

  /**
   * Add properties from a bankrupt player to this player
   * 
   * @param allProperties
   *          All the properties owned by a player who has gone bankrupt
   * @param gameOver
   *          Whether the game is over because the bankrupt player is the last
   *          other player in the game
   * @throws BankruptcyException
   */
  public void addProperties(TreeMap<Integer, Location> allProperties,
      boolean gameOver) throws BankruptcyException {
    
    TreeMap<Integer, Location> mortgaged = new TreeMap<Integer, Location>();

    // add all properties first
    for (Location l : allProperties.values()) {
      owned.put(l.index, l);
      l.owner = this;
      if (l.isMortgaged())
        mortgaged.put(l.index, l);
    }

    // mark all the properties that are part of monopolies
    PropertyFactory.getPropertyFactory(this.gameKey).checkForMonopoly();

    // if the game isn't over, then the gaining player needs to pay the
    // 10% fee on any mortgaged properties, and possibly unmortgage the
    // properties
    if (!gameOver) {
      processMortgagedNewProperties(mortgaged);
    }

    fireChangeEvent();
  }

  /**
   * Go through all the new properties gained by this player, and for the ones
   * that are mortgaged, pay the fee and then determine whether or not to
   * unmortgage the property
   * 
   * When a bankrupt player gives all property over to creditor, the new owner
   * must at once pay the Bank the amount of interest on the loan, which is 10%
   * of the value of the property. The new owner who does this may then, at
   * his/her option, pay the principal or hold the property until some later
   * turn, then lift the mortgage. If he/she holds property in this way until a
   * later turn, he/she must pay the interest again upon lifting the mortgage.
   * 
   * @param newProperties
   *          The mortgaged properties that the player is receiving.
   * @throws BankruptcyException
   *           If the player receiving the properties cannot pay the interest.
   */
  private void processMortgagedNewProperties(
      TreeMap<Integer, Location> newProperties) throws BankruptcyException 
  {
    Vector<Location> mortgaged = getSortedMortgages(newProperties);
    Vector<Location> unlifted = new Vector<Location>();

    if (mortgaged.size() == 0)
      return;

    logInfo(getName() + " received " + mortgaged.size()
        + " mortgaged properties.");
    logInfo(getName() + " owes interest on all new properties, "
        + "and may choose to lift morgages.");
    
    // determine how many can be paid off
    int payoff = 0;
    int availableCash = cash - getMinimumCash();
    do {
      // compute total payoff cost
      payoff = 0;
      for (Location lot : mortgaged) {
        payoff += 1.1 * lot.getCost() / 2;
      }
      for (Location lot : unlifted) {
        payoff += 0.1 * lot.getCost() / 2;
      }

      // if not enough cash to payoff all, remove last entry from map
      if (payoff > availableCash) {
        Location lot = mortgaged.remove(mortgaged.size() - 1);
        unlifted.add(lot);
      }

    } while (payoff > availableCash && mortgaged.size() > 0);

    // at this point, there is a payoff amount, a set of lots to lift the
    // mortgage from (may be empty), and a set of lots to just pay the
    // interest on (may be empty)
    
    // Start by checking whether the player can pay the payoff amount
    if (!canRaiseCash(payoff)) {
      logInfo(getName() + " can't raise the cash to pay interest.");
      throw new BankruptcyException();
    }

    // so either the player has enough cash, or can raise enough cash
    if (unlifted.size() > 0) {
      StringBuilder names = new StringBuilder();
      boolean first = true;
      for (Location lot : unlifted) {
        if (first) {
          names.append(lot.name);
          first = false;
        } else
          names.append(", ").append(lot.name);
      }
      logInfo(getName() + " paid interest on " + names.toString() + ".");
    }

    if (mortgaged.size() > 0) {
      StringBuilder names = new StringBuilder();
      boolean first = true;
      for (Location lot : mortgaged) {
        lot.setMortgaged(false);
        if (first) {
          names.append(lot.name);
          first = false;
        } else {
          names.append(", ").append(lot.name);
        }
      }
      logInfo(getName() + " lifted mortgage on " + names.toString());
    }

    try {
      getCash(payoff);
    } catch (BankruptcyException ignored) {
      // payoff < availableCash < cash, so this exception should not occur
    }
  }

  /**
   * Create a list of all mortgaged properties and decide whether or not to pay
   * them off.
   */
  public void payOffMortgages() {
    Vector<Location> lots = getSortedMortgages(owned);

    if (lots.size() == 0) 
      return;

    // now determine how many can be paid off
    int payoff = 0;
    int availableCash = cash - getMinimumCash();
    do {
      // compute total payoff cost
      payoff = 0;
      for (Location lot : lots) {
        payoff += 1.1 * lot.getCost() / 2;
      }

      // if not enough cash to payoff all, remove last entry from map
      if (payoff > availableCash)
        lots.remove(lots.size() - 1);

    } while (payoff > availableCash && lots.size() > 0);

    assert payoff < cash;
    if (lots.size() > 0) {
      logInfo(getName() + " lifting mortgages.");
      try {
        getCash(payoff);
      } catch (BankruptcyException ignored) {
        // payoff < availableCash < cash, so this exception should not occur
      }

      for (Location lot : lots) {
        lot.setMortgaged(false);
        logInfo(lot.name + " is no longer mortgaged.");
      }
    }
  }

  /**
   * Find all the Location objects in the lots argument, create a sorted
   * Collection of all the locations that are mortgaged. Sorting is in 
   * order of streets that are part of monopolies, utilities that are part of
   * monopolies, railroads, single utilities, and streets that are not part of
   * monopolies.
   * 
   * @param lots The set of Locations objects to check
   * @return A Sorted vector of Location objects
   */
  private Vector<Location> getSortedMortgages(TreeMap<Integer, Location> lots) {
    TreeMap<Integer, Location> map = new TreeMap<Integer, Location>();
    int mOffset = 0; // index offset for mortgaged streets in monopolies
    int u2Offset = 40; // index offset for mortgaged utility monopolies
    int rOffset = 80; // index offset for mortgaged railroads
    int u1Offset = 120; // index offset for mortgaged single utilities
    int sOffset = 160; // index offset for mortgaged streets 

    // go through all the lots, and if they are mortgaged, put them into a
    // sorted map in order by street monopolies, utility monopolies, railroads,
    // single utilities, and finally streets that are not part of monopolies
    for (Location lot : lots.values()) {
      int offset = rOffset;

      if (lot.isMortgaged()) {
        logFinest(lot.name
            + " is mortgaged; added to list of properties to unmortgage");

        if (lot instanceof StreetLocation) {
          offset = sOffset;
          if (lot.partOfMonopoly)
            offset = mOffset;
        }

        if (lot instanceof UtilityLocation) {
          offset = u1Offset;
          if (lot.partOfMonopoly)
            offset = u2Offset;
        }

        map.put(lot.index + offset, lot);
      }
    }

    Vector<Location> result = new Vector<Location>();
    result.addAll(map.values());
    return result;
  }

  /**
   * Compute the minimum amount of cash the player should have on hand based on
   * current game conditions.
   * 
   * @return The minimum amount of cash the player should have to avoid problems
   */
  protected int getMinimumCash() {
    // Frayn: Keep a minimum of 200 pounds (dollars) in cash,
    int result = 200;

    // plus 1% of the total and average opponent net worth,
    int totalnet = game.getTotalNetWorth();
    totalnet -= getTotalWorth();

    int count = game.getNumActivePlayers() - 1;

    int avgnet = totalnet / count;
    result += (int) (totalnet * 0.01);
    result += (int) (avgnet * 0.01);

    // plus 5% of the number of houses or hotels. (assume frayn meant 5% of
    // cost)
    PropertyFactory pf = PropertyFactory.getPropertyFactory(this.gameKey);
    for (int i = 0; i < 40; i++) {
      Location l = pf.getLocationAt(i);
      if (l.getNumHouses() > 0 && l.owner != this) {
        result += (int) (l.getNumHouses() * l.getHouseCost() * 0.05);
      }
      if (l.getNumHotels() > 0 && l.owner != this) {
        // multiply by 5 because hotels cost is hotelCost + 4 houses
        result += (int) (l.getNumHotels() * l.getHotelCost() * 0.05) * 5;
      }
    }

    return result;
  }

  /**
   * Take all cash away from the player (called during bankruptcy processing);
   * resets the player cash amount to 0.
   * 
   * @return The value of cash held by the player
   */
  public int getAllCash() {
    int amount = cash;
    cash = 0;
    fireChangeEvent();
    return amount;
  }

  /**
   * Set the bankrupt flag for this player
   */
  public void setBankrupt() {
    isBankrupt = true;
    fireChangeEvent();
  }

  /**
   * Sell all the hotels and houses owned by the player
   */
  public void sellAllHousesAndHotels() {
    for (Location l : owned.values()) {
      if (l.getNumHotels() > 0) {
        game.liquidateHotel(this, l);
      }

      assert l.getNumHouses() >= 0;
      if (l.getNumHouses() > 0)
        game.liquidateHouses(this, l);
    }

    fireChangeEvent();
  }

  /**
   * Attempt to buy a house for a property
   */
  public void processDevelopHouseEvent() {
    // Player has to have a monopoly
    if (!hasMonopoly()) {
      logFinest("Player does not have monopoly");
      return;
    }

    logFinest("Player has " + cash + " dollars");
    int minCash = getMinimumCash();
    logFinest("Player minimum cash is " + minCash);

    // Assume player will not try to raise cash for this. In most cases it
    // will not make sense for the player to sell houses or mortgage properties
    // to buy houses or hotels.
    if (cash < minCash) {
      logFinest("Player does not have minimum cash");
      return;
    }

    // Create a list of all the groups for which a player has a monopoly,
    // and for which no property in the group is mortgaged.
    Vector<PropertyGroups> groups = new Vector<PropertyGroups>();

    // Good strategy says to build all properties up to 3 houses first, and 
    // then if all monopolies of player have 3 houses, then build hotels.
    // need3Houses means some property in some group has less than 3 houses
    boolean need3Houses = false;

    for (Location aLocation : owned.values()) {
      // skip this property since the group is mortgaged
      if (aLocation.groupIsMortgaged(gameKey))
        continue;

      if (aLocation.partOfMonopoly) {
        if (!need3Houses && aLocation.getNumHotels() == 0
            && aLocation.getNumHouses() < 3)
          need3Houses = true;

        if (!groups.contains(aLocation.getGroup())) {
          groups.add(aLocation.getGroup());
          logFinest(aLocation.getGroup().toString()
              + " added to list of monopolies in processDevelopHouseEvent");
        }
      }
    }

    for (int i = 0; i < groupOrder.length; i++) {
      int houseCost = groupOrder[i].getHouseCost();
      if (cash < (getMinimumCash() + houseCost)) {
        logFinest("Player does not have " + houseCost
            + " dollars above minimum cash reserve to buy house/hotel for "
            + location.name);
        break;
      }
      
      for (PropertyGroups group : groups) {
        // Process properties in group order, so if location is not part of
        // current group, then skip it for now
        if (group == groupOrder[i]) {
          logFinest("Checking " + group + " for build decision");
          buyHousesForGroup(group, need3Houses);
        }
      }
    }
  }

  /**
   * Buy Houses or Hotels for a group. If need three houses is true, method will
   * try to buy until there are 3 houses on every location in the group. If
   * need3Houses is false, then buy a 4th house or a hotel.
   * 
   * @param group
   *          The group to buy houses for.
   * @param need3Houses
   *          If true --> buy up to 3 houses for each property in the group.<br>
   *          Otherwise --> buy a 4th house or a hotel for the properties in the
   *          group.
   */
  private void buyHousesForGroup(PropertyGroups group, boolean need3Houses) {
    // Best strategy is to buy in a particular order, either 312 or 321 or 21,
    // depending on the group, but for simplicity, always buy in 321 order (3rd 
    // property, 2nd property, then 1st property, in index order).
    boolean done = false;
    
    int numHousesBought = 0;
    int numHotelsBought = 0;

    while (!done) {
      int minHouses = Integer.MAX_VALUE;
      int minHotels = Integer.MAX_VALUE;
      Vector<Location> lots = new Vector<Location>();
      for (Location lot : owned.values()) {
        if (lot.getGroup() == group)
          lots.add(0, lot);
        else
          continue;

        if (lot.getNumHouses() < minHouses)
          minHouses = lot.getNumHouses();

        if (lot.getNumHotels() < minHotels)
          minHotels = lot.getNumHotels();
      }

      if (need3Houses && minHouses == 3)
        break;

      if (minHotels == 1) 
        break;

      if ((need3Houses && minHouses < 3) || 
          (!need3Houses && minHouses > 0 && minHouses < 4)) {
        for (Location lot : lots) {
          if (lot.getNumHouses() == minHouses) {
            if (cash >= (getMinimumCash() + lot.getHouseCost())) {
              int numBought = game.buyHouse(this, lot);
              numHousesBought += numBought;
            } else {
              done = true;
              break;
            }
          }
        }
      } else if (!need3Houses && (minHouses == 4 || minHouses == 0)) {
        // buy a hotel
        for (Location lot : lots) {
          if (lot.getNumHouses() == 4) {
            if (cash >= (getMinimumCash() + lot.getHotelCost())) {
              int numBought = game.buyHotel(this, lot);
              numHotelsBought += numBought;
            } else {
              done = true;
              break;
            }
          }
        }
      }
      // OTHER Conditions
      // need3Houses && minHouses == 3   --> some other group needs houses
      // need3Houses && minHouses > 3    --> should not happen
      // !need3Houses && minHouses > 4   --> should not happen
      // !need3Houses && minHouses 1,2,3 --> handled above
    }

    if (numHousesBought > 0) 
      logInfo(getName() + " bought " + numHousesBought
          + " houses for the property group " + group);
    if (numHotelsBought > 0) 
      logInfo(getName() + " bought " + numHotelsBought
          + " hotels for the property group " + group);
  }

  /**
   * @return The index which indicates in which order the player went bankrupt
   *         in a game: 0 for first, 1 for second, 2 for third, etc.
   */
  public Integer getBankruptIndex() {
    return Integer.valueOf(bankruptIndex);
  }

  /**
   * Set an index indicating which order the player went bankrupt, the first
   * player to go bankrupt in a game is assigned index 0, the second to go
   * bankrupt is index 1, and the third player to go bankrupt is 2, etc..
   * 
   * @param index
   *          The value to set.
   */
  public void setBankruptIndex(int index) {
    bankruptIndex = index;
  }

  /**
   * Print the chromosome for this player
   */
  public abstract void printGenome();

  /**
   * Compare players based on fitness score.
   */
  public int compareTo(AbstractPlayer arg0) {
    return Integer.valueOf(fitnessScore).compareTo(
        Integer.valueOf(arg0.fitnessScore));
  }

  /**
   * Set this player's index value
   * 
   * @param index
   *          The ID or index of the player
   */
  public void setIndex(int index) {
    playerIndex = index;
  }

  /**
   * Create a child player by recombination with another parent
   * 
   * @param parent2
   *          The parent to use in reproduction
   * @param index
   *          The index of the child player
   * @return An array containing the child players created by mating this player
   *         with the other parent player.
   */
  public abstract AbstractPlayer[] createChildren(AbstractPlayer parent2,
      int index);

  public String toString() {
    String separator = System.getProperty("line.separator");
    StringBuilder result = new StringBuilder(1024);
//    result.append("Player ").append(playerIndex+1)
    result.append(getName())
        .append(separator).append("  Total cash  : ").append(cash)
        .append(separator).append("  Net worth   : ").append(getTotalWorth())
//        .append(separator).append("  Fitness     : ").append(fitnessScore)
        .append(separator).append("  Has Monopoly: ").append(hasMonopoly())
        .append(separator).append("  Is Bankrupt : ").append(isBankrupt)
        .append(separator).append(separator);

    if (!owned.isEmpty()) {
      result.append("  Properties owned: ").append(separator);
      for (Location location : owned.values()) {
        result.append("    ").append(location.getFullInfoString())
            .append(separator);
      }
    }
    
    if (hasGetOutOfJailCard()) {
      result.append(separator);
      if (ccGOOJ != null) 
        result.append("Community Chest: ").append(ccGOOJ.toString()).append(separator);
      if (this.chanceGOOJ != null)
        result.append("Chance: ").append(chanceGOOJ.toString()).append(separator);
    }

    return result.toString();
  }

  /**
   * Copy this player and optionally mutate the new player as well.
   * 
   * @return
   */
  public abstract AbstractPlayer copyAndMutate();

  @Override
  public abstract Object clone() throws CloneNotSupportedException;

  /**
   * Join the given game with this player.
   * 
   * @param game
   */
  public void joinGame(Monopoly game) {
    this.game = game;
    setGameKey(game.gamekey);

    propertyTrader = new PropertyNegotiator(this, this.gameKey);
    resetAll();
    location = PropertyFactory.getPropertyFactory(this.gameKey).getLocationAt(
        locationIndex);
  }

  /**
   * Log a string to the debug log using INFO level
   * 
   * @param s
   *          The string to log
   */
  public void logInfo(String s) {
    if (game != null) {
      game.logInfo(s);
    }
  }

  /**
   * Log a string to the debug log using FINEST level
   * 
   * @param s
   *          The string to log
   */
  public void logFinest(String s) {
    if (game != null) {
      game.logFinest(s);
    }
  }

  /**
   * @return The number of properties owned by the player
   */
  public int getNumProperties() {
    return owned.size();
  }

  /**
   * @return The number of monopolies controlled by this player
   */
  public int getNumMonopolies() {
    int result = 0;
    PropertyGroups lastGroup = PropertyGroups.SPECIAL;

    for (Location l : owned.values()) {
      if (l.partOfMonopoly && l.getGroup() != lastGroup) {
        ++result;
        lastGroup = l.getGroup();
      }
    }

    return result;
  }

  public void setGameNetWorth(int totalNetWorth) {
    gameNetWorth = totalNetWorth;
  }

  public int getGameNetWorth() {
    return gameNetWorth;
  }

  /**
   * Determine if this player can use the given location to increase the chances
   * of getting monopoly.
   * 
   * @param location2
   *          The location to check
   * @return True if the player owns another property in the same group as
   *         location
   */
  public boolean needs(Location location2) {
    PropertyGroups group = location2.getGroup();
    for (Location location : owned.values())
      if (location.getGroup() == group)
        return true;

    return false;
  }

  public int evaluateTrade(TradeProposal trade) {
    int base = propertyTrader.evaluateOwnersHoldings();
    int newVal = propertyTrader.evaluateOwnersHoldings(trade);

    return newVal - base;
  }

  /**
   * A method to make trading decisions and act on those decisions.
   * @param locations The properties owned by other players that this player
   * can trade for.
   */
  public void processTradeDecisionEvent(ArrayList<Location> locations) {
    // only try to trade if this player owns at least one propertie
    // and other players own at least one property
    if (owned.size() > 0 && !locations.isEmpty()) {
      TradeProposal bestTrade = propertyTrader.findBestTrade();

      if (bestTrade != null) {
        assert bestTrade.cashDiff <= cash;
        game.proposeTrade(bestTrade);
      }
    }
  }

  public boolean answerProposedTrade(TradeProposal bestTrade) {
    logInfo(getName() + " is evaluating trade proposal from "
        + bestTrade.getProposer() + "\n" + bestTrade.toString());

    // if the player has to give too much money, the reject the trade
    if (cash + bestTrade.cashDiff < getMinimumCash()) {
      logFinest("Trade would reduce cash of " + getName()
          + " below minimum");
      logInfo("Trade refused");
      return false;
    }

    // If the estimated profit from the trade is less than threshold, then
    // reject the trade.
    int baseValue = propertyTrader.evaluateOwnersHoldings();
    int newValue = propertyTrader.evaluateOwnersHoldings(bestTrade);
    
    int profit = newValue - baseValue + bestTrade.cashDiff;
    
    if (profit < tradeThreshold) {
      logFinest("Trade does not exceed profit threshold");
      logInfo("Trade refused");
      return false;
    }

    logInfo(getName() + ": trade accepted");
    return true;
  }

  /**
   * Return a list of all the players in the game that this player is in.
   * Includes this player
   * 
   * @return An AbstractPlayer array that contains all the players in a game
   */
  public AbstractPlayer[] getAllPlayers() {
    return game.getAllPlayers();
  }

  public void setGameKey(String factoryKey)
  {
    gameKey = factoryKey;    
  }

  public void payIncomeTax() throws BankruptcyException {
    int totalWorth = getTotalWorth();
    if (totalWorth < 2000) {
      logInfo(getName() + " chooses to pay 10%");
      getCash(totalWorth / 10);
    } else {
      logInfo(getName() + " chooses to pay $200");
      getCash(200);
    }
  }
  
  public void addChangeListener(ChangeListener cl) {
    this.changeListener = cl;
  }
  
  private void fireChangeEvent() {
    if (changeListener != null) 
      changeListener.stateChanged(new ChangeEvent(this));
  }
}
