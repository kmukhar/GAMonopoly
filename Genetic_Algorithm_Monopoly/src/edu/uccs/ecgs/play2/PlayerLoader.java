package edu.uccs.ecgs.play2;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.Random;

import edu.uccs.ecgs.players.AbstractPlayer;
import edu.uccs.ecgs.players.ChromoTypes;

public class PlayerLoader {

  private static PlayerLoader _ref = new PlayerLoader();
  private Properties playerFiles;
  
  private PlayerLoader() {
    InputStream is = 
        PlayerLoader.class.getResourceAsStream("data/playerfiles.properties");
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

  public static PlayerLoader getLoader() {
    return _ref;
  }

  private AbstractPlayer loadPlayer(int index, String path) {
    DataInputStream dis = null;
    AbstractPlayer player = null;

    try {
      InputStream is = PlayerLoader.class.getResourceAsStream("data/" + path);
      dis  = new DataInputStream(is);

      char[] header = new char[3];
      header[0] = dis.readChar();
      header[1] = dis.readChar();
      header[2] = dis.readChar();
      
      String headerStr = new String(header);

      ChromoTypes chromoType = ChromoTypes.valueOf(headerStr);
      player = chromoType.getPlayer(index, dis);
      player.setSourceName(path);

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

  public AbstractPlayer[] get4Players() {
    AbstractPlayer[] players = new AbstractPlayer[4];

    Collection<Object> vals = playerFiles.values();
    ArrayList<Object> filenames = new ArrayList<Object>();
    filenames.addAll(vals);

    Random r = new Random(System.currentTimeMillis());

    for (int i = 0; i < players.length; i++) {
      int playerIndex = i + 1;
      Object path = filenames.remove(r.nextInt(filenames.size()));
      players[i] = this.loadPlayer(playerIndex, path.toString());
    }

    return players;
  }	
}
