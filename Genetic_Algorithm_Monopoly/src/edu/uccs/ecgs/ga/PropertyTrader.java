package edu.uccs.ecgs.ga;

import edu.uccs.ecgs.players.AbstractPlayer;

public class PropertyTrader {

  /**
   * Trade the given properties between two players
   * 
   * @param location1
   *          The property that owner1 is trading for location2
   * @param location2
   *          The property that owner2 is trading for location1
   */
  public static void tradeProperties(Location location1, Location location2)
  {
    AbstractPlayer owner1 = location1.getOwner();
    AbstractPlayer owner2 = location2.getOwner();

    owner2.removeProperty(location2);
    owner1.removeProperty(location1);
    
    owner2.addProperty(location1);
    owner1.addProperty(location2);
  }
}
