package edu.uccs.ecgs.states;

import edu.uccs.ecgs.ga.Actions;
import edu.uccs.ecgs.ga.Location;
import edu.uccs.ecgs.ga.Monopoly;
import edu.uccs.ecgs.players.AbstractPlayer;

public class PropertyDeclinedState extends PlayerState {

  @Override
  protected void enter() {
    super.enter();
  }

  @Override
  public PlayerState processEvent(Monopoly game, AbstractPlayer player, Events event) {
    game.logFinest(player.getName() + "; state " + this.getClass().getSimpleName() +
        "; event " + event.name());
    switch (event) {

    case DECLINE_PROPERTY_EVENT:
      Location location = player.getCurrentLocation();
      game.logInfo(player.getName() + " has decided NOT to buy "
          + location.name + " for " + location.getCost() + " dollars.");
      player.nextAction = Actions.AUCTION_BID;
      auctionState.enter();
      return auctionState;

    case LOST_AUCTION_EVENT:
      if (player.rolledDoubles()) {
        player.nextAction = Actions.ROLL_DICE;
        return this;
      } else {
        player.nextAction = Actions.MAKE_BUILD_DECISION;
        developPropertyState.enter();
        return developPropertyState;
      }

    case ROLL_DICE_EVENT:
      rollDice(game, player);
      return determineNextState(player);

    default:
      String msg = "Unexpected event " + event;
      throw new IllegalArgumentException(msg);
    }
  }

}
