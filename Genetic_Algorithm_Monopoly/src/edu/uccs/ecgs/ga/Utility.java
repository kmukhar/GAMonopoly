package edu.uccs.ecgs.ga;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import javax.swing.JFileChooser;

public class Utility
{
  private static String rootDir;

  public static synchronized Path getDirForGen(int generation)
  {
    return getDirForGen(null, null, generation);
  }

  public static synchronized Path getDirForGen(ChromoTypes chromoType,
      FitEvalTypes fitEval, int generation)
  {
    File f = null;
    if (rootDir == null || rootDir.equals("")) {
      if (Main.useGui) {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setDialogTitle("Select directory to save data files");
        int returnVal = JFileChooser.CANCEL_OPTION;
        while (returnVal != JFileChooser.APPROVE_OPTION) {
          returnVal = fc.showDialog(null, "Select");
        }

        f = fc.getSelectedFile();
      } else {
        // not using gui
        String dataDirName = System.getProperty("dataDirName");
        f = new File(dataDirName);
      }

      rootDir = f.getAbsolutePath();
      System.out.println("Log dir: " + rootDir);
    }

    Path path = FileSystems.getDefault().getPath(rootDir);
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
}
