package edu.uccs.ecgs.ga;

import java.util.ArrayList;
import java.util.TreeMap;
import edu.uccs.ecgs.players.AbstractPlayer;

/**
 * A class to assist in negotiating properties in the game of Monopoly. The
 * algorithms used in this class come from the paper
 * "Negotiation strategy of agents in the Monopoly game" by Yasamura, Oguchi,
 * and Nitta.
 * 
 * https://ieeexplore.ieee.org/xpl/articleDetails.jsp?tp=&arnumber=1013210
 */
public class PropertyNegotiator {

  private AbstractPlayer[] players;
  private AbstractPlayer owner;
  private TreeMap<PropertyGroups, ArrayList<Location>> locationGroups;
  private String gamekey;
  private int alpha = 1;
  private static double[] stProbs = new double[] { 0.0, 0.0, 1.0 / 36.0,
      2.0 / 36.0, 3.0 / 36.0, 4.0 / 36.0, 5.0 / 36.0, 6.0 / 36.0, 5.0 / 36.0,
      4.0 / 36.0, 3.0 / 36.0, 2.0 / 36.0, 1.0 / 36.0 };

  private TreeMap<PropertyGroups, Double> mtProbs;

  private enum GroupType {
    GROUP_2, GROUP_3, GROUP_4, NULL
  };

  public PropertyNegotiator(AbstractPlayer player, String gamekey) {
    this.owner = player;
    this.gamekey = gamekey;

    players = owner.getAllPlayers();

    locationGroups = new TreeMap<PropertyGroups, ArrayList<Location>>();
    initLocationGroups();

    mtProbs = new TreeMap<PropertyGroups, Double>();

    mtProbs.put(PropertyGroups.RAILROADS, 0.64);
    mtProbs.put(PropertyGroups.ORANGE, 0.50);
    mtProbs.put(PropertyGroups.RED, 0.49);
    mtProbs.put(PropertyGroups.YELLOW, 0.45);
    mtProbs.put(PropertyGroups.GREEN, 0.44);
    mtProbs.put(PropertyGroups.PURPLE, 0.43);
    mtProbs.put(PropertyGroups.LIGHT_BLUE, 0.39);
    mtProbs.put(PropertyGroups.UTILITIES, 0.32);
    mtProbs.put(PropertyGroups.DARK_BLUE, 0.27);
    mtProbs.put(PropertyGroups.BROWN, 0.24);
  }

  private void initLocationGroups()
  {
    Location[] locations = PropertyFactory.getPropertyFactory(gamekey)
        .getLocations();
    for (Location location : locations) {
      ArrayList<Location> locationGroup = locationGroups.get(location
          .getGroup());
      if (locationGroup == null) {
        locationGroup = new ArrayList<Location>();
        locationGroups.put(location.getGroup(), locationGroup);
      }
      locationGroup.add(location);
    }
  }

  /**
   * Expected short term gain is the sum of probability that a player will land
   * on group in one dice throw multiplied by the rental fee of the color group
   * given cash.
   * 
   * @param group
   *          The group for which to compute the short term gain
   * @param cash
   *          The cash on hand of the player which could be used to buy houses
   *          on a street location
   * @return The Expected Short Term Gain for getting a monopoly in the given
   *         group with the given amount of cash.
   */
  private int computeShortTermGain(PropertyGroups group, int cash)
  {
    double stGain = 0.0;

    // get the number of houses that could be built with the cash
    int numHouses = group.getNumHouses(cash);

    for (AbstractPlayer player : players) {
      if (owner == player)
        continue;

      int playerLocation = player.getCurrentLocation().index;
      // if player is in jail, then skip
      if (playerLocation == 10)
        continue;

      // create an array that lists the probabilities for the player
      // starting with a dice roll
      int index = playerLocation;

      double[] playerProbs = new double[40];
      for (int i = 0; i < 13; i++) {
        int newIndex = index + i;
        newIndex %= 40;
        playerProbs[newIndex] = stProbs[i];
      }

      ArrayList<Location> locationGroup = locationGroups.get(group);
      for (Location location : locationGroup) {
        // determine probability of landing on location
        double prob = playerProbs[location.index];

        int diceRoll = 0;
        if (prob > 0)
          diceRoll = location.index - playerLocation;

        if (diceRoll < 0)
          diceRoll += 40;

        double rent = (double) location.getPotentialRent(numHouses, diceRoll);

        stGain += prob * rent;
      }
    }

    return (int) stGain;
  }

