package edu.uccs.ecgs.ga;

public class TradeProposal {

  public Location location;
  public Location location2;
  private int cashDiff;
  private int agentProfit;

  public TradeProposal(Location location, Location location2) {
    this.location = location;
    this.location2 = location2;
  }

  /**
   * If positive, the amount is taken from location.owner and given to
   * location2.owner. If negative, the amount is taken from location2.owner and
   * given to location.owner.
   * 
   * @param cashDiff The amount of cash to include in the trade.
   */
  public void setCash(int cashDiff)
  {
    this.cashDiff = cashDiff;
  }

  public void setProfit(int agentProfit)
  {
    this.agentProfit = agentProfit;
  }
}
