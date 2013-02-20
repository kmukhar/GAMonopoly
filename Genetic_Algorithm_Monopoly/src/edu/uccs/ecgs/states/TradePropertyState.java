package edu.uccs.ecgs.states;

import edu.uccs.ecgs.ga.Actions;
import edu.uccs.ecgs.ga.Monopoly;
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
      game.logFinest("\nStarting process trade decision event");
      player.processTradeDecisionEvent();

      player.nextAction = Actions.DONE;
      inactiveState.enter();
      return inactiveState;
      
    default:
      String msg = "Unexpected event " + event;
      throw new IllegalArgumentException(msg);
    }
  }

}