  /**
   * Expected mid term gain is the sum of probability that a player will land on
   * group in one trip around the board multiplied by the rental fee of the
   * color group given cash. Probabilities for landing on a group in one trip
   * around the board are in the research paper.
   * 
   * @param group
   *          The group for which to compute the mid term gain
   * @param cash
   *          The cash on hand of the player which could be used to buy houses
   *          on a street location
   * @return The Expected Mid Term Gain for getting a monopoly in the given
   *         group with the given amount of cash.
   */
  private int computeMidTermGain(PropertyGroups group, int cash)
  {
    double mtGain = 0.0;

    // get the number of houses that could be built with the cash
    int numHouses = group.getNumHouses(cash);

    for (AbstractPlayer player : players) {
      if (owner == player)
        continue;

      int playerLocation = player.getCurrentLocation().index;
      // if player is in jail, then skip
      if (playerLocation == 10)
        continue;

      Double d = mtProbs.get(group);
      double prob = d.doubleValue();

      // dice roll is only used for utilities, so use average roll of 7
      int diceRoll = 7;
      // Get the first location in the group for rent query
      ArrayList<Location> locationGroup = locationGroups.get(group);
      Location location = locationGroup.get(0);
      double rent = (double) location.getPotentialRent(numHouses, diceRoll);

      mtGain += prob * rent;
    }

    return (int) mtGain;
  }

  /**
   * Expected long term gain is the mid term gain assuming the player has $1000
   * in cash
   * 
   * @param group
   *          The group for which to compute the mid term gain
   * @return The Expected Mid Term Gain for getting a monopoly in the given
   *         group with $1000 of cash.
   */
  private int computeLongTermGain(PropertyGroups group)
  {
    int ltGain = computeMidTermGain(group, 1000);
    return ltGain;
  }

  /**
   * The potential loss is the probability of landing on the location in one
   * dice roll multiplied by the rent they would have to pay if they land on the
   * location.
   * 
   * @param location
   * @return The potential loss to the player if they do not trade for the given
   *         property.
   */
  private int computeShortTermLoss(Location location)
  {
    double stLoss = 0.0;

    int playerLocation = owner.getCurrentLocation().index;

    // create an array that lists the probabilities for the player
    // starting with a dice roll
    int index = playerLocation;

    double[] playerProbs = new double[40];
    for (int i = 0; i < 13; i++) {
      index += i;
      index %= 40;
      playerProbs[index] = stProbs[i];
    }

    double prob = playerProbs[location.index];

    int diceRoll = 0;
    if (prob > 0)
      diceRoll = location.index - playerLocation;

    if (diceRoll < 0)
      diceRoll += 40;

    double rent = (double) location.getPotentialRent(0, diceRoll);

    stLoss += prob * rent;

    return (int) stLoss;
  }

  /**
   * The potential loss is the probability of landing on the location in one
   * trip around the board multiplied by the rent they would have to pay if they
   * land on the location.
   * 
   * @param location
   * @return The potential loss to the player if they do not trade for the given
   *         property.
   */
  private int computeMidTermLoss(Location location)
  {
    double mtLoss = 0.0;

    double prob = mtProbs.get(location.getGroup()).doubleValue();

    // dice roll is only used for utilities, so use average roll of 7
    int diceRoll = 7;
    double rent = (double) location.getPotentialRent(0, diceRoll);

    mtLoss += prob * rent;

    return (int) mtLoss;
  }

