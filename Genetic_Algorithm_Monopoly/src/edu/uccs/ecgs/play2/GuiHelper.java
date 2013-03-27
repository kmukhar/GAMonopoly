package edu.uccs.ecgs.play2;

import java.awt.Component;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import javax.swing.Icon;
import javax.swing.JOptionPane;

public class GuiHelper {
  protected static int intResult;
  protected static String strResult;
  protected static Object objResult;

  public static String showInputDialog(final Component parent,
                                       final String msg, final String title,
                                       final int msgType)
  {
    try {
      EventQueue.invokeAndWait(new Runnable() {
        @Override
        public void run()
        {
          GuiHelper.strResult = JOptionPane.showInputDialog(parent, msg, title,
              msgType);
        }
      });
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    return strResult;
  }

  public static Object showInputDialog(final Component parent,
                                         final String msg, final String title,
                                         final int msgType, final Icon icon,
                                         final Object[] values,
                                         final Object initial)
  {
    try {
      EventQueue.invokeAndWait(new Runnable() {
        @Override
        public void run()
        {
          GuiHelper.objResult = JOptionPane.showInputDialog(parent, msg, title,
              msgType, icon, values, initial);
        }
      });
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    return objResult;
  }

  public static int showConfirmDialog(final Component parent, final String msg,
                                      final String title, final int optionType)
  {
    try {
      EventQueue.invokeAndWait(new Runnable() {
        @Override
        public void run()
        {
          GuiHelper.intResult = JOptionPane.showConfirmDialog(parent, msg,
              title, optionType);
        }
      });
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    return intResult;
  }

  public static int showConfirmDialog(final Component parent, final String msg,
                                      final String title, final int optionType,
                                      final int msgType)
  {
    try {
      EventQueue.invokeAndWait(new Runnable() {
        @Override
        public void run()
        {
          GuiHelper.intResult = JOptionPane.showConfirmDialog(parent, msg,
              title, optionType, msgType);
        }
      });
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    return intResult;
  }

  public static int showOptionDialog(final Component parent, final Object msg,
                                     final String title, final int optionType,
                                     final int msgType, final Icon icon,
                                     final Object[] options,
                                     final Object initial)
  {
    try {
      EventQueue.invokeAndWait(new Runnable() {
        @Override
        public void run()
        {
          GuiHelper.intResult = JOptionPane.showOptionDialog(parent, msg,
              title, optionType, msgType, icon, options, initial);
        }
      });
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    return intResult;
  }

  public static void showMessageDialog(final Component parent,
                                       final String msg, final String title,
                                       final int optionType)
  {
    try {
      EventQueue.invokeAndWait(new Runnable() {
        @Override
        public void run()
        {
          JOptionPane.showMessageDialog(parent, msg, title, optionType);
        }
      });
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
