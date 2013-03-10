package edu.uccs.ecgs.ga;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;

import edu.uccs.ecgs.players.AbstractPlayer;
import edu.uccs.ecgs.states.Events;

public class Monopoly implements Runnable, Controllable {

  private Logger logger;

  boolean done = false;

  private int generation;
  private int match;
  private int game;

  int playerIndex = 0;
  Dice dice = Dice.getDice();
  int turnCounter = 0;
  Random r;
  private DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss.SS");
  
  /**
   * The players for this game.
   */
  private AbstractPlayer[] players;

  /**
   * The Chance and Community Chest cards.
   */
  private Cards cards;
  private int bankruptCount;

  private int numHouses;
  private int numHotels;

  public String gamekey;
  private long seed;
  private boolean paused = false;

  private Controllable flowController = this;

  /**
   * Constructor
   * @param generation Generation number for this game.
   * @param match Match number for this game.
   * @param gameNumber Game number for this game.
   * @param players Array of players for this game.
   */
  public Monopoly(int generation, int match, int gameNumber,
      AbstractPlayer[] players) {
    this.generation = generation;
    this.match = match;
    this.game = gameNumber;
    this.players = players;

    gamekey = "edu.uccs.ecgs.ga." + this.generation + "." + this.match + "."
        + game;

    r = new Random();
    seed = 1241797664697L;
    if (Main.useRandomSeed) {
      seed = System.currentTimeMillis();
    }
    r.setSeed(seed);

    turnCounter = 0;
    setNumHouses(32);
    numHotels = 12;

    cards = Cards.getCards();
  }

  public void setFlowController(Controllable flowController) {
    this.flowController = flowController;
  }

  @Override
  public void run() {
    try {
      playGame();
    } catch (Throwable t) {
      t.printStackTrace();
      StackTraceElement[] ste = t.getStackTrace();
      logSevere(t.toString());
      for (StackTraceElement s : ste) {
        logSevere(s.toString());
      }
    } finally {
      endGame();
    }
  }

