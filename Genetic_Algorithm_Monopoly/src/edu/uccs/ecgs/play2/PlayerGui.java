package edu.uccs.ecgs.play2;

import java.awt.*;
import java.awt.event.KeyEvent;
import javax.swing.*;
import edu.uccs.ecgs.ga.Location;
import edu.uccs.ecgs.ga.PropertyFactory;

@SuppressWarnings("serial")
public class PlayerGui extends JPanel {
  /**
   * Create the GUI and show it. For thread safety, this method should be
   * invoked from the event dispatch thread.
   */
  private static void createAndShowGUI()
  {
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
    frame.setVisible(true);
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
