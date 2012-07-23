package edu.uccs.ecgs.ga;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The Genetic Algorithm Engine. Create and manage the population of players.
 * Evaluate the fitness of the genome, and call the code to reproduce a
 * population.
 */
public class GAEngine implements Runnable {

  private int generation = 0;
  private int matches = 0;
  private int gameNumber = 0;

  /**
   * The minimum score required for a player chromosome to advance to the next
   * generation.
   */
//  private int minEliteScore;

  /**
   * A map sorted by score; the key for each entry is a score and the entry
   * value is the number of players with that score
   */
  TreeMap<Integer, Integer> scores = null;

  private Main program;

  /**
   * The pool of available players in a generation. When a player is picked for
   * a game, the player is removed from playerPool and placed into playersDone.
   * After all games have been played, the players are placed back into the
   * playerPool for the next set of games.
   */
  private Vector<AbstractPlayer> playerPool;
  private Vector<AbstractPlayer> playersDone;

  private Random r;

  /**
   * A queue that contains games to be played. This queue is used by the
   * gameExecutor.
   */
  private LinkedBlockingQueue<Runnable> runnableGames;

  /**
   * A list of games. The games in this list are the same as the games in
   * runnableGames. This list enables the GAEngine to call some cleanup methods
   * on the games after all the games are complete.
   */
  private ArrayList<Monopoly> games;

  /**
   * A Thread pool for executing the games in runnableGames.
   */
  private ThreadPoolExecutor gameExecutor;

  public GAEngine(Main main) {
    program = main;
    
    playerPool = new Vector<AbstractPlayer>(Main.maxPlayers);
    playersDone = new Vector<AbstractPlayer>(Main.maxPlayers);

    r = new Random();
    long seed = 1241797664697L;
    if (Main.useRandomSeed) {
      seed = System.currentTimeMillis();
    }
    r.setSeed(seed);
    createPlayers();
  }

  /**
	 * Create a pool of players. If Main.loadFromDisk is LOAD_AND_EVOLVE or
	 * LOAD_AND_COMPETE, this method loads players from stored data files.
	 * Otherwise this methods creates a pool of randomly generated players.
	 */
  private void createPlayers() {
  	if (program.loadFromDisk == LoadTypes.NO_LOAD) {
      // Create new population of players
      for (int i = 0; i < Main.maxPlayers; i++) {
        AbstractPlayer player = PlayerFactory.getPlayer(i, program.chromoType);
        player.initCash(1500);
        playerPool.add(player);
      }  		

  	} else {
      playerPool.clear();
      PopulationPropagator propagator = new PopulationPropagator(program);
      playerPool.addAll(propagator.loadPlayers(program.loadGeneration));

      if (program.loadFromDisk == LoadTypes.LOAD_AND_EVOLVE) {
        Vector<AbstractPlayer> newPopulation = propagator.evolve(playerPool,
            computeMinEliteScore());
        playerPool.clear();
        playerPool.addAll(newPopulation);
      } else {
        for (AbstractPlayer player : playerPool) {
          player.resetFitness();
        }
      }
  	}
  }

  /**
   * Pick four players at random from the pool of available players.
   * @return A list of four randomly selected players.
   */
  private AbstractPlayer[] getFourPlayers() {
    AbstractPlayer[] players = new AbstractPlayer[Main.numPlayers];
    for (int i = 0; i < players.length; i++) {
      players[i] = playerPool.remove(r.nextInt(playerPool.size()));
      playersDone.add(players[i]);
      assert !playerPool.contains(players[i]);
    }
    return players;
  }

  @Override
  public void run() {
    runGames();
  }

