package edu.uccs.ecgs.ga;

import java.util.ArrayList;

public class GAController implements Controllable {

  private ArrayList<Monopoly> games;
  private boolean paused;

  public GAController() {
    games = new ArrayList<Monopoly>();
  }
  
  public void addGame(Monopoly game) {
    games.add(game);
  }

  @Override
  public void pause()
  {
    paused = true;
    for (Monopoly game : games)
      game.pause();
  }

  @Override
  public void resume()
  {
    paused = false;
    for (Monopoly game : games)
      game.resume();
  }

  @Override
  public boolean isPaused()
  {
    return paused;
  }
}
