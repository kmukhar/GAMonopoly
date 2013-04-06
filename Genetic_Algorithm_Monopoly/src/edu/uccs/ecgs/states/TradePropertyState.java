package edu.uccs.ecgs.states;

import java.util.ArrayList;
import edu.uccs.ecgs.ga.*;
import edu.uccs.ecgs.players.AbstractPlayer;

public class TradePropertyState extends PlayerState {

  @Override
  protected void enter() {
    super.enter();
  }

  @Override
  public PlayerState processEvent (Monopoly game, AbstractPlayer player, Events event) {
    game.logFinest(player.getName() + "; state " + this.getClass().getSimpleName() +
        "; event " + event.name());
    switch (event) {
    
    case TRADE_DECISION_EVENT:
      if (Main.allowPropertyTrading) {
        game.logFinest("\nStarting process trade decision event");
        PropertyFactory pf = PropertyFactory.getPropertyFactory(game.gamekey);
        // get list of undeveloped lots owned by other players
        ArrayList<Location> locations = pf.getPropertiesOwnedByOthers(player);

        if (!locations.isEmpty())
          player.processTradeDecisionEvent(locations);
      }

      player.nextAction = Actions.DONE;
      inactiveState.enter();
      return inactiveState;
      
    default:
      String msg = "Unexpected event " + event;
      throw new IllegalArgumentException(msg);
    }
  }
}