  /**
   * Create and evolve a population of players. 
   */
  public void runGames() {
    if (program.loadFromDisk != LoadTypes.NO_LOAD) {
      generation = program.loadGeneration + 1;
    }
    int maxGeneration = generation + Main.numGenerations;

    runnableGames = new LinkedBlockingQueue<Runnable>();

    while (generation < maxGeneration) {
      program.setGenNum(generation);
      matches = 0;

      IFitnessEvaluator fitEval = null;
      if (program.loadFromDisk == LoadTypes.LOAD_AND_COMPETE) {
        // when competing, we may want to use a different evaluator than
        // specified in the startup
        String evaluator = System.getProperty("evaluator");
        fitEval = FitEvalTypes.valueOf(evaluator).get();
      } else {
        // otherwise use the specified evaluator
        fitEval = program.fitnessEvaluator.get();
      }

      while (matches < Main.numMatches) {
        program.setMatchNum(matches);
        gameExecutor = new ThreadPoolExecutor(Main.numThreads,
            Main.numThreads * 2, 1L, TimeUnit.MINUTES, runnableGames);
        gameNumber = 0;

        games = new ArrayList<Monopoly>();

        while (!playerPool.isEmpty()) {
          Monopoly game = new Monopoly(program, generation, matches, gameNumber,
              getFourPlayers());

          games.add(game);
          gameExecutor.execute(game);

          ++gameNumber;
          // sleep 1 millisecond so each game gets a different random seed
          try {
            Thread.sleep(1);
          } catch (InterruptedException ignored) {
          }
        }
        assert playerPool.isEmpty();

        // Start the executor shutdown process...
        gameExecutor.shutdown();

        // ...but wait for all games to complete, at which point the executor
        // will actually be shutdown
        boolean allGamesComplete = false; 
        while (!allGamesComplete) {
          try {
            // This will block until the executor is terminated or there is an
            // InterruptedException (timeout should not occur given the timeout
            // value of 106752 days). If the executor terminates normally,
            // allGamesComplete will be set to true.
            allGamesComplete = gameExecutor.awaitTermination(Long.MAX_VALUE,
                TimeUnit.NANOSECONDS);
          } catch (InterruptedException ignored) {
            ignored.printStackTrace();
          }
        }

        for (Monopoly game : games) {
          assert game.done;
        }
        fitEval.evaluate(playersDone);
        
        ++matches;

        // Move all the players back to the player pool.
        playerPool.addAll(playersDone);
        playersDone.removeAllElements();

        games.clear();
      }

      int minEliteScore = computeMinEliteScore();

      // dump the player data every dumpPeriod generations and the last
      // generation
      if (generation % Main.dumpPeriod == 0
          || generation == maxGeneration - 1) {
        dumpGenome();

        // not sure if normalization is useful, so don't call it for now.
        // fitEval.normalize(playerPool);

        dumpPlayerFitness();
      }

      generation++;

      if (generation < maxGeneration) {
        if (program.loadFromDisk == LoadTypes.LOAD_AND_COMPETE) {
          // don't evolve in this case
          for (AbstractPlayer player : playerPool) {
            player.resetFitness();
          }
        } else {
          PopulationPropagator propagator = new PopulationPropagator(program);
          Vector<AbstractPlayer> newPopulation = propagator.evolve(playerPool,
              minEliteScore);
          playerPool.clear();
          playerPool.addAll(newPopulation);

          for (AbstractPlayer player : playerPool) {
            assert player.getFitness() == 0;
          }
        }
      }

      // TODO There seems to be memory being held onto by the program.
      // I suspect it is the loggers or in the loggers. We have a different
      // logger instance for every game, and it does not appear that they ever
      // get released. This is here in the hope that it will help release some 
      // of the memory.
      try {
        System.gc(); // suggest garbage collection
        Thread.sleep(1000); // sleep for 1 sec to allow gc
      } catch (InterruptedException ignored) {
      }
    }
  }

  /**
   * Output files with player fitness data.
   */
  private void dumpPlayerFitness() {
    // the set is used to dump a list of each player with the player's
    // individual score
    ArrayList<AbstractPlayer> fitness = new ArrayList<AbstractPlayer>(
        Main.maxPlayers);

    fitness.addAll(playerPool);

    Path dir = program.getDirForGen(program.chromoType, program.fitnessEvaluator,
        generation);

    // dump the score counts
    BufferedWriter bw = null;
    try {
      FileWriter fw = new FileWriter(dir.resolve("fitness_scores.csv").toFile());
      bw = new BufferedWriter(fw);

      bw.write("fitness,num players");
      bw.newLine();

      // all scores
      int minScore = scores.firstKey();
      int maxScore = scores.lastKey();

      for (int i = minScore; i <= maxScore; i++) {
        if (scores.containsKey(i)) {
          bw.write(i + "," + scores.get(i).intValue());
          bw.newLine();
        } else {
          bw.write(i + ",0");
          bw.newLine();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (bw != null) {
        try {
          bw.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    Collections.sort(fitness);

    // dump individual fitness scores
    try {
      FileWriter fw = new FileWriter(dir.resolve("player_fitness.csv").toFile());
      bw = new BufferedWriter(fw);

      bw.write("fitness,player id");
      bw.newLine();

      for (AbstractPlayer player : fitness) {
        bw.write(player.getFitness() + "," + player.playerIndex);
        bw.newLine();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (bw != null) {
        try {
          bw.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Compute the minimum elite score to allow 10% of the players to directly
   * propagate to the next generation.
   */
  private int computeMinEliteScore()
  {
    scores = new TreeMap<Integer, Integer>();

    for (AbstractPlayer player : playerPool) {
      Integer val = scores.get(player.getFitness());
      if (val == null) {
        scores.put(player.getFitness(), 1);
      } else {
        Integer newVal = val.intValue() + 1;
        scores.put(player.getFitness(), newVal);
      }
    }

    int minEliteScore = 0;
    int playerCount = 0;
    int maxPlayerCount = (int) (0.1 * Main.maxPlayers);
    // Ensure at least 1 player move to next generation based on eliteness
    if (maxPlayerCount < 1) {
      maxPlayerCount = 1;
    }

    for (Integer key : scores.descendingKeySet()) {
      playerCount += scores.get(key).intValue();
      if (playerCount >= maxPlayerCount) {
        minEliteScore = key.intValue();
        break;
      }
    }
    return minEliteScore;
  }

  /**
   * Output data files with the chromosome for each player.
   */
  private void dumpGenome() {
    for (AbstractPlayer player : playerPool) {
      StringBuilder fn1 = new StringBuilder(32);
      // prepend 0s to the player index number 
      fn1.append("000").append(player.playerIndex);
      // now take just the last four chars so we have 0000 to 9999
      while (fn1.length() > 4) {
        fn1.deleteCharAt(0);
      }
      fn1.insert(0, "player");
      fn1.append(".dat");

      Path dir = program.getDirForGen(program.chromoType,
          program.fitnessEvaluator, generation);

      DataOutputStream dos = null;
      try {
        FileOutputStream fos = new FileOutputStream(dir.resolve(fn1.toString())
            .toFile());
        dos = new DataOutputStream(fos);
        player.dumpGenome(dos);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        if (dos != null) {
          try {
            dos.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

  /**
   * Send unpause signal to all games.
   */
  public void resume() {
    for (Monopoly game : games) {
      game.resume();
    }
  }
}
