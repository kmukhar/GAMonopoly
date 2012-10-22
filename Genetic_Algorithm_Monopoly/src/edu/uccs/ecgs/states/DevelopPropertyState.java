package edu.uccs.ecgs.states;

import edu.uccs.ecgs.ga.Actions;
import edu.uccs.ecgs.ga.Monopoly;
import edu.uccs.ecgs.players.AbstractPlayer;

public class DevelopPropertyState extends PlayerState {

  @Override
  protected void enter() {
    super.enter();
  }

  @Override
  public PlayerState processEvent(Monopoly game, AbstractPlayer player, Events event) {
    game.logFinest("Player " + player.playerIndex + "; state " + this.getClass().getSimpleName() +
        "; event " + event.name());

    switch (event) {

    case DEVELOP_DECISION_EVENT:
      player.processDevelopHouseEvent();

      player.nextAction = Actions.MAKE_MORTGAGE_DECISION;
      payoffMortgageState.enter();
      return payoffMortgageState;

    default:
      String msg = "Unexpected event " + event;
      throw new IllegalArgumentException(msg);
    }
  }

}
