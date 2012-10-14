package edu.uccs.ecgs.ga;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * A class to assist in negotiating properties in the game of Monopoly. The
 * algorithms used in this class come from the paper
 * "Negotiation strategy of agents in the Monopoly game" by Yasamura, Oguchi,
 * and Nitta.
 * 
 * https://ieeexplore.ieee.org/xpl/articleDetails.jsp?tp=&arnumber=1013210
 */
public class PropertyNegotiator {

  private String gamekey;
  private AbstractPlayer[] players;
  private AbstractPlayer owner;
  private TreeMap<PropertyGroups, ArrayList<Location>> locationGroups;
  private static double[] stProbs = new double[] { 0.0, 0.0, 1.0 / 36.0,
      2.0 / 36.0, 3.0 / 36.0, 4.0 / 36.0, 5.0 / 36.0, 6.0 / 36.0, 5.0 / 36.0,
      4.0 / 36.0, 3.0 / 36.0, 2.0 / 36.0, 1.0 / 36.0 };

  private static TreeMap<PropertyGroups, Double> mtProbs;

  public PropertyNegotiator(AbstractPlayer player, String gamekey) {
    this.owner = player;
    this.gamekey = gamekey;
    
    locationGroups = new TreeMap<PropertyGroups, ArrayList<Location>>();
    initLocationGroups();

    mtProbs = new TreeMap<PropertyGroups, Double>();

    mtProbs.put(PropertyGroups.RAILROADS, 64.0);
    mtProbs.put(PropertyGroups.ORANGE, 50.0);
    mtProbs.put(PropertyGroups.RED, 49.0);
    mtProbs.put(PropertyGroups.YELLOW, 45.0);
    mtProbs.put(PropertyGroups.GREEN, 44.0);
    mtProbs.put(PropertyGroups.PURPLE, 43.0);
    mtProbs.put(PropertyGroups.LIGHT_BLUE, 39.0);
    mtProbs.put(PropertyGroups.UTILITIES, 32.0);
    mtProbs.put(PropertyGroups.DARK_BLUE, 27.0);
    mtProbs.put(PropertyGroups.BROWN, 24.0);
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
        index += i;
        index %= 40;
        playerProbs[index] = stProbs[i];
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

      ArrayList<Location> locationGroup = locationGroups.get(group);
      for (Location location : locationGroup) {
        double prob = mtProbs.get(group).doubleValue();

        // dice roll is only used for utilities, so use average roll of 7
        int diceRoll = 7;
        double rent = (double) location.getPotentialRent(numHouses, diceRoll);

        mtGain += prob * rent;
      }
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
    return computeMidTermGain(group, 1000);
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
  public int evaluateProperty(Location aLocation)
  {
    int selfOwned = 0;
    AbstractPlayer owner1 = null;

    PropertyGroups group = aLocation.getGroup();
    int cash = owner.cash;

    ArrayList<Location> locationGroup = locationGroups.get(group);

    // figure out how many players own properties in the group
    for (Location location : locationGroup) {
      AbstractPlayer player = location.getOwner();
      if (player == null)
        continue;

      if (owner == player)
        ++selfOwned;
      else
        owner1 = owner;
    }

    int propertyValue = 0;
    if (selfOwned == 1) {
      // Case 1 - owner will trade to get the first property in group
      // f(c) = 0.5 * FV + ltGain
      propertyValue = getCase1Value(aLocation);
    } else if (selfOwned == 2) {
      if (owner1 == null) {
        // Case 2 - no other player owns a property in the group
        // f(c) = 2 * Case 1 value + mtGain
        propertyValue = getCase2Value(aLocation);
      } else {
        // Case 3 - 1 other player owns a property in the group
        // f(c) = case 2 value + stgain
        propertyValue = getCase2Value(aLocation)
            + computeShortTermGain(group, cash);
      }
    } else if (selfOwned == 3) {
      // Case 4 - owner has a monopoly
      // f(c) = 3 * case 1 value + alpha * (mtGain + stGain)
      // paper does not define alpha, so arbitrarily set it to 2
      propertyValue = 3
          * getCase1Value(aLocation)
          + 2
          * (computeMidTermGain(group, cash) + computeShortTermGain(group, cash));
    }

    return propertyValue;
  }

  /**
   * @param aLocation
   * @return
   */
  private int getCase1Value(Location aLocation)
  {
    return aLocation.getCost() / 2 + computeLongTermGain(aLocation.getGroup());
  }

  /**
   * @param aLocation
   * @return
   */
  private int getCase2Value(Location aLocation)
  {
    return 2 * getCase1Value(aLocation)
        + computeMidTermGain(aLocation.getGroup(), owner.cash);
  }
  
  /**
   * this is the big U eval function from the paper, but this is the baseline
   * calculation<br>
   * U = w1 * (sum(f(c)) + M) <br>
   *
   */
  public int evaluateOwnersHoldings() {
    TreeMap<Integer, Location> owned = owner.getAllProperties();
    double bigU = 0.0;
    for (Location location : owned.values()) {
      bigU += evaluateProperty(location);
    }
    
    bigU += owner.cash;
    bigU *= owner.w1;
    
    return (int) bigU;
  }
  
  /**
   * this is the big U eval function from the paper, but now compute the
   * players holdings based on a possible trade<br>
   * U = w1 * (sum(f(c)) + M) - w2 * (stLoss + mtLoss)<br>
   *
   */
  public int evaluateOwnersHoldings(Location losing, Location gaining) {
    // This seems dangerous to me, but this is the quick and dirty solution
    // we reset the owners as if a trade has been made, so we can evaluate
    // the result of the trade. At the end of the method these will be reset.
    AbstractPlayer originalOwner = gaining.getOwner();

    tradeProperties(losing, gaining);
    
    double bigU = evaluateOwnersHoldings();
    
    bigU -= owner.w2
        * ((double) (computeShortTermLoss(gaining) + computeMidTermLoss(gaining)));
    
    tradeProperties(gaining, losing);
    
    assert losing.getOwner() == owner;
    assert gaining.getOwner() == originalOwner;
    assert owner.getAllProperties().containsValue(losing);
    assert !owner.getAllProperties().containsValue(gaining);
    assert originalOwner.getAllProperties().containsValue(gaining);
    assert !originalOwner.getAllProperties().containsValue(losing);

    return (int) bigU;
  }

  /**
   * Trade the given properties between the owner of this PropertyNegotiator
   * and the player who owns "gaining" 
   * @param losing The property that owner is trading away
   * @param gaining The property that owner is receiving
   */
  private void tradeProperties(Location losing, Location gaining)
  {
    AbstractPlayer otherPlayer = gaining.getOwner();
    losing.setOwner(otherPlayer);
    otherPlayer.getAllProperties().put(losing.index, losing);
    otherPlayer.getAllProperties().remove(gaining.index);
    
    gaining.setOwner(owner);
    owner.getAllProperties().put(gaining.index, gaining);
    owner.getAllProperties().remove(losing.index);
  }
  
  /**
   * Create a list of all possible property trades between owner and the
   * other players  
   */
  public ArrayList<TradeProposal> getTradeProposals() {
    ArrayList<Location> otherProperties = new ArrayList<Location> ();
    ArrayList<Location> ownerProperties = new ArrayList<Location> ();
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
        //no point in trading two properties in the same group
        if (location.getGroup() == location2.getGroup())
          continue;
        
        //and only trade if each player needs the other property
        AbstractPlayer owner2 = location2.getOwner();
        if (!owner.needs(location2) || !owner2.needs(location)) 
          continue;

        tradeProposals.add(new TradeProposal(location, location2));
      }
    }
    
    return tradeProposals;
  }
  
  /**
   * Evaluate all possible trades, and return the trade with the highest
   * profit.
   * @return The trade with the highest profit.
   */
  public TradeProposal findBestTrade() {
    TradeProposal bestTrade = null;
    ArrayList<TradeProposal> proposals = getTradeProposals();
    int base = evaluateOwnersHoldings();
    int gain = Integer.MIN_VALUE;

    for (TradeProposal trade : proposals) {
      int newVal = evaluateOwnersHoldings(trade.location, trade.location2);
      int ownerProfit = newVal - base;
      int agentProfit = trade.location2.getOwner().evaluateTrade(trade);
      int cashDiff = ownerProfit - agentProfit;
      if (ownerProfit - cashDiff > gain) {
        bestTrade = trade;
        bestTrade.setCash(cashDiff);
        bestTrade.setProfit(agentProfit);
      }
    }

    return bestTrade;
  }
}