  /**
   * Play the game
   */
  private void playGame() {
    done = false;

    logFinest("Started game " + this.generation + "." + this.match + "."
        + this.game + " with players: ");
    for (AbstractPlayer p : players) {
      logFinest(p.getName());
    }

    for (AbstractPlayer player : players) {
      player.joinGame(this);
    }

    logFinest("Game seed: " + seed);

    bankruptCount = 0;

    while (!done) {

      synchronized (this) {
        if (flowController.isPaused()) {
          try {
            wait();
            if (done)
              break;
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }

      ++turnCounter;
      if (turnCounter == Main.maxTurns * Main.numPlayers) {
        done = true;
      }

      AbstractPlayer player = getNextPlayer();
      player.resetDoubles();

      logInfo("****************************************");
      logInfo("           START OF TURN " + turnCounter);
      logInfo("****************************************");
      logInfo(player.toTinyString());

      Events event = Events.PLAYER_ACTIVATED_EVENT;
      Actions action = Actions.NULL;

      while (action != Actions.DONE) {
        action = player.getNextActionEnum(event);
        
        if (bankruptCount == Main.numPlayers - 1) {
          break;
        }
        
        switch (action) {
        case ROLL_DICE:
          event = Events.ROLL_DICE_EVENT;
          break;

        case PAY_BAIL:
          event = Events.PAY_BAIL_EVENT;
          break;

        case PAY_RENT:
          event = Events.PAY_RENT_EVENT;
          break;

        case EVAL_PROPERTY:
          event = Events.EVAL_PROPERTY_EVENT;
          break;

        case BUY_PROPERTY:
          event = Events.BUY_PROPERTY_EVENT;
          break;

        case DECLINE_PROPERTY:
          event = Events.DECLINE_PROPERTY_EVENT;
          break;

        case AUCTION_BID:
          event = Events.AUCTION_STARTED_EVENT;
          break;

        case AUCTION_WON:
          event = Events.WON_AUCTION_EVENT;
          break;

        case AUCTION_LOST:
          event = Events.LOST_AUCTION_EVENT;
          break;

        case PROCESS_SPECIAL_ACTION:
          event = Events.PROCESS_SPECIAL_ACTION_EVENT;
          break;

        case MAKE_BUILD_DECISION:
          event = Events.DEVELOP_DECISION_EVENT;
          break;

        case MAKE_MORTGAGE_DECISION:
          event = Events.MORTGAGE_DECISION_EVENT;
          break;

        case MAKE_TRADE_DECISION:
          event = Events.TRADE_DECISION_EVENT;
          break;

        case DONE:
          break;
        case NULL:
          break;

        default:
          throw new IllegalArgumentException("Unhandled action " + action);
        }
      }

      if (bankruptCount == Main.numPlayers - 1) {
        done = true;
      }

      logInfo("");
      logInfo(player.toTinyString());
      logInfo("");
    }

    assert done;

    ArrayList<AbstractPlayer> sortedPlayers = new ArrayList<AbstractPlayer>();

    int totalNetWorth = 0;
    
    for (AbstractPlayer p : players) {
      int playerNetWorth = p.getTotalWorth();
      totalNetWorth += playerNetWorth;
      sortedPlayers.add(p);
    }
    
    Collections.sort(sortedPlayers, NetWorthComparator.get());

    logInfo('\f' + "GAME OVER");
    assert totalNetWorth > 0;

    // In order from loser to winner, add score points to player
    int score = Main.numPlayers;
    for (AbstractPlayer p : sortedPlayers) {
      p.setFinishOrder(score);
      p.setGameNetWorth(totalNetWorth);
      score--;
    }

    for (AbstractPlayer p : sortedPlayers) {
      logInfo("");
      logInfo(p.toString());
    }    
  }

  /**
   * Get a reference to the player whose turn it is to play.
   * @return The player who is next to play.
   */
  private AbstractPlayer getNextPlayer() {
    while (players[playerIndex].bankrupt()) {
      playerIndex = ++playerIndex % 4;
    }

    AbstractPlayer p = players[playerIndex];
    playerIndex = ++playerIndex % 4;

    return p;
  }

  /**
   * Transfer rent amount from one player to another
   * @param from Player who owes rent
   * @param to Player to whom rent is owed.
   * @param amount The amount of rent.
   * @throws BankruptcyException If the paying player does not have amount.
   */
  public void payRent(AbstractPlayer from, AbstractPlayer to, int amount)
      throws BankruptcyException {
    from.getCash(amount);
    to.receiveCash(amount);
  }

  /**
   * Sell a house from the given location. The proceeds from the sale are given
   * to the player that owns the property (identified by location.owner).
   * 
   * @param location
   *          The property to be sold.
   */
  public void sellHouse(Location location) {
    assert location.getNumHouses() > 0 : location.getFullInfoString();
    location.removeHouse();
    location.getOwner().receiveCash(location.getHouseCost() / 2);
    addHouse();
    
    logInfo("Sold house at " + location.toString() + "; property now has "
        + location.getNumHouses() + " houses");
    
    assert numHouses < 33 : "Invalid number of houses: " + numHouses;
  }

  /**
   * Player is bankrupt, so houses on location are liquidated
   * @param player The player who is bankrupt
   * @param l The property on which a hotel exists
   */
  public void liquidateHouses(AbstractPlayer player, Location l)
  {
    int cost = l.getHouseCost();
    int numHousesAtLocation = l.getNumHouses();
    int proceeds = numHousesAtLocation * cost;
    l.resetNumHouses();
    addHouses(numHousesAtLocation);
    player.receiveCash(proceeds);
  }

  public void sellHotel(Location location, Collection<Location> owned) 
  {
    PropertyFactory pf = PropertyFactory.getPropertyFactory(gamekey);
    ArrayList<Location> lots = pf.getAllPropertiesInGroup(location.getGroup());

    int countHotels = 0;
    int countHouses = 0;
    // sell one hotel and replace with 4 houses
    for (Location lot : lots) {
      if (lot.getNumHotels() == 1) {
        lot.removeHotel();
        ++countHotels;
        ++numHotels;
        for (int i = 0; i < 4; i++)
          lot.addHouse();
        numHouses -= 4; // don't call getHouse(), since we can go negative here
        assert lot.getNumHotels() == 0;
        assert lot.getNumHouses() == 4;
        break;
      }
    }

    // at this point, numHouses may be negative, in which case more hotels
    // and houses need to be sold until numHouses == 0 and houses are balanced
    // if numHouses is negative, sell all hotels, replace with 4 houses
    if (numHouses < 0) {
      for (Location lot : lots) {
        if (lot.getNumHotels() == 1) {
          lot.removeHotel();
          ++countHotels;
          ++numHotels;
          for (int i = 0; i < 4; i++)
            lot.addHouse();
          numHouses -= 4;
          assert lot.getNumHouses() == 4;
        }
        assert lot.getNumHotels() == 0;
      }
    }

    // reduce houses until numHouses is 0
    while (numHouses < 0) {
      for (Location lot : lots) {
        lot.removeHouse();
        ++countHouses;
        ++numHouses;
        if (numHouses == 0) 
          break;
      }
    }

    assert numHouses >= 0;
    int maxHouses = Integer.MIN_VALUE;
    int minHouses = Integer.MAX_VALUE;
    for (Location lot : lots) {
      if (lot.getNumHotels() > 0)
        maxHouses = 5;
      else if (lot.getNumHouses() < minHouses)
        minHouses = lot.getNumHouses();

      if (lot.getNumHouses() > maxHouses)
        maxHouses = lot.getNumHouses();
    }

    int diff = maxHouses - minHouses;
    assert (diff == 0) || (diff == 1);
    
    int proceeds = countHotels * location.getHotelCost() / 2;
    proceeds += countHouses * location.getHouseCost() / 2;

    AbstractPlayer player = location.getOwner();
    if (countHotels > 0)
      logInfo(player.getName() + " sold " + countHotels
          + (countHotels == 1 ? " hotel." : " hotels."));
    if (countHouses > 0)
      logInfo(player.getName() + " sold " + countHouses
          + (countHouses == 1 ? " house." : " houses."));

    player.receiveCash(proceeds);
  }

  /**
   * Player is bankrupt, so hotels on location are liquidated
   * @param abstractPlayer The player who is bankrupt
   * @param l The property on which a hotel exists
   */
  public void liquidateHotel(AbstractPlayer abstractPlayer, Location l)
  {
    int cost = l.getHotelCost();
    int numHotels = l.getNumHotels();
    assert numHotels == 1 : "Location has more than one hotel!";
    l.removeHotel();
    ++numHotels;
    // hotel liquidation is the same as selling 1 hotel and 4 houses
    abstractPlayer.receiveCash(cost + 4 * l.getHouseCost());
  }

  /**
   * Buy a house for player at location.
   * 
   * @param player
   *          The player that is buying the house.
   * @param location
   *          The location which will initially receive the house. The player
   *          must build evenly, so after buying a house for this location, the
   *          player may rebalance the houses among the properties in this
   *          location's group.
   * @return The number of houses bought, either 0 or 1. Zero houses can be
   *         bought if there are no more houses in the bank, or if the player
   *         does not have enough money to buy a house.
   */
  public int buyHouse(AbstractPlayer player, Location location) {
    if (numHouses == 0) {
      logFinest(player.getName()
          + " wanted to buy house, but none are available");
      return 0;
    }

    try {
      assert player.canRaiseCash(location.getHouseCost()) :
          "Player tried to buy house with insufficient cash";
      assert !location.isMortgaged : "Player tried to buy house; Location "
          + location.name + " is mortgaged.";
      assert location.partOfMonopoly : "Player tried to buy house; Location "
          + location.name + " is not part of monopoly";
      assert !PropertyFactory.getPropertyFactory(gamekey).groupIsMortgaged(
          location.getGroup()) : "Player tried to buy house; Some property in "
          + location.getGroup() + " is mortgaged.";

      player.getCash(location.getHouseCost());
      location.addHouse();

      logInfo(player.getName() + " bought house for property group "
          + location.getGroup());
      getHouse();
      assert numHouses >= 0 : "Invalid number of houses: " + numHouses;
      logFinest("Bank now has " + numHouses + " houses");
      return 1;

    } catch (BankruptcyException ignored) {
      // expect that any player that buys a house first verifies they
      // have enough cash
      ignored.printStackTrace();
    } catch (AssertionError ae) {
      logFinest(player.toString());
      throw ae;
    }

    return 0;
  }

  /**
   * Buy a hotel for player at location.
   * 
   * @param player
   *          The player that is buying the hotel.
   * @param location
   *          The location which will receive the hotel.
   */
  public int buyHotel(AbstractPlayer player, Location location) {
    if (numHotels == 0) {
      logFinest(player.getName()
          + " wanted to buy hotel, but none are available");
      return 0;
    }

    try {
      assert player.canRaiseCash(location.getHotelCost()) : 
        "Player tried to buy house with insufficient cash";
      player.getCash(location.getHotelCost());

      location.addHotel();
      --numHotels;
      assert numHotels >= 0 : "Invalid number of hotels: " + numHotels;
      
      logInfo("Bought hotel at " + location.toString());

      // add the houses back to the bank
      this.addHouses(4);

      return 1;
    } catch (BankruptcyException ignored) {
      ignored.printStackTrace();
    }
    return 0;
  }

  /**
   * Take all the necessary actions when a player goes bankrupt. This includes
   * selling all houses and hotels, and giving all cash and properties to the
   * gaining player.
   * 
   * @param player
   *          The player who went bankrupt.
   * @param gainingPlayer
   *          The player who receives all the bankrupt player's assets. This can
   *          be another player in the game, or it can be the bank. If the
   *          parameter is null, then the player went bankrupt against the bank
   *          (for example, by owing income tax).
   */
  public void processBankruptcy(AbstractPlayer player,
      AbstractPlayer gainingPlayer) {

    logInfo(player.getName() + " is bankrupt");
    player.setBankruptIndex(bankruptCount);
    ++bankruptCount;

    boolean gameOver = false;
    if (bankruptCount == Main.numPlayers - 1) {
      gameOver = true;
    }

    if (gainingPlayer != null) {
      logInfo("Gaining player is " + gainingPlayer.getName());
    } else {
      logInfo("Gaining player is bank");
    }

    // return any get out of jail cards to the stack
    while (player.hasGetOutOfJailCard()) {
      player.useGetOutOfJailCard();
    }

    logFinest("Bankrupt count: " + bankruptCount);

    player.sellAllHousesAndHotels();

    if (gainingPlayer == null) {
      // player went bankrupt because they owed bank money
      player.getAllCash();

      if (!gameOver) {
        ArrayList<Location> lotsToAuction = new ArrayList<Location>();
        lotsToAuction.addAll(player.getAllProperties().values());
        player.clearAllProperties();
        auctionLots(lotsToAuction);
      }
    } else {
      // give all cash to gaining player
      gainingPlayer.receiveCash(player.getAllCash());

      // give all property to gaining player
      // mortgaged properties are handled in the addProperties method
      try {
        gainingPlayer.addProperties(player.getAllProperties(), gameOver);
        player.clearAllProperties();
      } catch (BankruptcyException e) {
        //rarely, the player gaining the properties will not be able to raise
        //the case to pay the interest. The gainingPlayer goes bankrupt also.
        processBankruptcy(gainingPlayer, null);
      }
    }

    player.setBankrupt();
    assert player.cash == 0;
  }

  /**
   * For each lot in the map, gather the bids from each player and decide on 
   * the winner of each auction. In case of ties bids, the winner is the
   * player that makes the bid first. Bids are gathered by iterating through
   * the array of AbstractPlayers, so the player with the lower array index
   * will win when there is a tie. 
   * @param lotsToAuction The Map
   */
  public void auctionLots(Collection<Location> lotsToAuction) {
    // set owner to null and mortgaged to false for all lots
    for (Location location : lotsToAuction) {
      location.setOwner(null);
      location.setMortgaged(false);
    }

    String msg = "";

    for (Location location : lotsToAuction) {
      logInfo("\nAUCTION\nBank is auctioning " + location.name);

      int highBid = 0;
      AbstractPlayer highBidPlayer = null;
      int secondHighestBid = 0;

      TreeMap<Integer, Vector<AbstractPlayer>> bids = 
          new TreeMap<Integer, Vector<AbstractPlayer>>();
      for (AbstractPlayer p : players) {
        if (!p.bankrupt()) {

          int bid = p.getBidForLocation(location);
          Vector<AbstractPlayer> v = bids.get(bid);
          if (v == null)
            v = new Vector<AbstractPlayer>();
          v.add(p);
          bids.put(bid, v);
        }
      }

      for (Integer bid : bids.keySet()) {
        Vector<AbstractPlayer> players = bids.get(bid);
        for (AbstractPlayer player : players) {
          logInfo(player.getName() + "'s maximum bid is " + bid);

          if (bid > highBid) {
            secondHighestBid = highBid;
            highBid = bid;
            highBidPlayer = player;
          } else if (bid > secondHighestBid) {
            secondHighestBid = bid;
          }
        }
      }

      int finalBid = 0;

      if (highBid == 0) {
        msg = "early";
        // end auction
        break;
      } else {
        finalBid = secondHighestBid + 5;
        if (finalBid > highBid) {
          finalBid = highBid;
        }
      }

      assert finalBid > 0 : "Final bid is invalid: " + finalBid;
      assert highBidPlayer != null : "High bid player is null";
      assert highBidPlayer.canRaiseCash(finalBid) : highBidPlayer.getName()
          + " cannot raise cash " + finalBid;

      try {
        processAuctionResult(highBidPlayer, location, finalBid);
        for (AbstractPlayer player : players) {
          player.auctionResult(highBidPlayer, location, finalBid);
        }
      } catch (BankruptcyException e) {
        // assume player cannot win auction unless they have enough cash
        // TODO Verify that this exception will not occur
        Throwable t = new Throwable(toString(), e);
        t.printStackTrace();
      }
    }

    logInfo("Auction has ended " + msg);
    boolean printHead = true;
    for (Location location : lotsToAuction) {
      if (location.getOwner() == null) {
        if (printHead) {
          logInfo("The following lots were not bought at auction:");
          printHead = false;
        }
        logInfo(location.name);
      }
    }
  }

  /**
   * Sell the Location to the Player for the given Amount.
   * @param aPlayer The player who won the auction.
   * @param aLocation The location that was being auctioned.
   * @param amount The amount that player owes for the location.
   * @throws BankruptcyException If the player does not have amount.
   */
  private void processAuctionResult(AbstractPlayer aPlayer, Location aLocation,
      int amount) throws BankruptcyException 
  {
    logInfo("\n" + aPlayer.getName() + " won the auction for " + aLocation.name
        + " with a winning bid (may not be the max bid) of $"
        + amount + ".");    

    aPlayer.getCash(amount);
    aPlayer.addProperty(aLocation);
  }

  /**
   * Get the number of houses in the bank.
   * @return The number of houses in the bank.
   */
  public int getNumHousesInBank() {
    return numHouses;
  }

  /**
   * Unpause the game.
   */
  public synchronized void resume() {
    paused = false;
    notify();
  }

  /**
   * Pause the game
   */
  public void pause() {
    paused  = true;
  }

  /**
   * Terminate the game
   */
  public synchronized void terminate() {
    done = true;
    notify();
  }

  /**
   * Release the logger resources and PropertyFactory resource for this game.
   */
  public void endGame() {
    if (logger != null) {
      Handler[] handlers = logger.getHandlers();
      for (Handler handler : handlers) {
        handler.flush();
        handler.close();
        logger.removeHandler(handler);
        handler = null;
      }
    }
    PropertyFactory.releasePropertyFactory(gamekey);
    logger = null;
  }

  /**
   * Called after a special card instructs the player to pay all other players
   * $50.
   * 
   * @param player
   *          The player that owes the amount to other players.
   * @throws BankruptcyException
   *           If the player does not have enough money to pay other players.
   */
  public void payEachPlayer50(AbstractPlayer player) throws BankruptcyException {
    int numPlayersToPay = 0;
    for (AbstractPlayer p : players) {
      if (p != player && !p.bankrupt()) {
        ++numPlayersToPay;
      }
    }

    int amount = numPlayersToPay * 50;
    player.getCash(amount);

    for (AbstractPlayer p : players) {
      if (p != player && !p.bankrupt()) {
        p.receiveCash(50);
      }
    }
  }

  /**
   * Called after a special card awards the player $10 from all other players
   * 
   * @param player
   *          The player that receives the amount from other players.
   */
  public void collect10FromAll(AbstractPlayer player) {
    int payments = 0;

    for (AbstractPlayer p : players) {
      if (p != player && !p.bankrupt()) {
        try {
          p.getCash(10);
          payments += 10;
        } catch (BankruptcyException e) {
          processBankruptcy(p, player);
        }
      }
    }
    player.receiveCash(payments);
  }

  public Cards getCards() {
    return cards;
  }

  /**
   * Return the total net worth of all players in the game.
   * 
   * @return The total net worth of all players in the game.
   */
  public int getTotalNetWorth() {
    int result = 0;
    for (AbstractPlayer player : players) {
      result += player.getTotalWorth();
    }
    return result;
  }

  /**
   * Return the number of players in the game that are not bankrupt.
   * 
   * @return The number of players in the game that are not bankrupt.
   */
  public int getNumActivePlayers() {
    return players.length - bankruptCount;
  }

  /**
   * Log information about the most recent dice roll.
   * 
   * @param roll
   *          The values of each dice.
   */
  public void logDiceRoll(int[] roll) {
    logInfo("\nDice : " + roll[0]+"; " + roll[1]);

    if (roll[0] == roll[1]) {
      logInfo("Doubles!!");
    }
  }

  /**
   * Get a reference to the Dice instance for this game.
   * 
   * @return The Dice instance for this game.
   */
  public Dice getDice() {
    return dice;
  }

  @Override
  public String toString() {
    return "Gen: " + generation + "; Match: " + match + "; Game: " + game;
  }
  
  public void logInfo(String s) {
    if (logger != null) {
      logger.info(s);
    }
  }
  
  public void logFinest(String s) {
    if (logger != null) {
      logger.finest(s);
    }
  }
  
  public void logSevere(String s) {
    if (logger != null) {
      logger.severe(s);
    }
  }

  public void setLogger (Logger logger) {
    this.logger = logger;
  }

  /**
   * Process a proposed trade between two players.
   * 
   * @param bestTrade
   *          The object that contains the two properties and cash to be traded.
   * @return True --> if the trade was accepted<br>False --> otherwise
   */
  public boolean proposeTrade(TradeProposal bestTrade)
  {
    AbstractPlayer owner1 = bestTrade.location.getOwner();
    AbstractPlayer owner2 = bestTrade.location2.getOwner();
    boolean tradeAccepted = false;

    if (owner2.answerProposedTrade(bestTrade)) {
      tradeAccepted = true;

      logFinest("\nOwner 1 before trade");
      logFinest(owner1.toString());
      logFinest("\nOwner 2 before trade");
      logFinest(owner2.toString());
      
      PropertyTrader.tradeProperties(bestTrade.location, bestTrade.location2);
      int cash = bestTrade.cashDiff;
      try {
        if (cash > 0) {
          owner1.getCash(cash);
          owner2.receiveCash(cash);
        } else {
          owner2.getCash(Math.abs(cash));
          owner1.receiveCash(Math.abs(cash));
        }
      } catch (BankruptcyException ignored) {
        // player will not accept trade if bankruptcy will occur
      }

      logFinest("\nOwner 1 after trade");
      logFinest(owner1.toString());
      logFinest("\nOwner 2 after trade");
      logFinest(owner2.toString());
    }

    return tradeAccepted;
  }

  /**
   * @return A list of all the players in this game
   */
  public AbstractPlayer[] getAllPlayers()
  {
    return players;
  }
  
  public int getHouse() {
    numHouses--;
    assert numHouses >= 0;
    return 1;
  }
  
  public void addHouse() {
    addHouses(1);
  }
  
  public void setNumHouses(int num) {
    numHouses = num;
    assert numHouses >= 0;
    assert numHouses <= 32;
  }
  
  public void addHouses(int num) {
    numHouses += num;
    assert numHouses < 33 : "Invalid number of houses: " + numHouses;
  }

  public void mortgageProperty(Location lot) {
    lot.setMortgaged();
    lot.getOwner().receiveCash(lot.getCost() / 2);
  }

  @Override
  public boolean isPaused()
  {
    return paused;
  }
}
