package cs571.mukhar.states;

import java.util.TreeMap;

import cs571.mukhar.Actions;
import cs571.mukhar.Location;
import cs571.mukhar.Monopoly;

public class AuctionState extends PlayerState {
  @Override
  protected void enter() {
    super.enter();
  }

  @Override
  public PlayerState processEvent(Events event) {
    logger.info("Player " + player.playerIndex + "; state " + this.getClass().getSimpleName() +
        "; event " + event.name());
    switch (event) {
    
    case AUCTION_STARTED_EVENT:
      
      TreeMap<Integer, Location> lotsToAuction = new TreeMap<Integer, Location>();
      Location location = player.getCurrentLocation();
      lotsToAuction.put(location.index, location);
      
      Monopoly.auctionLots(lotsToAuction);

      if (location.owner == player) {
        player.nextAction = Actions.AUCTION_WON;
        buyPropertyState.enter();
        return buyPropertyState;
      } else {
        player.nextAction = Actions.AUCTION_LOST;
        propertyDeclinedState.enter();
        return propertyDeclinedState;
      }

    default:
      String msg = "Unexpected event " + event;
      throw new IllegalArgumentException(msg);
    }
  }
}
