package edu.uccs.ecgs.play2;

import java.io.*;
import edu.uccs.ecgs.players.AbstractPlayer;
import edu.uccs.ecgs.players.ChromoTypes;

public class PlayerLoader {

	public static AbstractPlayer loadPlayer(String path, int index) {
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
}
