package edu.uccs.ecgs.ga;

import java.util.Vector;

/**
 * Compute fitness based on the number of wins a player has. For each first
 * place finish in a game, the player is awarded a score that is the number of
 * players in a game minus 1 (e.g. if there are 4 players in a game, the score
 * for a win is 3). The player gets 0 points for any other game result (second
 * through last place).
 */
public class NumWinsFitnessEvaluator extends AbstractFitnessEvaluator {
  private static final int FIRST_PLACE = 1;
  private static final int MAX_SCORE = Main.numPlayers - 1;
  private static final int MIN_SCORE = 0;

  @Override
  public void evaluate(Vector<AbstractPlayer> players)
  {
    for (AbstractPlayer player : players) {
      // Compute the score for the most recent game
      int gameScore = player.getFinishOrder() == FIRST_PLACE ? MAX_SCORE
          : MIN_SCORE;

      // Store the new fitness value
      player.addToFitness(gameScore);
    }
  }

  @Override
  public String getDirName()
  {
    return "num_wins";
  }

  @Override
  public boolean isType(FitEvalTypes type) {
    if (type == FitEvalTypes.NUM_WINS)
      return true;
    
    return false;
  }
}