package edu.uccs.ecgs.play2;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class InfoDialog {

  static int showOptionDialog(String filename, String[] options)
  {
    InputStream is = PlayerGui.class.getResourceAsStream(filename);
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
  
    StringBuilder aboutMsg = new StringBuilder();
  
    String line = null;
    try {
      line = br.readLine();
      while (line != null) {
        aboutMsg.append(line);
        line = br.readLine();
      }
    } catch (IOException e) {
      aboutMsg = new StringBuilder();
      aboutMsg.append("Monopoly");
    } finally {
      try {
        br.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  
    final JEditorPane editorPane = getEditorPane(aboutMsg.toString());
  
    JScrollPane sp = new JScrollPane(editorPane);
    sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    sp.setBorder(null);

    JFrame frame = new JFrame();
    frame.setIconImage(PlayerGui.monopolyIcon.getImage());
    
    Object defOption = null; 
    if (options != null && options.length > 1) 
      defOption = options[0];
    int result = JOptionPane.showOptionDialog(frame, sp, "About this program",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE,
        null, options, defOption);
    frame.dispose();

    return result;
  }

  public static void showFinalDialog(String filename, String gameStats)
  {
    InputStream is = PlayerGui.class.getResourceAsStream(filename);
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
  
    StringBuilder aboutMsg = new StringBuilder();
  
    String line = null;
    try {
      line = br.readLine();
      while (line != null) {
        aboutMsg.append(line);
        line = br.readLine();
      }
    } catch (IOException e) {
      aboutMsg = new StringBuilder();
      aboutMsg.append("Monopoly");
    } finally {
      try {
        br.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    String msg = aboutMsg.toString().replaceFirst("RESULTS", gameStats);
    gameStats = gameStats.replace("%0D%0A", "<br>\n");
    msg = msg.replaceFirst("RESULTS", gameStats);
    final JEditorPane editorPane = getEditorPane(msg);

    JScrollPane sp = new JScrollPane(editorPane);
    sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    sp.setBorder(null);

    JFrame frame = new JFrame();
    frame.setIconImage(PlayerGui.monopolyIcon.getImage());
    
    Object defOption = null; 

    int result = JOptionPane.showOptionDialog(frame, sp, "About this program",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE,
        null, null, defOption);
    frame.dispose();
  }

  /**
   * @param msg The text for the pane
   * @return An instance of JEditorPane with the string
   */
  private static JEditorPane getEditorPane(String msg)
  {
    System.setProperty("awt.useSystemAAFontSettings", "on");
    final JEditorPane editorPane = new JEditorPane();
    editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES,
        Boolean.TRUE);
    editorPane.setPreferredSize(new Dimension(500, 475));
    editorPane.setEditable(false);
    editorPane.setContentType("text/html");
    editorPane.setText(msg);
  
    // This section of code from
    // https://forums.oracle.com/forums/message.jspa?messageID=9909614
    Color c = new Color(214, 217, 223); // default color for JOptionPane
    UIDefaults defaults = new UIDefaults();
    defaults.put("EditorPane[Enabled].backgroundPainter", c);
    editorPane.putClientProperty("Nimbus.Overrides", defaults);
    editorPane.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
    editorPane.setBackground(c);
    // end code https://forums.oracle.com/forums/message.jspa?messageID=9909614
  
    // the code for resizing the editorpane is from
    // http://java.dzone.com/tips/tip-displaying-rich-messages-u
    // set editor pane to be resizeable
    editorPane.addHierarchyListener(new HierarchyListener() {
      public void hierarchyChanged(HierarchyEvent e)
      {
        Window window = SwingUtilities.getWindowAncestor(editorPane);
        if (window instanceof Dialog) {
          Dialog dialog = (Dialog) window;
          if (!dialog.isResizable()) {
            dialog.setResizable(true);
          }
        }
      }
    });
  
    // the code for processing hyperlinks is from
    // http://java.dzone.com/tips/tip-displaying-rich-messages-u
    // Add Hyperlink listener to process hyperlinks
    editorPane.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(final HyperlinkEvent e)
      {
        if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
          EventQueue.invokeLater(new Runnable() {
            public void run()
            {
              // Show hand cursor
              SwingUtilities.getWindowAncestor(editorPane).setCursor(
                  Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
              // Show URL as the tooltip
              editorPane.setToolTipText(e.getURL().toExternalForm());
            }
          });
        } else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
          EventQueue.invokeLater(new Runnable() {
            public void run()
            {
              // Show default cursor
              SwingUtilities.getWindowAncestor(editorPane).setCursor(
                  Cursor.getDefaultCursor());
              // Reset tooltip
              editorPane.setToolTipText(null);
            }
          });
        } else if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          if (Desktop.isDesktopSupported()) {
            try {
              Desktop.getDesktop().browse(e.getURL().toURI());
            } catch (Exception ignored) {
            }
          }
        }
      }
    });
    return editorPane;
  }
}
