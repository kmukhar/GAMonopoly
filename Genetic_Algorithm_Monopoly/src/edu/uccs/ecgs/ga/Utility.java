package edu.uccs.ecgs.ga;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.logging.*;

import javax.swing.JFileChooser;
import edu.uccs.ecgs.players.ChromoTypes;

public class Utility
{
  private static String rootDir;
  private static String rootDir2;

  public synchronized Path getDirForGen(int generation)
  {
    return getDirForGen(null, null, generation);
  }

  public static synchronized Path getDirForGen(ChromoTypes chromoType,
      FitEvalTypes fitEval, int generation)
  {
    File f = null;

    String datapath = System.getProperty("datapath");
    if (datapath == null)
      datapath = "";

    if (Main.useGui) {
      if (rootDir == null || rootDir.equals("")) {
        JFileChooser fc = new JFileChooser(datapath);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setDialogTitle("Select directory to save data files");
        int returnVal = JFileChooser.CANCEL_OPTION;
        while (returnVal != JFileChooser.APPROVE_OPTION) {
          returnVal = fc.showDialog(null, "Select");
        }

        f = fc.getSelectedFile();
        rootDir = f.getAbsolutePath();
        System.out.println("Log dir: " + rootDir);
      } else {
        f = new File(rootDir);
      }
    } else {
      // not using gui
      if (rootDir2 == null) {
        // String dataDirName = System.getProperty("dataDirName") + "_"
        // + System.getProperty("evaluator").toLowerCase();

        // f = new File(dataDirName);
        f = new File("data");
        rootDir2 = f.getAbsolutePath();
        System.out.println("Log dir: " + rootDir2);
      } else {
        f = new File(rootDir2);
      }
    }

    Path path = FileSystems.getDefault().getPath(f.getAbsolutePath());
    path = path.resolve(chromoType.toString()).resolve(
        fitEval.get().getDirName());

    if (generation < 10) {
      path = path.resolve("Generation_0000" + generation);
    } else if (generation < 100) {
      path = path.resolve("Generation_000" + generation);
    } else if (generation < 1000) {
      path = path.resolve("Generation_00" + generation);
    } else if (generation < 10000) {
      path = path.resolve("Generation_0" + generation);
    } else if (generation < 100000) {
      path = path.resolve("Generation_" + generation);
    }

    f = path.toFile();
    if (!f.exists()) {
      f.mkdirs();
    }

    return path;
  }

  /**
   * Create a formatter and set logging on or off depending on state of
   * {@link Main#debug}. If Main.debug is true, then logging is
   * turned on; if debug is false, logging is turned off.
   */
  public static Logger initLogger(int generation, int match, int game,
                                  String gamekey)
  {
    Logger logger = null;
    if (Main.debug != Level.OFF) {
      logger = Logger.getLogger(gamekey);
      logger.setLevel(Main.debug);

      logFileSetup(logger, generation, match, game);
    }
    return logger;
  }

  /**
   * Create the log file based on generation, match, and game number, and add
   * the formatter to the logger.
   */
  public static void logFileSetup(Logger logger, int generation, int match,
                                  int game)
  {
    Path dir = getDirForGen(Main.chromoType,
        Main.fitnessEvaluator, generation);

    dir = dir.resolve(getMatchString(match).toString());
    File file = dir.toFile();
    if (!file.exists()) {
      file.mkdir();
    }

    StringBuilder fileName = new StringBuilder();
    fileName.append(getGameString(game)).append(".rtf");

    try {
      FileHandler fh = new FileHandler(dir.resolve(fileName.toString())
          .toString(), false);
      logger.addHandler(fh);

      Formatter formatter = new Formatter() {
        @Override
        public String format(LogRecord record) {
          return record.getMessage() + "\n";
        }
      };

      fh.setFormatter(formatter);

    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  /**
   * @return A string of the form "Match_nnn" where nnn is the match number.
   */
  private static StringBuilder getMatchString(int match) {
    StringBuilder result = new StringBuilder("" + match);

    while (result.length() < 3) {
      result.insert(0, 0);
    }

    result.insert(0, "Match_");

    return result;
  }

  /**
   * @return A string of the form "Game_nnn" where nnn is the game number.
   */
  private static String getGameString(int game) {
    StringBuilder result = new StringBuilder("" + game);

    while (result.length() < 3) {
      result.insert(0, 0);
    }

    result.insert(0, "Game_");

    return result.toString();
  }

}
