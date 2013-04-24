package edu.uccs.ecgs.ga;

import edu.uccs.ecgs.players.AbstractPlayer;

public class TradeProposal {

  public Location location;
  public Location location2;
  public int cashDiff;
  public int agentProfit;

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
  
  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("Trade Proposal\nLocation1: ").append(location);
    if (cashDiff > 0) 
      sb.append(" and ").append(cashDiff).append(" dollars");

    sb.append(" for\nLocation2: ").append(location2);
    if (cashDiff < 0)
      sb.append(" and ").append(Math.abs(cashDiff)).append(" dollars");

    return sb.toString();
  }

  /**
   * @return The name of the proposer of this trade
   */
  public String getProposerName()
  {
    return location.getOwner().getName();
  }

  /**
   * @return The player that is the proposer of this trade
   */
  public AbstractPlayer getProposer()
  {
    return location.getOwner();
  }
}
