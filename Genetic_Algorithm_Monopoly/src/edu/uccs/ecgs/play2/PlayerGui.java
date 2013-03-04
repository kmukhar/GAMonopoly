package edu.uccs.ecgs.play2;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import edu.uccs.ecgs.ga.*;
import edu.uccs.ecgs.players.AbstractPlayer;
import edu.uccs.ecgs.players.HumanPlayer;

@SuppressWarnings("serial")
public class PlayerGui extends JPanel {
  private static String playerName;
  private static int playerIndex;
  private static NameAndIndexDialog dialog;
  private ArrayList<String> dataNames = new ArrayList<String>();
  private JButton startButton;
  // private JButton pauseButton;
  private JButton resignButton;
  private JButton nextButton;

  private PlayerPanel[] playerPanels;
  private AbstractPlayer[] players;
  private Main main;
  private Monopoly game;
  private JTextArea gameInfo;

  private Thread gameThread;
  private GameController controller;
  private Hashtable<Location, LocationButton> locationButtons;

  public static PropertyFactory factory;

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
    gui.game = new Monopoly(0, 0, 0, gui.players);
    PlayerGui.factory = PropertyFactory.getPropertyFactory(gui.game.gamekey);

    frame.addWindowListener(new java.awt.event.WindowAdapter() {
      @Override
      public void windowClosing(java.awt.event.WindowEvent e)
      {
        System.exit(0);
      }
    });

    frame.add(gui, BorderLayout.CENTER);

    frame.add(gui.getSouthBorder(), BorderLayout.SOUTH);
    frame.add(gui.getWestBorder(), BorderLayout.WEST);
    frame.add(gui.getNorthBorder(), BorderLayout.NORTH);
    frame.add(gui.getEastBorder(), BorderLayout.EAST);

    for (AbstractPlayer player : gui.players) {
      for (LocationButton lb : gui.locationButtons.values()) {
        player.addChangeListener(lb);
      }
    }