  /**
   * Evaluate a property on how valuable it would be for the player to gain that
   * property<br>
   * 
   * @param location
   */
  public int evaluateProperty(PropertyGroups group)
  {
    int selfOwned = 0;
    int otherOwned = 0;

    GroupType groupType = getGroupType(group);

    int cash = owner.cash;

    ArrayList<Location> locationGroup = locationGroups.get(group);

    // figure out how many players own properties in the group
    for (Location aLocation : locationGroup) {
      AbstractPlayer player = aLocation.getOwner();
      if (player == null)
        continue;

      if (owner == player)
        ++selfOwned;
      else
        ++otherOwned;
    }

    int propertyValue = 0;
    for (Location aLocation : locationGroup) {
      switch (groupType) {
      case GROUP_2:
        // Property Group 2 is the groups that have only two properties: BROWN,
        // DARK BLUE, and UTILITIES. There are three ownership situations to
        // deal with here: 1-0, 1-1, and 2-0
        switch (selfOwned) {
        case 1:
          if (otherOwned == 0) {
            // ownership: 1-0
            propertyValue = getCase1Value(aLocation);
          } else {
            // ownership: 1-1
            propertyValue = getCase2Value(aLocation)
                + computeShortTermGain(group, cash);
          }
          break;
        case 2:
          // ownership: 2-0
          propertyValue = (3 * getCase1Value(aLocation))
              + alpha
              * (computeMidTermGain(group, cash) + computeShortTermGain(group,
                  cash));
          break;
        default:
          propertyValue = 0;
          break;
        }
        break;
      case GROUP_3:
        // Property Group 3 is all the groups that have three properties. There
        // are four ownership situations to
        // deal with here: 1-0-0, 1-1-0, 1-1-1, 1-2-0 (Case 1)
        // 2-0-0 (Case 2)
        // 2-1-0, and (Case 3)
        // 3-0-0 (Case 4)
        // See the Yasamura paper for more details.
        switch (selfOwned) {
        case 1:
          // ownership: 1-0-0, 1-1-0, 1-1-1, 1-2-0
          propertyValue = getCase1Value(aLocation);
          break;
        case 2:
          if (otherOwned == 0) {
            // ownership: 2-0-0
            propertyValue = getCase2Value(aLocation);
          } else {
            // ownership: 2-1-0
            propertyValue = getCase2Value(aLocation)
                + computeShortTermGain(group, cash);
          }
          break;
        case 3:
          // ownership: 3-0-0
          propertyValue = (3 * getCase1Value(aLocation))
              + alpha
              * (computeMidTermGain(group, cash) + computeShortTermGain(group,
                  cash));
          break;
        default:
          propertyValue = 0;
          break;
        }
        break;
      case GROUP_4:
        // Property Group 4 is the railroads where there are four properties.
        // There are four ownership situations to
        // deal with here: 1-x-x-x (Case 1)
        // 2-0-0, 2-1-0, 3-0-0 (Case 2)
        // 2-2-0, 3-1-0 (Case 3)
        // 4-0-0 (Case 4)
        // See the Yasamura paper for more details.
        switch (selfOwned) {
        case 1:
          // ownership: 1-x-x-x
          propertyValue = getCase1Value(aLocation);
          break;
        case 2:
          if (otherOwned == 0 || otherOwned == 1) {
            // ownership: 2-0-0 or 2-1-0
            propertyValue = getCase2Value(aLocation);
          } else if (otherOwned == 2) {
            // ownership: 2-2-0
            propertyValue = getCase2Value(aLocation)
                + computeShortTermGain(group, cash);
          }
          break;
        case 3:
          // ownership: 3-0-0
          if (otherOwned == 0) {
            propertyValue = getCase2Value(aLocation);
          } else {
            // ownership: 3-1-0
            propertyValue = getCase2Value(aLocation)
                + computeShortTermGain(group, cash);
          }
        case 4:
          // ownership: 4-0-0-0
          propertyValue = (3 * getCase1Value(aLocation))
              + alpha
              * (computeMidTermGain(group, cash) + computeShortTermGain(group,
                  cash));
          break;
        default:
          propertyValue = 0;
          break;
        }
        break;
      default:
        propertyValue = 0;
        break;
      }
    }

    return propertyValue;
  }

  private GroupType getGroupType(PropertyGroups group)
  {
    switch (group) {
    case LIGHT_BLUE:
    case PURPLE:
    case RED:
    case ORANGE:
    case YELLOW:
    case GREEN:
      return GroupType.GROUP_3;

    case BROWN:
    case DARK_BLUE:
    case UTILITIES:
      return GroupType.GROUP_2;

    case RAILROADS:
      return GroupType.GROUP_4;

    default:
      return GroupType.NULL;
    }
  }

  /**
   * @param aLocation
   * @return
   */
  private int getCase1Value(Location aLocation)
  {
    int result = aLocation.getCost() / 2
        + computeLongTermGain(aLocation.getGroup());
    return result;
  }

  /**
   * @param aLocation
   * @return
   */
  private int getCase2Value(Location aLocation)
  {
    int result = 2 * getCase1Value(aLocation)
        + computeMidTermGain(aLocation.getGroup(), owner.cash);
    return result;
  }

  /**
   * this is the big U eval function from the paper, but this is the baseline
   * calculation<br>
   * U = w1 * (sum(f(c)) + M) <br>
   * 
   */
  public int evaluateOwnersHoldings()
  {
    double bigU = 0.0;
    for (PropertyGroups group : PropertyGroups.values()) {
      bigU += evaluateProperty(group);
    }

    bigU += owner.cash;
    bigU *= owner.w1;

    return (int) bigU;
  }

  /**
   * Create a list of all possible property trades between owner and the other
   * players
   */
  public ArrayList<TradeProposal> getTradeProposals()
  {
    ArrayList<Location> otherProperties = new ArrayList<Location>();
    ArrayList<Location> ownerProperties = new ArrayList<Location>();
    ArrayList<TradeProposal> tradeProposals = new ArrayList<TradeProposal>();

    // start by getting all the properties owned by other players
    for (AbstractPlayer player : players) {
      if (owner == player)
        ownerProperties.addAll(player.getAllProperties().values());
      else
        otherProperties.addAll(player.getAllProperties().values());
    }

    for (Location location : ownerProperties) {
      for (Location location2 : otherProperties) {
        // no point in trading two properties in the same group
        if (location.getGroup() == location2.getGroup())
          continue;

        // and only trade if each player needs the other property
        AbstractPlayer owner2 = location2.getOwner();
        if (!owner.needs(location2) || !owner2.needs(location))
          continue;

        assert !location.getGroup().equals(location2.getGroup());
        assert location.getGroup() != location2.getGroup();
        tradeProposals.add(new TradeProposal(location, location2));
      }
    }

    return tradeProposals;
  }

