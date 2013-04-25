package edu.uccs.ecgs.players;

import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import edu.uccs.ecgs.ga.*;
import edu.uccs.ecgs.play2.*;

public class HumanPlayer extends AbstractPlayer {

  private String name;
  private String htmlStart = "<html><body width=250>";
  private String htmlEnd = "</body></html>";
  private CopyOnWriteArrayList<Location> monopolies;
  private CopyOnWriteArrayList<Location> sellableLots;
  protected int result;
  private static String[] yesno = new String[] { "Yes", "No" };

  public HumanPlayer(int index, String name) {
    super(index, ChromoTypes.HUM);
    this.name = name;
  }

  /**
   * @return the player's name in the form "Player s" where s is the player's
   *         name.
   */
  @Override
  public String getName()
  {
    return name;
  }

  @Override
  public boolean payBailP()
  {
    String message1 = htmlStart + "Do you want to pay bail to get out of Jail?"
        + htmlEnd;
    if (this.hasGetOutOfJailCard()) {
      message1 = htmlStart + "Do you want to use your Get Out Of Jail card?"
          + htmlEnd;
    }

    int result = GuiHelper.showConfirmDialog(null, message1, "Pay Bail?",
        JOptionPane.YES_NO_OPTION);

    // but in case the player accidentally clicked yes when they might not
    // have enough money, force them to confirm if they don't have enough
    // money
    if (result == JOptionPane.YES_OPTION) {
      if (hasGetOutOfJailCard()) {
        if (cash < getMinimumCash()) {
          result = GuiHelper.showConfirmDialog(null, htmlStart
              + "You don't have a lot of cash. Are you sure you want to use "
              + "your Get Out Of Jail card?" + htmlEnd,
              "Confirm use Get Out Of Jail card", JOptionPane.YES_NO_OPTION,
              JOptionPane.INFORMATION_MESSAGE);
        }
      } else {
        if (canRaiseCash(50)) {
          if (cash < 50) {
            // they don't have enough money to pay bail
            result = GuiHelper.showConfirmDialog(null, htmlStart
                + "You do not have enough cash to pay $50 bail. You "
                + "will need to raise cash by selling hotels or "
                + "houses, or mortgaging  properties. Are you sure you "
                + "want to pay bail?" + htmlEnd, "Confirm pay bail",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
          } else if (cash < getMinimumCash()) {
            // they have some money to pay bail, but not very much
            result = GuiHelper.showConfirmDialog(null, htmlStart
                + "You don't have a lot of cash. Are you sure you want to pay "
                + "$50 bail?" + htmlEnd, "Confirm pay bail",
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
          }
        } else {
          // if they pay bail, they will go bankrupt
          result = GuiHelper.showConfirmDialog(null, htmlStart
              + "If you try to pay bail, you will go bankrupt! Are you sure "
              + "you want to pay bail of $50?" + htmlEnd, "Confirm pay bail",
              JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        }
      }
    }

    return result == JOptionPane.YES_OPTION;
  }

  @Override
  public boolean buyProperty()
  {
    return buyProperty(location);
  }

  @Override
  public boolean buyProperty(Location lot)
  {
    StringBuilder msg = new StringBuilder();

    msg.append(htmlStart);
    msg.append("You landed on ").append(lot.name).append(". You have $")
        .append(cash).append(".<p><p>Do you want to buy ").append(lot.name)
        .append(" for ").append(lot.getCost()).append(" dollars?")
        .append(getOtherOwners(lot.getGroup())).append("<p>")
        .append("<table border=1 width=\"100%\"><tr><td>")
        .append(lot.getFormattedString()).append("</td></tr></table>")
        .append(htmlEnd);

    int result = GuiHelper.showConfirmDialog(null, msg.toString(),
        "Buy Property?", JOptionPane.YES_NO_OPTION);

    return result == JOptionPane.YES_OPTION;
  }

  /*
   * (non-Javadoc)
   * 
   * @see edu.uccs.ecgs.players.AbstractPlayer#payIncomeTax()
   */
  @Override
  public void payIncomeTax() throws BankruptcyException
  {
    final int totalWorth = getTotalWorth();
    final String percent = "10%";
    final String flat = "$200";

    String defaultOption = flat;
    if (totalWorth < 2000)
      defaultOption = percent;

    final String theDefault = new String(defaultOption);

    int result = GuiHelper.showOptionDialog(null, htmlStart
        + "Your current net worth is " + totalWorth
        + "<p>Do you want to pay 10% or $200" + htmlEnd, "Income Tax",
        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
        new String[] { percent, flat }, theDefault);

    if (result == 0) {
      getCash(totalWorth / 10);
    } else {
      getCash(200);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * edu.uccs.ecgs.players.AbstractPlayer#getBidForLocation(edu.uccs.ecgs.ga
   * .Location)
   */
  @Override
  public int getBidForLocation(Location lot)
  {
    assert !bankrupt();

    int bid = 0;
    StringBuilder msg = new StringBuilder();
    msg.append(htmlStart).append(lot.name).append(" is being auctioned.<p><p>")
        .append("You have $").append(cash)
        .append(". The normal cost for this property is $")
        .append(lot.getCost()).append(getOtherOwners(lot.getGroup()))
        .append("<p>").append("<table border=1 width=\"100%\"><tr><td>")
        .append(lot.getFormattedString()).append("</td></tr></table><p><p>")
        .append("What is the MAXIMUM you want to bid for this property? ")
        .append("(0 to ").append(this.getLiquidationValue()).append(")<p><p>")
        .append(htmlEnd);

    while (true) {
      String result = GuiHelper.showInputDialog(null, msg.toString(),
          "Bid for property", JOptionPane.QUESTION_MESSAGE);

      if (result == null)
        result = "0";

      try {
        bid = Integer.parseInt(result.trim());
        if (bid < 0 || bid > getLiquidationValue())
          throw new NumberFormatException();
        break;
      } catch (NumberFormatException e) {
        GuiHelper.showMessageDialog(null, htmlStart
            + "Your bid does not appear to be valid.<p>"
            + "Please enter a dollar amount between 0 " + "and "
            + getLiquidationValue() + ".<p>" + "Bid 0 to decline the auction"
            + htmlEnd, "Bid error", JOptionPane.ERROR_MESSAGE);
      }
    }

    return bid;
  }

  /**
   * Receive the results of an auction and provide a dialog so the actual human
   * player is notified of the results.
   * 
   * @see edu.uccs.ecgs.players.AbstractPlayer#auctionResult
   *      (edu.uccs.ecgs.players.AbstractPlayer, edu.uccs.ecgs.ga.Location, int)
   */
  @Override
  public void auctionResult(AbstractPlayer player, Location lot, int amount)
  {
    if (bankrupt())
      return;

    String playerName = new String(player.getName());
    String lotName = new String(lot.name);

    GuiHelper.showMessageDialog(null, htmlStart + playerName
        + " won the auction for " + lotName + " with a winning bid "
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
  private String getOtherOwners(PropertyGroups group)
  {
    StringBuilder result = new StringBuilder();
    PropertyFactory pf = PropertyFactory.getPropertyFactory(this.gameKey);
    ArrayList<Location> lots = pf.getAllPropertiesInGroup(group);

    for (Location lot : lots) {
      if (lot.getOwner() != null) {
        result.append(lot.name).append(" is owned by ")
            .append(lot.getOwner().getName()).append("<br>");
      }
    }

    if (result.length() > 0) {
      result.insert(0, "<p><p>Some properties in this group are "
          + "owned by other players.<br>");
    }

    return result.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * edu.uccs.ecgs.players.AbstractPlayer#answerProposedTrade(edu.uccs.ecgs.
   * ga.TradeProposal)
   */
  @Override
  public boolean answerProposedTrade(TradeProposal bestTrade)
  {
    String accept = "Accept Trade";
    String reject = "Reject Trade";
    String defaultOption = reject;

    int cash = bestTrade.cashDiff;

    Location lot1 = bestTrade.location;
    AbstractPlayer owner1 = lot1.getOwner();
    Location lot2 = bestTrade.location2;

    StringBuilder sb = new StringBuilder();
    sb.append(owner1.getName())
        .append(" is proposing to trade their property ").append(lot1);
    if (cash > 0)
      sb.append(" and ").append(cash).append(" dollars ");

    sb.append(" for your property ").append(lot2);
    if (cash < 0)
      sb.append(" and ").append(Math.abs(cash)).append(" dollars.");

    logInfo("\n" + sb.toString());

    sb.insert(0, htmlStart);

    sb.append("<p><p>");
    sb.append(owner1.getName()).append(" owns ");
    boolean first = true;
    for (Location lot : owner1.getAllProperties().values()) {
      if (first)
        first = false;
      else
        sb.append(", ");
      sb.append(lot);
    }
    sb.append("<p><p>");

    sb.append("Do you want to Accept or Reject this trade").append(htmlEnd);

    int result = GuiHelper.showOptionDialog(null, sb.toString(),
        "Trade Proposed", JOptionPane.DEFAULT_OPTION,
        JOptionPane.QUESTION_MESSAGE, null, new String[] { accept, reject },
        defaultOption);

    if (result == 0) {
      logInfo("Trade accepted");
      return true;
    }

    logInfo("Trade rejected");
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

    int result = GuiHelper.showOptionDialog(null, htmlStart
        + "Do you want to trade any of your properties?" + htmlEnd,
        "Trade Properties", JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE, null, yesno, yesno[1]);

    if (result == 0) {
      ArrayList<Location> list = new ArrayList<Location>();
      for (Location l : this.getAllProperties().values()) {
        // assume that no player would ever trade away a property that
        // has houses or hotels on it.
        if (l.getNumHouses() + l.getNumHotels() == 0)
          list.add(l);
      }

      Location locationToTrade = (Location) GuiHelper.showInputDialog(null,
          htmlStart + " Which property of yours do you wish to trade?",
          "Select your property", JOptionPane.QUESTION_MESSAGE, null,
          list.toArray(), list.get(0));

      if (locationToTrade == null)
        return;

      Location locationToGet = (Location) GuiHelper.showInputDialog(null,
          htmlStart
              + " Which property of another player do you wish to trade for?",
          "Select other player's property", JOptionPane.QUESTION_MESSAGE, null,
          locations.toArray(), locations.get(0));

      if (locationToGet == null)
        return;

      int cash = getCashPartOfTrade();

      if (cash == Integer.MIN_VALUE)
        return;

      TradeProposal bestTrade = new TradeProposal(locationToTrade,
          locationToGet);
      bestTrade.setCash(cash);

      assert bestTrade.location != null;
      assert bestTrade.location2 != null;

      AbstractPlayer owner = locationToGet.getOwner();

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

      GuiHelper.showMessageDialog(null, sb.toString(), "Trade Result",
          JOptionPane.INFORMATION_MESSAGE);
    }
  }

  private int getCashPartOfTrade()
  {
    String give = "Give";
    String receive = "Receive";
    String none = "No Cash";
    String cancel = "Cancel trade";
    String defaultOption = none;

    int result = GuiHelper.showOptionDialog(null, htmlStart
        + "Do you want to give or receive cash with the trade?" + htmlEnd,
        "Cash Portion of Trade", JOptionPane.DEFAULT_OPTION,
        JOptionPane.QUESTION_MESSAGE, null, new String[] { give, receive, none,
            cancel }, defaultOption);

    if (result == 2)
      return 0;

    if (result == 3)
      return Integer.MIN_VALUE;

    int amount = 0;
    while (true) {
      String strCash = GuiHelper.showInputDialog(null, htmlStart
          + "How much cash do you want to "
          + (result == 0 ? "give (0 to " + getLiquidationValue() + ")"
              : "receive") + "?<p>" + htmlEnd, "Bid for property",
          JOptionPane.QUESTION_MESSAGE);

      if (strCash == null || "".equals(strCash))
        return Integer.MIN_VALUE;

      try {
        amount = Integer.parseInt(strCash);
        if (amount < 0)
          throw new NumberFormatException();
        if (result == 0 && amount > getLiquidationValue())
          throw new NumberFormatException();

        break;
      } catch (NumberFormatException e) {
        if (result == 0) {
          GuiHelper.showMessageDialog(null, htmlStart
              + "The cash amount does not appear to be valid.<p>"
              + "Please enter a whole number between 0 and "
              + getLiquidationValue() + ".<p>" + htmlEnd, "Error",
              JOptionPane.ERROR_MESSAGE);
        } else {
          GuiHelper.showMessageDialog(null, htmlStart
              + "The cash amount does not appear to be valid.<p>"
              + "Please enter a whole number greater than 0.<p>" + htmlEnd,
              "Error", JOptionPane.ERROR_MESSAGE);
        }
      }
    }

    if (result == 1)
      amount = -amount;

    return amount;
  }

  /*
   * (non-Javadoc)
   * 
   * @see edu.uccs.ecgs.players.AbstractPlayer#processDevelopHouseEvent()
   */
  @Override
  public void processDevelopHouseEvent()
  {
    // don't do anything here, the gui allows the player to decide when to buy
    // houses
  }

  /**
   * Buy houses for monopolies
   */
  public void buyHouses()
  {
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
      } else
        done = true;

      if (game.getNumHousesInBank() == 0) {
        done = true;
        StringBuilder msg = new StringBuilder();
        msg.append(htmlStart);
        msg.append("The bank has no more houses to sell.");
        msg.append(htmlEnd);
        JOptionPane.showMessageDialog(null, msg.toString(),
            "Bank out of houses", JOptionPane.INFORMATION_MESSAGE);
      }
    }
  }

  /**
   * Sell houses
   */
  public void sellHouses()
  {
    boolean done = false;
    if (sellableLots == null)
      updateSellableLots();

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
            Object selection = JOptionPane.showInputDialog(null, htmlStart
                + "Because the bank has less than 4 houses, "
                + "you must sell more hotels or houses in addition to the"
                + "hotel you have chosen to sell. If you choose to proceed, "
                + "the bank will automatically sell the correct number of "
                + "hotels and houses." + htmlEnd, "Selling a hotel",
                JOptionPane.WARNING_MESSAGE, null, selectionValues,
                selectionValues[0]);

            if (selection.equals(selectionValues[0])) {
              game.sellHotel(selected, getAllProperties().values());
            }
          } else {
            game.sellHotel(selected, getAllProperties().values());
          }
        }
      } else {
        done = true;
      }

      fireChangeEvent();
    }
  }

