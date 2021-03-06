package edu.uccs.ecgs.states;

import edu.uccs.ecgs.ga.Actions;
import edu.uccs.ecgs.ga.BankruptcyException;
import edu.uccs.ecgs.ga.Location;
import edu.uccs.ecgs.ga.Monopoly;
import edu.uccs.ecgs.players.AbstractPlayer;

public class PayRentState extends PlayerState {

  @Override
  protected void enter() {
    super.enter();
  }

  @Override
  public PlayerState processEvent(Monopoly game, AbstractPlayer player, Events event) {
    game.logFinest(player.getName() + "; state " + this.getClass().getSimpleName() +
        "; event " + event.name());
    switch (event) {

    case PAY_RENT_EVENT:
      Location location = player.getCurrentLocation();

      if (location.getOwner() != player) {
        if (location.isMortgaged()) {
            game.logInfo("Lot is mortgaged, rent: 0");
            location.resetRentMultiplier();
        } else {
          int amount = location.getRent(game.getDice().getLastRoll());
          assert amount >= 0 : "Invalid rent: " + location.name + "; rent: "
              + amount;

          game.logInfo("Rent for " + location.toString() + ": " + amount);
          
          if (amount > 0) {
            try {
              game.payRent(player, location.getOwner(), amount);
            } catch (BankruptcyException e) {
              // e.printStackTrace();
              game.processBankruptcy(player, location.getOwner());
              player.nextAction = Actions.DONE;
              return inactiveState;
            }
          }
        }
      }

      if (player.rolledDoubles()) {
        player.nextAction = Actions.ROLL_DICE;
        return activeState;
      } else {
        player.nextAction = Actions.MAKE_BUILD_DECISION;
        return developPropertyState;
      }

    case ROLL_DICE_EVENT:
      rollDice(game, player);
      if (player.nextAction == Actions.MAKE_BUILD_DECISION) {
        developPropertyState.enter();
        return developPropertyState;
      } else if (player.nextAction == Actions.ROLL_DICE) {
        return activeState;
      } else {
        atNewLocationState.enter();
        return atNewLocationState;
      }

    default:
      String msg = "Unexpected event " + event;
      throw new IllegalArgumentException(msg);
    }
  }

}