  /**
   * Evaluate all possible trades, and return the trade with the highest profit.
   * 
   * @return The trade with the highest profit.
   */
  public TradeProposal findBestTrade()
  {
    TradeProposal bestTrade = null;
    ArrayList<TradeProposal> proposals = getTradeProposals();
    
    int base = evaluateOwnersHoldings();

    if (proposals.size() > 0) {
      owner.logFinest("All proposals: ");
      for (TradeProposal proposal : proposals) {
        owner.logFinest("    " + proposal);
      }

      owner.logFinest("Base value of owner is " + base);
    }

    int gain = Integer.MIN_VALUE;

    for (TradeProposal trade : proposals) {
      int newVal = evaluateOwnersHoldings(trade);
      owner.logFinest("\nNew val after trading " + trade.location + " for "
          + trade.location2 + " is " + newVal);

      int ownerProfit = newVal - base;
      owner.logFinest("Owner profit is " + ownerProfit);
      
      ownerProfit = owner.evaluateTrade(trade);
      owner.logFinest("Owner profit is " + ownerProfit);

      int agentProfit = trade.location2.getOwner().evaluateTrade(trade);
      owner.logFinest("Agent profit is " + agentProfit);
      
      int cashDiff = ownerProfit - agentProfit;
      owner.logFinest("Cash difference is " + cashDiff);

      int addCashToTrade = cashDiff/2;
      
      owner.logFinest("Sweetener for trade is 1/2 cash diff: " + addCashToTrade);

      int ownerAdjProfit = ownerProfit - addCashToTrade;
      int agentAdjProfit = agentProfit + addCashToTrade;
      
      owner.logFinest("Owner adjusted profit is " + ownerAdjProfit);
      owner.logFinest("Agent adjusted profit is " + agentAdjProfit);

      // if this is a better gain...
      if (ownerAdjProfit > gain) {
        // and if the player has the cash...
        if (owner.cash > cashDiff) {
          //then make this the current best trade
          gain = ownerAdjProfit;
          bestTrade = trade;
          bestTrade.setCash(addCashToTrade);
          bestTrade.setProfit(ownerAdjProfit);
          owner.logFinest("Best trade is " + trade);
        } else {
          owner.logFinest("Owner does not have enough cash; "
              + "no change to best trade");
        }
      } else {
        owner.logFinest("Not better than current best trade; no change to best trade");
      }
    }

    return bestTrade;
  }

  /**
   * Evaluate the trade from the perspective of the player identified by the
   * instance field owner. This is the big U eval function from the paper, but
   * now compute the players holdings based on a possible trade<br>
   * U = w1 * (sum(f(c)) + M) - w2 * (stLoss + mtLoss)<br>
   * 
   * @param trade
   *          The proposed trade
   * @return A numeric value that represents the potential profit to this.owner
   *         from making this trade.
   */
  public int evaluateOwnersHoldings(TradeProposal trade)
  {
    Location location1 = null;
    Location location2 = null;
    int startCash = owner.cash;

    if (trade.location.getOwner() == owner) {
      location2 = trade.location;
      location1 = trade.location2;
      owner.cash -= trade.cashDiff;
    } else {
      location1 = trade.location;
      location2 = trade.location2;
      owner.cash += trade.cashDiff;
    }

    AbstractPlayer owner1 = location1.getOwner();
    AbstractPlayer owner2 = location2.getOwner();

    location1.setOwnerForTradeEvaluation(owner2);
    location2.setOwnerForTradeEvaluation(owner1);
    
    double bigU = evaluateOwnersHoldings();

    bigU -= owner.w2
        * ((double) (computeShortTermLoss(location1) 
            + computeMidTermLoss(location1)));

    location1.setOwnerForTradeEvaluation(owner1);
    location2.setOwnerForTradeEvaluation(owner2);
    owner.cash = startCash;

    assert owner.cash == startCash;
    assert location2.getOwner() == owner2;
    assert location1.getOwner() == owner1;
    assert owner2.getAllProperties().containsValue(location2);
    assert !owner2.getAllProperties().containsValue(location1);
    assert owner1.getAllProperties().containsValue(location1);
    assert !owner1.getAllProperties().containsValue(location2);

    return (int) bigU;
  }
}
