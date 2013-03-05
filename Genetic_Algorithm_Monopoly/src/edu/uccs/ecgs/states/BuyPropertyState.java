package edu.uccs.ecgs.states;

import java.util.ArrayList;
import edu.uccs.ecgs.ga.Actions;
import edu.uccs.ecgs.ga.BankruptcyException;
import edu.uccs.ecgs.ga.Location;
import edu.uccs.ecgs.ga.Monopoly;
import edu.uccs.ecgs.players.AbstractPlayer;

public class BuyPropertyState extends PlayerState {

  @Override
  protected void enter() {
    super.enter();
  }

  @Override
  public PlayerState processEvent(Monopoly game, AbstractPlayer player, Events event) {
    game.logFinest(player.getName() + "; state " + this.getClass().getSimpleName() +
        "; event " + event.name());
    switch (event) {
    
    case BUY_PROPERTY_EVENT:
      Location location = player.getCurrentLocation();

      assert player.canRaiseCash(location.getCost()) : "Player cannot raise cash: " + location.getCost();
      
      game.logInfo(player.getName() + " has decided to buy "
          + location.name + " for " + location.getCost() + " dollars.");
      
      try {
        player.getCash(location.getCost());
        player.addProperty(location);
      } catch (BankruptcyException e) {
        // player will not buy house unless they have enough cash
        // TODO Verify that this exception will not occur
        Throwable t = new Throwable(game.toString(), e);
        t.printStackTrace();

        //but just in case this happens...
        ArrayList<Location> lotsToAuction = new ArrayList<Location>();
        lotsToAuction.add(location.index, location);
        game.auctionLots(lotsToAuction);
      }

      if (player.rolledDoubles()) {
        player.nextAction = Actions.ROLL_DICE;
        return activeState;
      } else {
        player.nextAction = Actions.MAKE_BUILD_DECISION;
        developPropertyState.enter();
        return developPropertyState;
      }
      
    case WON_AUCTION_EVENT:
      if (player.rolledDoubles()) {
        player.nextAction = Actions.ROLL_DICE;
      } else {
        player.nextAction = Actions.MAKE_BUILD_DECISION;
        developPropertyState.enter();
        return developPropertyState;
      }
      return this;

    case ROLL_DICE_EVENT:
      rollDice(game, player);
      return determineNextState(player);

    default:
      String msg = "Unexpected event " + event;
      throw new IllegalArgumentException(msg);
    }
  }

}
