package edu.uccs.ecgs.states;

//import org.junit.Assert;

import edu.uccs.ecgs.ga.Actions;
import edu.uccs.ecgs.ga.BankruptcyException;
import edu.uccs.ecgs.ga.Dice;
import edu.uccs.ecgs.ga.Monopoly;
import edu.uccs.ecgs.players.AbstractPlayer;

public class InJailState extends PlayerState {

  @Override
  protected void enter() {
    super.enter();
  }

  @Override
  public PlayerState processEvent(Monopoly game, AbstractPlayer player,
                                  Events event)
  {
    game.logFinest(player.getName() + "; state "
        + this.getClass().getSimpleName() + "; event " + event.name());
    Dice dice = game.getDice();
    switch (event) {
    
    case ROLL_DICE_EVENT:
      int[] roll = dice.roll();
      game.logDiceRoll(roll);
      int currentRoll = roll[0] + roll[1];

      //even though player rolled doubles, they do not get to roll again
      //so do not call player.setDoubles()

      if (dice.rolledDoubles()) {
        player.leaveJail();
        game.logInfo(player.getName() + " rolled doubles! Leaving jail now.");
        movePlayer(currentRoll, player);
        assert player.nextAction != Actions.ROLL_DICE : "Invalid action: ";
        atNewLocationState.enter();
        return determineNextState(player);
      } else {
        //did not roll doubles
        player.setDoubles(false);
        if (player.jailSentenceCompleted()) {
          game.logInfo(player.getName()
              + " has been in jail for 3 turns, must leave jail now.");
          if (player.hasGetOutOfJailCard()) {
            player.useGetOutOfJailCard();
          } else {
            //actually pay bail
            try {
              player.getCash(50);
            } catch (BankruptcyException e) {
              //e.printStackTrace();
              game.processBankruptcy(player, null);
              player.nextAction = Actions.DONE;
              return inactiveState;
            }
          }
          player.leaveJail();
          movePlayer(currentRoll, player);
          assert player.nextAction != Actions.ROLL_DICE : "Invalid action: ";
          return determineNextState(player);
        } else {
          game.logInfo(player.getName() + " did not roll doubles. Still in jail.");
        }
        player.nextAction = Actions.MAKE_BUILD_DECISION;
        developPropertyState.enter();
        return developPropertyState;
      }

    case PAY_BAIL_EVENT:
      //assume that if the player wants to pay bail, they would
      //use a Get Out Of Jail Card first, if they have one
      if (player.hasGetOutOfJailCard()) {
        player.useGetOutOfJailCard();
        game.logInfo(player.getName() + " used Get Out of Jail Free card");
      } else {
        //actually pay bail
        try {
          player.payBail();
        } catch (BankruptcyException e) {
          //e.printStackTrace();
          game.processBankruptcy(player, null);
          player.nextAction = Actions.DONE;
          return inactiveState;
        }
      }

      player.leaveJail();
      player.nextAction = Actions.ROLL_DICE;
      bailPaidState.enter();
      return bailPaidState;

    default:
      String msg = "Unexpected event " + event;
      throw new IllegalArgumentException(msg);
    }
  }
}
