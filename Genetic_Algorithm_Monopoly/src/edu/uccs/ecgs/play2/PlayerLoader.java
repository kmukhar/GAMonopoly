package edu.uccs.ecgs.play2;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

import edu.uccs.ecgs.players.AbstractPlayer;
import edu.uccs.ecgs.players.ChromoTypes;

public class PlayerLoader {

  private static PlayerLoader _ref = new PlayerLoader();
  private Properties playerFiles;

  /**
   * Constructor. Loads all file names found in edu/uccs/ecgs/play2/data
   * into a properties file where the file name is the property key, and
   * the value is null. Each file name must be the name of a player data file
   * as output by the GAEngine class in the package edu.uccs.ecgs.ga.
   */
  private PlayerLoader() {
    InputStream is = 
        PlayerLoader.class.getResourceAsStream("data/datafiles.properties");
    
    playerFiles = new Properties();

    try {
      playerFiles.load(is);
    } catch (IOException ignored) {
    } finally {
      try {
        is.close();
      } catch (IOException ignored) {
      }
    }
  }

  /**
   * @return A reference to this object
   */
  public static PlayerLoader getLoader() {
    return _ref;
  }

  /**
   * Loads a player from a data file
   * @param index The player index for the AbstractPlayer instance
   * @param filename The file name of the data file
   * @return An instance of AbstractPlayer
   */
  private AbstractPlayer loadPlayer(int index, String filename) {
    DataInputStream dis = null;
    AbstractPlayer player = null;

    try {
      InputStream is = PlayerLoader.class.getResourceAsStream("data/" + filename);
      dis  = new DataInputStream(is);

      char[] header = new char[3];
      header[0] = dis.readChar();
      header[1] = dis.readChar();
      header[2] = dis.readChar();
      
      String headerStr = new String(header);

      ChromoTypes chromoType = ChromoTypes.valueOf(headerStr);
      player = chromoType.getPlayer(index, dis);
      player.setSourceName(filename);
      player.setTradingParameters();

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (dis != null) {
        try {
          dis.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    
    return player;
	}

  /**
   * @return A 4 element array of 3 AbstractPlayers loaded from randomly
   *         selected data files; the 4th element is empty. The caller should
   *         insert the 4th player into the array appropriately (add to end or
   *         insert into middle and move other players).
   */
  public ArrayList<AbstractPlayer> get3Players() {
    ArrayList<AbstractPlayer> players = new ArrayList<AbstractPlayer>();

    ArrayList<Object> filenames = new ArrayList<Object>();
    filenames.addAll(playerFiles.keySet());

    Random r = new Random(System.currentTimeMillis());

    for (int i = 0; i < 3; i++) {
      int playerIndex = i + 1;
      Object path = filenames.remove(r.nextInt(filenames.size()));
      players.add(this.loadPlayer(playerIndex, path.toString()));
    }

    return players;
  }	
}
