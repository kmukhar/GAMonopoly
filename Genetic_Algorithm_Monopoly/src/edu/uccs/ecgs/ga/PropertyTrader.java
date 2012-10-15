package edu.uccs.ecgs.ga;

public class PropertyTrader {

  /**
   * Trade the given properties between the owner of this PropertyNegotiator and
   * the player who owns "gaining"
   * 
   * @param losing
   *          The property that owner is trading away
   * @param gaining
   *          The property that owner is receiving
   */
  public static void tradeProperties(Location losing, Location gaining)
  {
    AbstractPlayer owner = losing.owner;
    AbstractPlayer otherPlayer = gaining.getOwner();

    losing.setOwner(otherPlayer);
    otherPlayer.getAllProperties().put(losing.index, losing);
    otherPlayer.getAllProperties().remove(gaining.index);

    gaining.setOwner(owner);
    owner.getAllProperties().put(gaining.index, gaining);
    owner.getAllProperties().remove(losing.index);
  }
}
