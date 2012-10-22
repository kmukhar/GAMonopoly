package edu.uccs.ecgs.play2;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.*;
import javax.swing.*;
import edu.uccs.ecgs.ga.*;
import edu.uccs.ecgs.players.AbstractPlayer;
import edu.uccs.ecgs.players.HumanPlayer;

@SuppressWarnings("serial")
public class PlayerGui extends JPanel {
  private static String playerName;
  private static int playerIndex;
  private ArrayList<String> dataNames = new ArrayList<String>();

  /**
   * Create the GUI and show it. For thread safety, this method should be
   * invoked from the event dispatch thread.
   */
  private static void createAndShowGUI()
  {
    showInitialDialog();
    getNameAndIndex();

    // Create and set up the window.
    JFrame frame = new JFrame("Monopoly Simulator");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    frame.setLayout(new BorderLayout());
    // Add content to the window.
    PlayerGui gui = new PlayerGui();
    frame.add(gui, BorderLayout.CENTER);

    frame.add(gui.getSouthBorder(), BorderLayout.SOUTH);
    frame.add(gui.getWestBorder(), BorderLayout.WEST);
    frame.add(gui.getNorthBorder(), BorderLayout.NORTH);
    frame.add(gui.getEastBorder(), BorderLayout.EAST);

    // Display the window.
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
    
    gui.playGame();
  }

  private void playGame()
  {
    AbstractPlayer[] players = createPlayers();
  }

  private AbstractPlayer[] createPlayers()
  {
    Random r = new Random();
    AbstractPlayer[] players = new AbstractPlayer[4];

    for (int i = 0; i < players.length; i++) {
      if (i == playerIndex) {
        HumanPlayer player = new HumanPlayer(i, playerName);
        players[i] = player;
      } else {
        int index = r.nextInt(dataNames.size());
        String path = dataNames.remove(index);
        AbstractPlayer player = PlayerLoader.loadPlayer(path, i);
        players[i] = player;
      }
    }

    return players;
  }

  private static void showInitialDialog()
  {
    Calendar endCal = GregorianCalendar.getInstance();
    // set end date for research to 15 Dec 2012 
    endCal.set(2012, 11, 15, 12, 0, 0);
    
    Calendar nowCal = GregorianCalendar.getInstance();
    String path = "About.html";
    if (nowCal.after(endCal)) {
      path = "About2.html";
    }
    
    JLabel label = new JLabel();
    InputStream is = PlayerGui.class.getResourceAsStream(path);
    InputStreamReader isr = new InputStreamReader(is);
    BufferedReader br = new BufferedReader(isr); 

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
      aboutMsg.append("Monopoly Simulator");
    } finally {
      try {
        br.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    label.setText(aboutMsg.toString());

    int result = JOptionPane.showConfirmDialog(null, label,
        "About this program", JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.INFORMATION_MESSAGE);
    
    if (result == JOptionPane.CANCEL_OPTION) {
      System.exit(0);
    }
  }

  private static void getNameAndIndex()
  {
    final NameAndIndexDialog dialog = new NameAndIndexDialog(
        new javax.swing.JFrame(), true);
    dialog.addWindowListener(new java.awt.event.WindowAdapter() {

      @Override
      public void windowClosing(WindowEvent e)
      {
        // do nothing
      }

      @Override
      public void windowClosed(java.awt.event.WindowEvent e)
      {
        PlayerGui.playerName = dialog.getName();
        PlayerGui.playerIndex = dialog.getIndex();
      }
    });
    dialog.setLocationRelativeTo(null);
    dialog.setVisible(true);
  }

  public static void main(String[] args)
  {
    // Schedule a job for the event dispatch thread:
    // creating and showing this application's GUI.
    SwingUtilities.invokeLater(new Runnable() {
      public void run()
      {
        // Turn off metal's use of bold fonts
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        createAndShowGUI();
      }
    });
  }

  static String factoryKey = "edu.uccs.ecgs.play";
  static PropertyFactory factory = PropertyFactory
      .getPropertyFactory(factoryKey);

  public PlayerGui() {
    super(new GridLayout(1, 2));

    for (int i = 0; i < 10; i++) {
      String baseName = "player000";
      dataNames.add(baseName + i + ".dat");
    }

    JTabbedPane tabbedPane = new JTabbedPane();

    JComponent panel1 = makeTextPanel("Panel #1");
    tabbedPane.addTab("Tab 1", panel1);
    tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

    JComponent panel2 = makeTextPanel("Panel #2");
    tabbedPane.addTab("Tab 2", panel2);
    tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

    JComponent panel3 = makeTextPanel("Panel #3");
    tabbedPane.addTab("Tab 3", panel3);
    tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

    JComponent panel4 = makeTextPanel("Panel #4 (has a preferred size of 410 x 50).");
    panel4.setPreferredSize(new Dimension(180, 50));
    tabbedPane.addTab("Tab 4", panel4);
    tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);

    // Add the tabbed pane to this panel.
    add(tabbedPane);

    // The following line enables to use scrolling tabs.
    tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    
    JTextArea textArea = new JTextArea();
    textArea.setEditable(false);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);

    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setPreferredSize(new Dimension(180,50));
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    
    JTextField columnHead = new JTextField("Game Information");
    columnHead.setEditable(false);
    columnHead.setPreferredSize(new Dimension(180,22));
    scrollPane.setColumnHeaderView(columnHead);

    add(scrollPane);
  }

  protected JComponent makeTextPanel(String text)
  {
    JPanel panel = new JPanel(false);
    JLabel filler = new JLabel(text);
    filler.setHorizontalAlignment(JLabel.CENTER);
    panel.setLayout(new GridLayout(1, 1));
    panel.add(filler);
    return panel;
  }

  private JPanel getSouthBorder()
  {
    JPanel south = new JPanel();
    south.setLayout(new GridLayout(1, 0));

    for (int i = 10; i >= 0; i--) {
      Location l = factory.getLocationAt(i);
      LocationButton lb = new LocationButton(l);
      south.add(lb);
    }

    return south;
  }

  private JPanel getWestBorder()
  {
    JPanel west = new JPanel();
    west.setLayout(new GridLayout(0, 1));

    for (int i = 19; i > 10; i--) {
      Location l = factory.getLocationAt(i);
      LocationButton lb = new LocationButton(l);
      west.add(lb);
    }

    return west;
  }

  private JPanel getNorthBorder()
  {
    JPanel north = new JPanel();
    north.setLayout(new GridLayout(1, 0));

    for (int i = 20; i <= 30; i++) {
      Location l = factory.getLocationAt(i);
      LocationButton lb = new LocationButton(l);
      north.add(lb);
    }

    return north;
  }

  private JPanel getEastBorder()
  {
    JPanel east = new JPanel();
    east.setLayout(new GridLayout(0, 1));

    for (int i = 31; i < 40; i++) {
      Location l = factory.getLocationAt(i);
      LocationButton lb = new LocationButton(l);
      east.add(lb);
    }

    return east;
  }
}
