package edu.uccs.ecgs.players;

import java.io.*;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import edu.uccs.ecgs.ga.*;

public class HumanPlayer extends AbstractPlayer {

  private String name;

  public HumanPlayer(int index, ChromoTypes chromoType) {
    super(index, chromoType);
    // TODO Auto-generated constructor stub
  }

  public HumanPlayer(int index) {
    super(index, ChromoTypes.HUM);
  }

  public HumanPlayer(int index, DataInputStream dis) {
    super(index, ChromoTypes.HUM);
  }
  
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
    int result = JOptionPane.showConfirmDialog(null,
        "Do you want to pay bail to get out of Jail?", "Pay Bail?",
        JOptionPane.YES_NO_OPTION);
    return result == JOptionPane.YES_OPTION;
  }

  @Override
  public boolean buyProperty()
  {
    return buyProperty (location);
  }

  @Override
  public boolean buyProperty(Location location)
  {
    int result = JOptionPane.showConfirmDialog(null, "Do you want to buy "
        + location.name + "?", "Buy Property?", JOptionPane.YES_NO_OPTION);
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
        "<html><body>Your current net worth is " + totalWorth
            + "<p>Do you want to pay 10% or $200</body></html>", "Income Tax",
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
  public int getBidForLocation(Location currentLocation)
  {
    int bid = 0;
    while (true) {
      String result = JOptionPane.showInputDialog(null, "<html><body>"
          + currentLocation.name + " is being auctioned.<p>"
          + "What is the maximum you want to bid for this property?<p>" 
          + "(0 to " + cash + ")"
          + "</body></html>", "Bid for property", JOptionPane.QUESTION_MESSAGE);
      try {
        bid = Integer.parseInt(result);
        if (bid < 0 || bid > cash)
          throw new NumberFormatException();
        break;
      } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(null, "<html><body>"
            + "Your bid does not appear to be valid.<p>"
            + "Please enter a whole number between 0 "
            + "and " + cash + ".<p>"
            + "Bid 0 to decline the auction" + "</body></html>", "Bid error",
            JOptionPane.ERROR_MESSAGE);
      }
    }

    return bid;
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

    int result = JOptionPane.showOptionDialog(null, "<html><body>"
        + bestTrade.location.owner.getName() + " is proposing to trade "
        + bestTrade.location + " for " + bestTrade.location2 + "<p><p>"
        + "Do you want to Accept or Reject this trade</body></html>",
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
    TradeProposal bestTrade = null;

    int result = JOptionPane.showConfirmDialog(null, "<html><body>"
        + "Do you want to trade any of your properties?</body></html>",
        "Trade Properties", JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE);
    if (result == 0) {
      ArrayList<Location> list = new ArrayList<Location>();
      for (Location l : this.getAllProperties().values()) {
        // assume that no player would ever trade away a property that
        // has houses or hotels on it.
        if (l.getNumHouses() + l.getNumHotels() == 0)
          list.add(l);
      }

      Location locationToTrade = (Location) JOptionPane.showInputDialog(null,
          "<html><body>" + " Which property of yours do you wish to trade?",
          "Select your property", JOptionPane.QUESTION_MESSAGE, null,
          list.toArray(), list.get(0));

      Location locationToGet = (Location) JOptionPane.showInputDialog(null,
          "<html><body>"
              + " Which property of another player do you wish to trade for?",
          "Select other player's property", JOptionPane.QUESTION_MESSAGE, null,
          locations.toArray(), locations.get(0));

      int cash = getCashPartOfTrade();
      bestTrade = new TradeProposal(locationToTrade, locationToGet);
      bestTrade.setCash(cash);

      game.proposeTrade(bestTrade);
    }
  }

  private int getCashPartOfTrade()
  {
    String give = "Give";
    String receive = "Receive";
    String none = "No Cash";
    String defaultOption = none;

    int result = JOptionPane.showOptionDialog(null, "<html><body>"
        + "Do you want to give or receive cash with the trade?"
        + "</body></html>", "Cash Portion of Trade",
        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
        new String[] { give, receive, none }, defaultOption);

    if (result == 2)
      return 0;

    int amount = 0;
    while (true) {
      String strCash = JOptionPane.showInputDialog(null, "<html><body>"
          + "How much cash do you want to "
          + (result == 0 ? "give" : "receive") + "?<p>" + "(0 to " + cash + ")"
          + "</body></html>", "Bid for property", JOptionPane.QUESTION_MESSAGE);
      try {
        amount = Integer.parseInt(strCash);
        if (amount < 0 || amount > cash)
          throw new NumberFormatException();
        break;
      } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(null, "<html><body>"
            + "The cash amount does not appear to be valid.<p>"
            + "Please enter a whole number between 0 " + "and " + cash + ".<p>"
            + "</body></html>", "Error", JOptionPane.ERROR_MESSAGE);
      }
    }

    return amount;
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
}
