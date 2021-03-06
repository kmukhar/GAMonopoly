package edu.uccs.ecgs.ga;

import java.util.Vector;
import edu.uccs.ecgs.players.AbstractPlayer;

/**
 * 
 */
public class TournamentFitnessEvaluator extends AbstractFitnessEvaluator {
  @Override
  public void evaluate(Vector<AbstractPlayer> players)
  {
    for (AbstractPlayer player : players) {
      // Compute the score for the most recent game
      int gameScore = player.getFinishOrder() < 3 ? 1 : 0;

      // Store the new fitness value
      player.addToFitness(gameScore);
    }
  }

  @Override
  public String getDirName()
  {
    return "tournament";
  }

  @Override
  public boolean isType(FitEvalTypes type) {
    if (type == FitEvalTypes.TOURNAMENT)
      return true;
    
    return false;
  }
}
