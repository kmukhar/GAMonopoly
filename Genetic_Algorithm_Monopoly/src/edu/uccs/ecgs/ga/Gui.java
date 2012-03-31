package edu.uccs.ecgs.ga;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class Gui extends JFrame {

  JButton button = null;
  private Main program;
  private ArrayList<JTextField> textFields = new ArrayList<JTextField>();

  public JTextField matchNum = new JTextField(3);
  public JTextField genNum = new JTextField(4);

  public Gui(Main main) {
    this.program = main;
  }
  
  public void init(String[][] fields) {
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setTitle("Simulation with chromosome type " + Main.chromoType);

    this.getContentPane().add(getSimPanel(fields));
    this.pack();
    this.setVisible(true);
    assert program != null;
  }

  private JPanel getSimPanel(String[][] fields) {
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();

    gbc.gridy = 0;
    gbc.insets = new Insets(2,2,2,2);

    for (String[] field : fields) {
      JLabel label = new JLabel(field[0]);
      JTextField text = new JTextField(field[1], 3);
      textFields.add(text);
      
      gbc.gridx = 0;
      gbc.anchor = GridBagConstraints.EAST;
      gbc.ipadx = 0;
      panel.add(label, gbc);

      gbc.anchor = GridBagConstraints.WEST;
      gbc.gridx += 1;
      gbc.ipadx = 40;
      panel.add(text, gbc);
      
      gbc.gridy += 1;
    }
    
    JPanel genPanel = new JPanel();
    genPanel.add(new JLabel("Generation: "));
    genPanel.add(genNum);
    genNum.setText("0");
    genNum.setEditable(false);
    gbc.gridx = 0;
    gbc.anchor = GridBagConstraints.EAST;
    gbc.ipadx = 0;
    panel.add(genPanel, gbc);
    
    JPanel matchPanel = new JPanel();
    matchPanel.add(new JLabel("Match: "));
    matchPanel.add(matchNum);
    matchNum.setText("0");
    matchNum.setEditable(false);
    gbc.anchor = GridBagConstraints.WEST;
    gbc.gridx += 1;
    gbc.ipadx = 40;
    panel.add(matchPanel, gbc);
    gbc.gridy += 1;

    button = new JButton("Start All Games");
    gbc.gridx = 0;
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    panel.add(button, gbc);

    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        if (!Main.started) {
          button.setText("Pause All Games");
          Thread t = new Thread(new Runnable(){
            @Override
            public void run() {
              setExecutionValues();
              startSimulation();
            }});
          t.start();
        } else if (Main.paused) {
          // button currently says Run Monopoly
          button.setText("Pause All Games");
          Main.resume();
        } else {
          // button currently says Pause Monopoly
          button.setText("Restart All Games");
          Main.pause();
        }
      }
    });

    return panel;
  }

  private void setExecutionValues()
  {
    int i = 0;
    for (JTextField text : textFields) {
      text.setEditable(false);
      Main.setExecutionValue(i++, text.getText());
    }
    startSimulation();
  }

  private void startSimulation() {
    assert program!= null;
    program.startSimulation();
  }
}
