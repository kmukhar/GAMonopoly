package edu.uccs.ecgs.play2;

import edu.uccs.ecgs.ga.Controllable;
import edu.uccs.ecgs.ga.Monopoly;

public class GameController implements Controllable {

  private Monopoly game;
  boolean paused = true;

  public GameController(Monopoly game) {
    this.game = game;
  }

  @Override
  public void pause()
  {}

  @Override
  public void resume()
  {
    game.resume();
  }

  @Override
  public boolean isPaused()
  {
    return paused;
  }

}
