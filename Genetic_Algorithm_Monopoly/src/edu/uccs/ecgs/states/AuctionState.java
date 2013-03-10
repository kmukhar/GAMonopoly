package edu.uccs.ecgs.states;

import java.util.ArrayList;
import edu.uccs.ecgs.ga.Actions;
import edu.uccs.ecgs.ga.Location;
import edu.uccs.ecgs.ga.Monopoly;
import edu.uccs.ecgs.players.AbstractPlayer;

public class AuctionState extends PlayerState {
  @Override
  protected void enter() {
    super.enter();
  }

  @Override
  public PlayerState processEvent(Monopoly game, AbstractPlayer player, Events event) {
    game.logFinest(player.getName() + "; state " + this.getClass().getSimpleName() +
        "; event " + event.name());
    switch (event) {
    
    case AUCTION_STARTED_EVENT:
      
      ArrayList<Location> lotsToAuction = new ArrayList<Location>();
      Location location = player.getCurrentLocation();
      lotsToAuction.add(location);
      
      game.auctionLots(lotsToAuction);

      if (location.getOwner() == player) {
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