  /*
   * (non-Javadoc)
   * 
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
      GuiHelper.showMessageDialog(null, htmlStart
          + "You are unable to raise enough cash to pay your bill of " + amount
          + ". You are bankrupt!" + htmlEnd, "You are bankrupt",
          JOptionPane.INFORMATION_MESSAGE);
      throw new BankruptcyException();
    }

    String myself = "Raise cash myself"; // result == 0
    String theGame = "Let Game raise cash for me"; // result == 1
    String defaultOption = myself;

    int result = GuiHelper
        .showOptionDialog(
            null,
            htmlStart
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

    if (result == -1) {
      // user clicked close window button
      GuiHelper.showMessageDialog(null, htmlStart + "Sorry. You can't cancel. "
          + "You must sell houses or mortgage properties yourself." + htmlEnd,
          "Cancel not allowed", JOptionPane.INFORMATION_MESSAGE);
    }

    boolean done = false;
    while (!done) {
      String mortgage = "Mortgage property";
      String sellHouse = "Sell House";
      String sellHotel = "Sell Hotel";

      ArrayList<Location> unmortgaged = new ArrayList<Location>();
      ArrayList<Location> lotsWithHotels = new ArrayList<Location>();
      CopyOnWriteArrayList<Location> lotsWithHouses = new CopyOnWriteArrayList<Location>();

      // create a list or lots that can be mortgaged
      for (Location lot : getAllProperties().values()) {
        if (lot.canBeMortgaged()) {
          unmortgaged.add(lot);
        }
      }

      if (sellableLots == null)
        updateSellableLots();

      // create a list of lots with houses or hotels that can be sold
      for (Location lot : sellableLots) {
        if (lot.getNumHotels() > 0)
          lotsWithHotels.add(lot);
        if (lot.getNumHouses() > 0)
          lotsWithHouses.add(lot);
      }

      ArrayList<String> options = new ArrayList<String>();
      if (!unmortgaged.isEmpty())
        options.add(mortgage);

      if (!lotsWithHotels.isEmpty())
        options.add(sellHotel);

      if (!lotsWithHouses.isEmpty())
        options.add(sellHouse);

      do {
        result = GuiHelper.showOptionDialog(null, htmlStart
            + "Do you want to mortgage properties, sell hotels, or sell "
            + "houses to raise cash?" + htmlEnd, "Raise cash to pay bills",
            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
            options.toArray(), options.get(0));

        if (result == -1) {
          // user clicked close window button
          GuiHelper.showMessageDialog(null, htmlStart
              + "Sorry. You can't cancel. You must pick a valid option."
              + htmlEnd, "Cancel not allowed", JOptionPane.INFORMATION_MESSAGE);
        }
      } while (result == -1);

      String selected = options.get(result);

      if (selected.equals(mortgage)) {
        Location locationToMortgage = (Location) GuiHelper.showInputDialog(
            null, htmlStart
                + " Which property of yours do you wish to mortgage?",
            "Select property to mortgage", JOptionPane.QUESTION_MESSAGE, null,
            unmortgaged.toArray(), unmortgaged.get(0));
        if (locationToMortgage != null)
          game.mortgageProperty(locationToMortgage);
      }

      if (selected.equals(sellHotel)) {
        Location hotel = (Location) GuiHelper.showInputDialog(null, htmlStart
            + " Which property has a hotel that you want to sell?",
            "Select hotel to sell", JOptionPane.QUESTION_MESSAGE, null,
            lotsWithHotels.toArray(), lotsWithHotels.get(0));
        if (hotel != null)
          game.sellHotel(hotel, getAllProperties().values());
      }

      if (selected.equals(sellHouse)) {
        Location house = (Location) GuiHelper.showInputDialog(null, htmlStart
            + " Which property has a house that you want to sell?",
            "Select house to sell", JOptionPane.QUESTION_MESSAGE, null,
            lotsWithHouses.toArray(), lotsWithHouses.get(0));
        if (house != null)
          game.sellHouse(house);
      }

      if (cash > amount)
        done = true;
    }
    super.getCash(amount);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * edu.uccs.ecgs.players.AbstractPlayer#processMortgagedNewProperties(java
   * .util.TreeMap)
   */
  @Override
  protected void processMortgagedNewProperties(TreeMap<Integer, Location> newProperties)
      throws BankruptcyException
  {
    fireChangeEvent();
    Vector<Location> mortgaged = getSortedMortgages(newProperties);

    if (mortgaged.size() == 0)
      return;

    int interest = 0;
    for (Location lot : mortgaged) {
      interest += 0.1 * lot.getCost() / 2;
    }

    StringBuilder sb = new StringBuilder();
    sb.append(" received ").append(mortgaged.size())
        .append(" mortgaged properties.");
    StringBuilder sb2 = new StringBuilder();
    sb2.append(interest).append(" interest on all new properties.");

    logInfo(getName() + sb.toString());
    for (Location lot : mortgaged)
      logInfo(lot.toString());
    logInfo(getName() + " owes $" + sb2.toString());

    sb.insert(0, "You");
    sb.insert(0, htmlStart);
    sb.append("<p><p>");
    sb.append("You owe $").append(sb2.toString());
    sb.append(htmlEnd);

    GuiHelper.showMessageDialog(null, sb.toString(), "Interest owed",
        JOptionPane.INFORMATION_MESSAGE);

    getCash(interest);

    Object[] options = new String[] { "Lift Mortgages", "Later" };
    int result = GuiHelper.showOptionDialog(null, htmlStart + "You paid $"
        + interest
        + " in interest. You can lift the mortgages from any properties by "
        + "clicking the Lift Mortgages button. If you want to lift the "
        + "mortgages later, click the Later button." + htmlEnd,
        "Interest Payed", JOptionPane.DEFAULT_OPTION,
        JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
    
    if (result == 0) {
      liftMortgages(false);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see edu.uccs.ecgs.players.AbstractPlayer#getSourceName()
   */
  @Override
  public String getSourceName()
  {
    return getName();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * edu.uccs.ecgs.players.AbstractPlayer#fireChangeEvent(javax.swing.event.
   * ChangeEvent)
   */
  @Override
  protected void fireChangeEvent(ChangeEvent event)
  {
    super.fireChangeEvent(event);

    boolean ableToSell = false;
    boolean ableToBuy = false;

    // if the player has houses or hotels, then enable sell
    if (getNumHotels() + getNumHouses() > 0) {
      ableToSell = true;
      updateSellableLots();
    }

    // update the list of properties that can have houses bought for them
    updateHouseReadyLots();

    // if this player has a monopoly AND...
    // if the game has houses to sell...
    if (hasMonopoly() && game.getNumHousesInBank() > 0) {
      if (monopolies.size() > 0)
        ableToBuy = true;
    } else if (game.getNumHousesInBank() == 0) {
      for (Location lot : monopolies) {
        if (lot.getNumHouses() == 4) {
          ableToBuy = true;
          break;
        }
      }
    }

    final boolean sell = ableToSell;
    final boolean buy = ableToBuy;

    java.awt.EventQueue.invokeLater(new Runnable() {
      public void run()
      {
        PlayerGui.updateHouseButtons(sell, buy);
      }
    });

    int countMortgaged = 0;
    for (Location lot : getAllProperties().values()) {
      if (lot.isMortgaged()) {
        ++countMortgaged;
        break;
      }
    }

    final boolean hasMortgages = countMortgaged > 0;
    java.awt.EventQueue.invokeLater(new Runnable() {
      public void run()
      {
        PlayerGui.updateMortgageButton(hasMortgages);
      }
    });
  }

  /**
   * Create a list of lots upon which a player can build houses or hotels. If no
   * houses are available from the bank, then only list lots which can have a
   * hotel.
   */
  private void updateHouseReadyLots()
  {
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
        if (location.getNumHouses() < groupMin[location.getGroup().ordinal()]) {
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
    // player's cash. Finally, if bank is out of houses, remove locations with
    // less than 4 houses
    for (Location location : monopolies) {
      int index = location.getGroup().ordinal();
      int numHouses = location.getNumHouses();
      if (numHouses > groupMin[index]) {
        monopolies.remove(location);
      } else if (location.getHouseCost() > cash) {
        monopolies.remove(location);
      } else if (game.getNumHousesInBank() == 0 && numHouses < 4) {
        monopolies.remove(location);
      }
    }
  }

  /**
   * Create a list of lots from which a player can sell houses.
   */
  private void updateSellableLots()
  {
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

  /*
   * (non-Javadoc)
   * 
   * @see edu.uccs.ecgs.players.AbstractPlayer#payOffMortgages()
   */
  @Override
  public void payOffMortgages()
  {
    // don't do anything here, the gui allows the player to decide when to lift
    // mortgages
  }

  /**
   * Allows the player to pay interest and mortgage amount to lift mortgages
   * from properties that are owned by this player.
   */
  public void liftMortgages()
  {
    liftMortgages(true);
  }

  /**
   * Lift the mortgages from properties owned by this player.
   * 
   * @param payInterest
   *          If true, player pays the interest and the mortgage, if false,
   *          player only pays the mortgage. When the player has received the
   *          properties through another player's bankruptcy, the
   *          processMortgagedNewProperties method pays the interest, so it
   *          doesn't need to be paid here if that method calls this method.
   */
  public void liftMortgages(boolean payInterest)
  {
    Vector<Location> mortgaged = getSortedMortgages(getAllProperties());

    double interest = 1.0;
    if (payInterest) {
      interest = 1.1;
    }
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
        int payoff = (int) (interest * ((double) lot.getCost()) / 2.0);
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

  /*
   * (non-Javadoc)
   * 
   * @see edu.uccs.ecgs.players.AbstractPlayer#setBankrupt()
   */
  @Override
  public void setBankrupt()
  {
    super.setBankrupt();
    String computer = "Let computer play";
    String human = "I will keep playing";
    String defaultOption = computer;

    StringBuilder sb = new StringBuilder();
    sb.append(htmlStart)
        .append("Since you are now out of the game, you can let the computer ")
        .append("play the game to the end without pausing, or you can keep ")
        .append("playing by pressing the 'Play a Turn' button.<p><p>")
        .append("Click 'Let computer play' to let the computer play without ")
        .append("pausing.<p><p>")
        .append(
            "Click 'I will keep playing' to continue controlling each turn.")
        .append(htmlEnd);

    int result = GuiHelper.showOptionDialog(null, sb.toString(),
        "Let the computer finish?", JOptionPane.DEFAULT_OPTION,
        JOptionPane.QUESTION_MESSAGE, null, new String[] { computer, human },
        defaultOption);

    if (result == 0) {
      PlayerGui.pauseOff();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see edu.uccs.ecgs.players.AbstractPlayer#setBankNumHouses(int)
   */
  @Override
  public void setBankNumHouses(int numHouses)
  {
    super.setBankNumHouses(numHouses);
    fireChangeEvent();
  }

  @Override
  public void dumpGenome(DataOutputStream out) throws IOException
  {
  }

  @Override
  public void printGenome()
  {
  }

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