    // Display the window.
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);

    gui.playGame();
  }

  private void playGame()
  {
    main = new Main();
    main.pause();
    Main.maxTurns = 500; // set a high number so the game can run to finish

    controller = new GameController(game);
    game.setFlowController(controller);
    game.setLogger(createTextAreaLogger());

    gameThread = new Thread(game);
  }

  private AbstractPlayer[] createPlayers()
  {
    Random r = new Random(System.currentTimeMillis());

    playerName = dialog.getName();
    playerIndex = dialog.getIndex();

    AbstractPlayer[] players = new AbstractPlayer[4];
    for (int i = 0; i < 4; i++) {
      String baseName = "player000";
      dataNames.add(baseName + i + ".dat");
    }

    for (int i = 0; i < players.length; i++) {
      AbstractPlayer player;
      if (i == playerIndex) {
        player = new HumanPlayer(i, playerName);
        players[i] = player;
      } else {
        int index = r.nextInt(dataNames.size());
        String path = dataNames.remove(index);
        player = PlayerLoader.loadPlayer(path, i+1);
        players[i] = player;
      }
    }
    return players;
  }

  private static void showInitialDialog()
  {
    Calendar endCal = GregorianCalendar.getInstance();
    // set end date for research to 30 Mar 2013
    endCal.set(2013, 2, 30, 12, 0, 0);

    Calendar nowCal = GregorianCalendar.getInstance();
    String path = "About.html";
    if (nowCal.after(endCal)) {
      path = "About2.html";
    }

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

    System.setProperty("awt.useSystemAAFontSettings", "on");
    final JEditorPane editorPane = new JEditorPane();
    editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES,
        Boolean.TRUE);
    editorPane.setPreferredSize(new Dimension(500, 420));
    editorPane.setEditable(false);
    editorPane.setContentType("text/html");
    editorPane.setText(aboutMsg.toString());

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

    JScrollPane sp = new JScrollPane(editorPane);
    sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    sp.setBorder(null);

    int result = JOptionPane.showConfirmDialog(null, sp, "About this program",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);

    if (result == JOptionPane.CANCEL_OPTION) {
      System.exit(0);
    }
  }

  private static void getNameAndIndex()
  {
    dialog = new NameAndIndexDialog(new javax.swing.JFrame(), true);
    dialog.setLocationRelativeTo(null);
    dialog.setVisible(true);
  }

  public static void main(String[] args)
  {
    /* Set the Nimbus look and feel */

    /*
     * If Nimbus (introduced in Java SE 6) is not available, stay with the
     * default look and feel. For details see
     * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
     */
    try {
      for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager
          .getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
          javax.swing.UIManager.setLookAndFeel(info.getClassName());
          break;
        }
      }
    } catch (Exception ex) {
      java.util.logging.Logger.getLogger(PlayerGui.class.getName()).log(
          java.util.logging.Level.SEVERE, null, ex);
    }

    /* Create and display the dialog */
    java.awt.EventQueue.invokeLater(new Runnable() {
      public void run()
      {
        createAndShowGUI();
      }
    });
  }

  public PlayerGui() {
    setLayout(new BorderLayout());

    locationButtons = new Hashtable<Location, LocationButton>();

    players = createPlayers();

    JTabbedPane tabbedPane = new JTabbedPane();

    playerPanels = new PlayerPanel[4];
    int tabWidth = 48;

    for (int i = 0; i < playerPanels.length; i++) {
      PlayerPanel panel = makePlayerPanel();
      playerPanels[i] = panel;
      tabbedPane.addTab("<html><body><table width='" + tabWidth + "'>"
          + players[i].getName() + "</table></body></html>", panel);
      playerPanels[i].setPlayer(players[i]);
    }

    tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
    tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
    tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);
    tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);

    // Add the tabbed pane to this panel.
    JPanel gamePanel = new JPanel();
    gamePanel.setLayout(new GridLayout(1, 2));
    gamePanel.add(tabbedPane);

    // The following line enables to use scrolling tabs.
    tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

    gameInfo = new JTextArea();
    gameInfo.setEditable(false);
    gameInfo.setLineWrap(true);
    gameInfo.setWrapStyleWord(true);

    JScrollPane scrollPane = new JScrollPane(gameInfo);
    scrollPane.setPreferredSize(new Dimension(180, 50));
    scrollPane
        .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane
        .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    JTextField columnHead = new JTextField("Game Information");
    columnHead.setEditable(false);
    columnHead.setPreferredSize(new Dimension(180, 22));
    scrollPane.setColumnHeaderView(columnHead);

    gamePanel.add(scrollPane);

    JPanel buttonPanel = new JPanel();
    createStartButton();
    buttonPanel.add(startButton);

    // createPauseButton();
    // buttonPanel.add(pauseButton);

    createNextButton();
    buttonPanel.add(nextButton);

    createResignButton();
    buttonPanel.add(resignButton);

    add(buttonPanel, BorderLayout.NORTH);
    add(gamePanel, BorderLayout.CENTER);

  }

  protected PlayerPanel makePlayerPanel()
  {
    PlayerPanel panel = new PlayerPanel();
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
      locationButtons.put(l, lb);
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
      locationButtons.put(l, lb);
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
      locationButtons.put(l, lb);
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
      locationButtons.put(l, lb);
    }

    return east;
  }

  /**
   * 
   */
  private void createResignButton()
  {
    resignButton = new JButton("Resign");
    resignButton.setEnabled(false);
    resignButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0)
      {
        // TODO Auto-generated method stub
      }
    });
  }

  /**
   * 
   */
  private void createPauseButton()
  {
    // pauseButton = new JButton("Pause");
    // pauseButton.setEnabled(false);
    // pauseButton.addActionListener(new ActionListener(){
    // @Override
    // public void actionPerformed(ActionEvent arg0)
    // {
    // if (pauseButton.getText().equalsIgnoreCase("pause")) {
    // main.pause();
    // pauseButton.setText("Resume");
    // } else {
    // pauseButton.setText("Pause");
    // main.resume();
    // game.resume();
    // }
    // }});
  }

  /**
   * 
   */
  private void createNextButton()
  {
    nextButton = new JButton("Step");
    nextButton.setEnabled(false);
    nextButton.setMnemonic(KeyEvent.VK_S);
    nextButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0)
      {
        main.resume();
        controller.resume();
      }
    });
  }

  /**
   * 
   */
  private void createStartButton()
  {
    startButton = new JButton("Start");
    startButton.setMnemonic(KeyEvent.VK_S);
    startButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0)
      {
        startButton.setEnabled(false);
        nextButton.setEnabled(true);

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
          public void run()
          {
            startGame();
          }
        });
      }
    });
  }

  private void startGame()
  {
    Thread t = new Thread(new Runnable() {
      @Override
      public void run()
      {
        gameThread.start();

        try {
          gameThread.join();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        AbstractPlayer[] players = game.getAllPlayers();
        AbstractPlayer winner = null;
        for (AbstractPlayer player : players) {
          if (player.getFinishOrder() == 1)
            winner = player;
        }
        JOptionPane.showMessageDialog(null,
            "The Game Is Over. " + winner.getName() + " is the winner.");
        System.exit(0);
      }});
    t.start();
  }

  /**
   * @return
   */
  private Logger createTextAreaLogger()
  {
    Logger logger = Logger.getLogger("edu.uccs.ecgs");
    logger.setLevel(Level.INFO);
    Handler h = new TextAreaHandler(gameInfo);
    logger.addHandler(h);
    return logger;
  }

  /**
   * @param lot
   * @return
   */
  public LocationButton getButtonForLocation(Location lot) {
    return locationButtons.get(lot);
  }
}
