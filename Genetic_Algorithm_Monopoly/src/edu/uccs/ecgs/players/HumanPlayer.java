package edu.uccs.ecgs.players;

import java.io.*;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;

import edu.uccs.ecgs.ga.*;
import edu.uccs.ecgs.play2.LocationButton;
import edu.uccs.ecgs.play2.PlayerGui;

public class HumanPlayer extends AbstractPlayer {

  private String name;
  private String htmlStart = "<html><body width=250>";
  private String htmlEnd = "</body></html>";
  private CopyOnWriteArrayList<Location> monopolies;
  private CopyOnWriteArrayList<Location> sellableLots;

  public HumanPlayer(int index, String name) {
    super(index, ChromoTypes.HUM);
    this.name = name;
  }

  /**
   * @return the player's name in the form "Player s" where s is the player's
   * name.
   */
  @Override
  public String getName()
  {
    return name;
  }

  @Override
  public boolean payBailP()
  {
    String message = "Do you want to pay bail to get out of Jail?";
    if (this.hasGetOutOfJailCard()) {
      message = "Do you want to use your Get Out Of Jail card?";
    }
    
    int result = JOptionPane.showConfirmDialog(null,
        message, "Pay Bail?",
        JOptionPane.YES_NO_OPTION);
    return result == JOptionPane.YES_OPTION;
  }

  @Override
  public boolean buyProperty()
  {
    return buyProperty (location);
  }

  @Override
  public boolean buyProperty(Location lot)
  {
    StringBuilder msg = new StringBuilder();

    msg.append(htmlStart);
    msg.append("You landed on ").append(lot.name)
        .append(".<p>Do you want to buy ").append(lot.name).append(" for ")
        .append(lot.getCost()).append(" dollars?")
        .append(getOtherOwners(lot.getGroup())).append("<p>")
        .append("<table border=1 width=\"100%\"><tr><td>")
        .append(lot.getFormattedString())
        .append("</td></tr></table>")
        .append(htmlEnd);

    int result = JOptionPane.showConfirmDialog(null, msg.toString(),
        "Buy Property?", JOptionPane.YES_NO_OPTION);

    return result == JOptionPane.YES_OPTION;
  }

  /* (non-Javadoc)
   * @see edu.uccs.ecgs.players.AbstractPlayer#payIncomeTax()
   */
  @Override
  public void payIncomeTax() throws BankruptcyException {
    int totalWorth = getTotalWorth();
    String percent = "10%";
    String flat = "$200";
    String defaultOption = flat;
    if (totalWorth < 2000) 
      defaultOption = percent;

    int result = JOptionPane.showOptionDialog(null,
        htmlStart + "Your current net worth is " + totalWorth
            + "<p>Do you want to pay 10% or $200"+htmlEnd, "Income Tax",
        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
        new String[] { percent, flat }, defaultOption);
    if (result == 0) {
      getCash(totalWorth/10);
    } else {
      getCash(200);
    }
  }

  /* (non-Javadoc)
   * @see edu.uccs.ecgs.players.AbstractPlayer#getBidForLocation(edu.uccs.ecgs.ga.Location)
   */
  @Override
  public int getBidForLocation(Location lot)
  {
    int bid = 0;
    StringBuilder msg = new StringBuilder();
    msg.append(htmlStart).append(lot.name).append(" is being auctioned.<p><p>")
        .append("The normal cost for this property is $").append(lot.getCost())
        .append(getOtherOwners(lot.getGroup())).append("<p>")
        .append("<table border=1 width=\"100%\"><tr><td>")
        .append(lot.getFormattedString()).append("</td></tr></table><p><p>")
        .append("What is the MAXIMUM you want to bid for this property? ")
        .append("(0 to ").append(cash).append(")<p><p>").append(htmlEnd);

    while (true) {
      String result = JOptionPane.showInputDialog(null, msg.toString(),
          "Bid for property", JOptionPane.QUESTION_MESSAGE);
      try {
        bid = Integer.parseInt(result);
        if (bid < 0 || bid > cash)
          throw new NumberFormatException();
        break;
      } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(null, htmlStart
            + "Your bid does not appear to be valid.<p>"
            + "Please enter a dollar amount between 0 "
            + "and " + cash + ".<p>"
            + "Bid 0 to decline the auction" + htmlEnd, "Bid error",
            JOptionPane.ERROR_MESSAGE);
      }
    }

    return bid;
  }

  /**
   * Receive the results of an auction and provide a dialog so the actual
   * human player is notified of the results.
   * @see edu.uccs.ecgs.players.AbstractPlayer#auctionResult
   * (edu.uccs.ecgs.players.AbstractPlayer, edu.uccs.ecgs.ga.Location, int)
   */
  @Override
  public void auctionResult(AbstractPlayer player, Location lot,
      int amount) {
    JOptionPane.showMessageDialog(null, htmlStart + player.getName()
        + " won the auction for " + lot.name + " with a winning bid "
        + " (may not be the max bid) of $" + amount + "." + htmlEnd,
        "Auction Complete", JOptionPane.INFORMATION_MESSAGE);
  }

  /**
   * Checks to see if any properties in the group are owned, and if so lists the
   * owners of those properties.
   * 
   * @param group
   *          The group to check if other locations are owned by other players.
   * @return A String with a list of other owners of properties in the group.
   *         Empty String if there are no other properties owned in the group.
   */
  private String getOtherOwners(PropertyGroups group) {
    StringBuilder result = new StringBuilder();
    PropertyFactory pf = PropertyFactory.getPropertyFactory(this.gameKey);
    ArrayList<Location> lots = pf.getAllPropertiesInGroup(group);

    for (Location lot : lots) {
      if (lot.owner != null) {
        result.append(lot.name).append(" is owned by ")
            .append(lot.owner.getName()).append("<br>");
      }
    }

    if (result.length() > 0) {
      result.insert(0, "<p><p>Some properties in this group are "
          + "owned by other players.<br>");
    }
    
    return result.toString();
  }

  /* (non-Javadoc)
   * @see edu.uccs.ecgs.players.AbstractPlayer#answerProposedTrade(edu.uccs.ecgs.ga.TradeProposal)
   */
  @Override
  public boolean answerProposedTrade(TradeProposal bestTrade)
  {
    String accept = "Accept Trade";
    String reject = "Reject Trade";
    String defaultOption = reject;

    int cash = bestTrade.cashDiff;
    
    StringBuilder sb = new StringBuilder();
    sb.append(htmlStart).append(bestTrade.location.owner.getName())
        .append(" is proposing to trade ").append(bestTrade.location);
    if (cash > 0)
      sb.append(" and ").append(cash).append(" dollars ");

    sb.append(" for ").append(bestTrade.location2);
    if (cash < 0)
      sb.append(" and ").append(Math.abs(cash)).append(" dollars.")
          .append("<p><p>")
          .append("Do you want to Accept or Reject this trade").append(htmlEnd);
    
    int result = JOptionPane.showOptionDialog(null, sb.toString(),
        "Trade Proposed", JOptionPane.DEFAULT_OPTION,
        JOptionPane.QUESTION_MESSAGE, null, new String[] { accept, reject },
        defaultOption);
    if (result == 0) {
      return true;
    }

    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see edu.uccs.ecgs.players.AbstractPlayer#processTradeDecisionEvent()
   */
  @Override
  public void processTradeDecisionEvent(ArrayList<Location> locations)
  {
    if (this.getNumProperties() == 0)
      return;

    int result = JOptionPane.showOptionDialog(null, htmlStart
        + "Do you want to trade any of your properties?"+htmlEnd,
        "Trade Properties", JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE,null, new String[]{"Yes","No"}, "No");

    if (result == 0) {
      ArrayList<Location> list = new ArrayList<Location>();
      for (Location l : this.getAllProperties().values()) {
        // assume that no player would ever trade away a property that
        // has houses or hotels on it.
        if (l.getNumHouses() + l.getNumHotels() == 0)
          list.add(l);
      }

      Location locationToTrade = (Location) JOptionPane.showInputDialog(null,
          htmlStart
              + " Which property of yours do you wish to trade?",
          "Select your property", JOptionPane.QUESTION_MESSAGE, null,
          list.toArray(), list.get(0));

      if (locationToTrade == null)
        return;
      
      Location locationToGet = (Location) JOptionPane.showInputDialog(null,
          htmlStart
              + " Which property of another player do you wish to trade for?",
          "Select other player's property", JOptionPane.QUESTION_MESSAGE, null,
          locations.toArray(), locations.get(0));

      if (locationToGet == null) 
        return;
      
      int cash = getCashPartOfTrade();
      TradeProposal bestTrade = new TradeProposal(locationToTrade,
          locationToGet);
      bestTrade.setCash(cash);

      assert bestTrade.location != null;
      assert bestTrade.location2 != null;

      AbstractPlayer owner = locationToGet.owner;

      boolean accepted = game.proposeTrade(bestTrade);
      StringBuilder sb = new StringBuilder();
      sb.append(htmlStart);
      if (accepted) {
        sb.append(owner.getName()).append(" accepted the trade. You now own ");
        sb.append(locationToGet.name).append(" and ").append(owner.getName());
        sb.append(" owns ").append(locationToTrade.name).append(".");
      } else {
        sb.append(owner.getName()).append(" rejected the trade.");
      }
      sb.append(htmlEnd);

      JOptionPane.showMessageDialog(null, sb.toString(), "Trade Result",
          JOptionPane.INFORMATION_MESSAGE);
    }
  }

  private int getCashPartOfTrade()
  {
    String give = "Give";
    String receive = "Receive";
    String none = "No Cash";
    String defaultOption = none;

    int result = JOptionPane.showOptionDialog(null, htmlStart
        + "Do you want to give or receive cash with the trade?"
        + htmlEnd, "Cash Portion of Trade",
        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
        new String[] { give, receive, none }, defaultOption);

    if (result == 2)
      return 0;

    int amount = 0;
    while (true) {
      String strCash = JOptionPane.showInputDialog(null,
          htmlStart + "How much cash do you want to "
              + (result == 0 ? "give" : "receive") + "?<p>" + "(0 to " + cash
              + ")" + htmlEnd, "Bid for property",
          JOptionPane.QUESTION_MESSAGE);
      try {
        amount = Integer.parseInt(strCash);
        if (amount < 0 || amount > cash)
          throw new NumberFormatException();
        break;
      } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(null, htmlStart
            + "The cash amount does not appear to be valid.<p>"
            + "Please enter a whole number between 0 " + "and " + cash + ".<p>"
            + htmlEnd, "Error", JOptionPane.ERROR_MESSAGE);
      }
    }

    return amount;
  }

  /* (non-Javadoc)
   * @see edu.uccs.ecgs.players.AbstractPlayer#processDevelopHouseEvent()
   */
  @Override
  public void processDevelopHouseEvent() {
    // don't do anything here, the gui allows the player to decide when to buy
    // houses
  }

  /**
   * Buy houses for monopolies
   */
  public void buyHouses() {
    boolean done = false;
    while (!done) {
      if (monopolies.size() == 0) {
        done = true;
        break;
      }

      Object object = JOptionPane.showInputDialog(null, htmlStart
          + "Which property do you want to buy a house for?<p<p>"
          + "Click Cancel if you no longer want to buy a house or hotel."
          + htmlEnd, "Select property to build on",
          JOptionPane.QUESTION_MESSAGE, null, monopolies.toArray(),
          monopolies.get(0));

      Location selected = (Location) object;
      if (selected != null) {
        if (selected.getNumHouses() < 4)
          game.buyHouse(this, selected);
        else
          game.buyHotel(this, selected);
      } 
      else
        done = true;

      fireChangeEvent();
    }
  }

  /**
   * Sell houses
   */
  public void sellHouses() {
    boolean done = false;

    while (!done) {
      if (sellableLots.size() == 0) {
        done = true;
        break;
      }

      Object object = JOptionPane.showInputDialog(null, htmlStart
          + "Which property do you want to sell a house or hotel from?<p<p>"
          + "Click Cancel if you no longer want to sell a house or hotel."
          + htmlEnd, "Select property to sell house or hotel from",
          JOptionPane.QUESTION_MESSAGE, null, sellableLots.toArray(),
          sellableLots.get(0));

      Location selected = (Location) object;
      if (selected != null) {
        if (selected.getNumHouses() > 0)
          game.sellHouse(selected);
        else {
          if (game.getNumHousesInBank() < 4) {
            Object[] selectionValues = new Object[] {
                "Ok, I still want to sell that hotel", "No, let me reconsider" };
            JOptionPane.showInputDialog(null, htmlStart
                + "Because the bank has less than 4 houses, "
                + "you must sell more hotels or houses in addition to the"
                + "hotel you have chosen to sell. If you choose to proceed, "
                + "the bank will automatically sell the correct number of "
                + "hotels and houses." + htmlEnd,
                "Selling a hotel", JOptionPane.WARNING_MESSAGE,
                    null, selectionValues, selectionValues[0]);
          }
          game.sellHotel2(selected,getAllProperties().values());
        }
      } else {
        done = true;
      }

      fireChangeEvent();
    }
  }

  /* (non-Javadoc)
   * @see edu.uccs.ecgs.players.AbstractPlayer#getCash(int)
   */
  @Override
  public void getCash(int amount) throws BankruptcyException
  {
    if (cash >= amount) {
      super.getCash(amount);
      return;
    }

    if (!canRaiseCash(amount)) {
      JOptionPane.showMessageDialog(null, htmlStart
          + "You are unable to raise enough " + "cash to pay your bill of "
          + amount + ". You are bankrupt!");
      throw new BankruptcyException();
    }

    String myself = "Raise cash myself";
    String theGame = "Let Game raise cash for me";
    String defaultOption = myself;

    int result = JOptionPane.showOptionDialog(null, htmlStart 
        + "You do not have enough cash to pay what you owe. Do you want to "
        + "try to sell houses or hotels, and mortgage properties yourself to "
        + "raise the cash? Or do you want to let the game sell your properties "
        + "to raise the cash?" + htmlEnd, "Raise cash to pay bills",
        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
        new String[] { myself, theGame }, defaultOption);

    if (result == 1) {
      super.getCash(amount);
      return;
    }

    boolean done = false;
    while (!done) {
      String mortgage = "Mortgage property";
      String sellHouse = "Sell House";
      String sellHotel = "Sell Hotel";

      ArrayList<Location> unmortgaged = new ArrayList<Location>();
      ArrayList<Location> lotsWithHotels = new ArrayList<Location>();
      CopyOnWriteArrayList<Location> lotsWithHouses = 
          new CopyOnWriteArrayList<Location>();
      
      for (Location lot : getAllProperties().values()) {
        if (lot.getNumHotels() > 0) {
          lotsWithHotels.add(lot);
        } else if (lot.getNumHouses() > 0) {
          lotsWithHouses.add(lot);
        } else if (!lot.isMortgaged()) {
          unmortgaged.add(lot);
        }
      }

      // If any lots in group have a hotel, can't sell a house until all
      // hotels in group are sold
      for (Location lotWithHotel : lotsWithHotels) {
        PropertyGroups group = lotWithHotel.getGroup();
        for (Location lotWithHouse : lotsWithHouses) {
          if (lotWithHouse.getGroup() == group)
            lotsWithHouses.remove(lotWithHouse);
        }
      }

      ArrayList<String> options = new ArrayList<String>();
      if (!unmortgaged.isEmpty())
        options.add(mortgage);

      if (!lotsWithHotels.isEmpty())
        options.add(sellHotel);

      if (!lotsWithHouses.isEmpty())
        options.add(sellHouse);

      result = JOptionPane.showOptionDialog(null, htmlStart
          + "Do you want to mortgage properties, sell hotels, or sell "
          + "houses to raise cash?"
          + htmlEnd, "Raise cash to pay bills",
          JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
          options.toArray(), options.get(0));

      String selected = options.get(result);

      if (selected.equals(mortgage)) {
        Location locationToMortgage = (Location) JOptionPane.showInputDialog(
            null, htmlStart
                + " Which property of yours do you wish to mortgage?",
            "Select property to mortgage", JOptionPane.QUESTION_MESSAGE, null,
            unmortgaged.toArray(), unmortgaged.get(0));
        game.mortgageProperty(locationToMortgage);
      }

      if (selected.equals(sellHotel)) {
        Location hotel = (Location) JOptionPane.showInputDialog(
            null, htmlStart
                + " Which property has a hotel that you want to sell?",
            "Select hotel to sell", JOptionPane.QUESTION_MESSAGE, null,
            lotsWithHotels.toArray(), lotsWithHotels.get(0));
        game.sellHotel2(hotel, getAllProperties().values());
      }

      if (selected.equals(sellHouse)) {
        Location house = (Location) JOptionPane.showInputDialog(
            null, htmlStart
                + " Which property has a house that you want to sell?",
            "Select house to sell", JOptionPane.QUESTION_MESSAGE, null,
            lotsWithHouses.toArray(), lotsWithHouses.get(0));
        game.sellHouse(house);
      }
      
      if (cash > amount)
        done = true;
    }
    super.getCash(amount);
  }

  /* (non-Javadoc)
   * @see edu.uccs.ecgs.players.AbstractPlayer#getSourceName()
   */
  @Override
  public String getSourceName() {
    return getName();
  }

  /* (non-Javadoc)
   * @see edu.uccs.ecgs.players.AbstractPlayer#fireChangeEvent(javax.swing.event.ChangeEvent)
   */
  @Override
  protected void fireChangeEvent(ChangeEvent event) {
    super.fireChangeEvent(event);

    boolean ableToSell = false;
    boolean ableToBuy = false;
    
    // if the player has houses or hotels, then enable sell
    if (getNumHotels() + getNumHouses() > 0) {
      ableToSell = true;
      updateSellableLots();
    }

    // if this player has a monopoly AND...
    // if the game has houses to sell AND...
    if (hasMonopoly() && game.getNumHousesInBank() > 0) {
      // if the player can buy houses
      // the min number of houses on the properties in a group
      updateHouseReadyLots();

      if (monopolies.size() > 0)
        ableToBuy = true;
    }
    
    PlayerGui.updateHouseButtons(ableToSell, ableToBuy);

    int countMortgaged = 0;
    for (Location lot : getAllProperties().values()) {
      if (lot.isMortgaged()) {
        ++countMortgaged;
        break;
      }
    }

    PlayerGui.updateMortgageButton(countMortgaged > 0);
  }

  /**
   * Create a list of lots upon which a player can build houses. 
   */
  private void updateHouseReadyLots() {
    int[] groupMin = new int[PropertyGroups.values().length];
    for (PropertyGroups group : PropertyGroups.values()) {
      groupMin[group.ordinal()] = Integer.MAX_VALUE;
    }
    
    // Create a list of all the properties that the player owns that are also
    // part of monopolies
    monopolies = new CopyOnWriteArrayList<Location>();

    for (Location location : getAllProperties().values()) {
      if (PropertyFactory.getPropertyFactory(this.gameKey).groupIsMortgaged(
          location.getGroup())) {
        // skip this property since the group is mortgaged
        continue;
      }

      if (location.partOfMonopoly && location.getNumHotels() == 0) {
        monopolies.add(location);
        if (location.getNumHouses() < groupMin[location.getGroup().ordinal()]) 
        {
          groupMin[location.getGroup().ordinal()] = location.getNumHouses();
        }
        logFinest(location.toString()
            + " added to list of monopolies in updateHouseReadyLots");
      }
    }

    // now remove any properties that have more than the min properties in a
    // group. Houses and hotels must be balanced, so one can't build on a 
    // property that already has greater than the min number of houses. Also 
    // remove properties where the cost of a house is greater than the 
    // player's cash.
    for (Location location : monopolies) {
      int index = location.getGroup().ordinal();
      if (location.getNumHouses() > groupMin[index]) {
        monopolies.remove(location);
      } else if (location.getHouseCost() > cash) {
        monopolies.remove(location);
      }
    }
  }

  /**
   * Create a list of lots from which a player can sell houses. 
   */
  private void updateSellableLots() {
    int[] groupMax = new int[PropertyGroups.values().length];
    for (PropertyGroups group : PropertyGroups.values()) {
      groupMax[group.ordinal()] = Integer.MIN_VALUE;
    }
    
    // Create a list of all the properties that the player owns that are also
    // part of monopolies
    sellableLots = new CopyOnWriteArrayList<Location>();

    for (Location location : getAllProperties().values()) {
      if (location.getNumHotels() == 0 && location.getNumHouses() == 0) {
        // skip this property since there are no hotels or houses to sell
        continue;
      }

      sellableLots.add(location);
      int numHouses = location.getNumHouses() + location.getNumHotels() * 5;

      if (numHouses > groupMax[location.getGroup().ordinal()]) {
        groupMax[location.getGroup().ordinal()] = numHouses;
      }
      logFinest(location.toString()
          + " added to list of lots with houses to sell in updateSellableLots");
    }

    // now remove any properties that have less than the max properties in a
    // group. Houses and hotels must be balanced, so one can't sell a house
    // from a property that already has less than the max number of houses.
    for (Location location : sellableLots) {
      int index = location.getGroup().ordinal();
      int numHouses = location.getNumHouses() + location.getNumHotels() * 5;
      if (numHouses < groupMax[index]) {
        sellableLots.remove(location);
      }
    }
  }

  /* (non-Javadoc)
   * @see edu.uccs.ecgs.players.AbstractPlayer#payOffMortgages()
   */
  @Override
  public void payOffMortgages()
  {
    // don't do anything here, the gui allows the player to decide when to lift
    // mortgages
  }

  public void liftMortgages() {
    Vector<Location> mortgaged = getSortedMortgages(getAllProperties());

    boolean done = false;
    while (!done && mortgaged.size() > 0) {
      Location lot = (Location) JOptionPane.showInputDialog(null, htmlStart
          + " Which mortgaged property do you wish to lift the mortgage from? "
          + "Click Cancel if you don't want to lift any more mortgages."
          + htmlEnd, "Select Property", JOptionPane.QUESTION_MESSAGE, null,
          mortgaged.toArray(), mortgaged.get(0));

      if (lot == null) 
        done = true;
      else {
        int payoff = (int)(1.1 * ((double)lot.getCost()) / 2.0);
        if (payoff > cash) {
          JOptionPane.showMessageDialog(null, htmlStart + "You don't have "
              + "enough money to lift the mortgage for " + lot.name + ". You "
              + "need $" + payoff + ", but you only have $" + cash + ".",
              "Not enough cash", JOptionPane.INFORMATION_MESSAGE);
        } else {
          int result = JOptionPane.showConfirmDialog(null, htmlStart
              + "It will cost " + "$" + payoff
              + " to lift the mortgage. Click OK to pay the " + "mortgage. "
              + "Click cancel to quit." + htmlEnd, "Payoff cost",
              JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
          if (result == JOptionPane.OK_OPTION) {
            try {
              getCash(payoff);
              mortgaged.remove(lot);
              lot.setMortgaged(false);
            } catch (BankruptcyException ignored) {
              // should not happen, payoff < cash was checked in if block
            }
            fireChangeEvent();
          }
        }        
      }
    }
  }

  @Override
  public void dumpGenome(DataOutputStream out) throws IOException {}

  @Override
  public void printGenome() {}

  @Override
  public AbstractPlayer[] createChildren(AbstractPlayer parent2, int index)
  {
    return null;
  }

  @Override
  public AbstractPlayer copyAndMutate()
  {
    return null;
  }

  @Override
  public Object clone() throws CloneNotSupportedException
  {
    return null;
  }

  public HumanPlayer(int index, ChromoTypes chromoType) {
    super(index, chromoType);
  }

  public HumanPlayer(int index) {
    super(index, ChromoTypes.HUM);
  }

  public HumanPlayer(int index, DataInputStream dis) {
    super(index, ChromoTypes.HUM);
  }  
}
