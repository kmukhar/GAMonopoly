package edu.uccs.ecgs.ga;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class Gui extends JFrame {

  JButton button = null;
  private Main program;
  private ArrayList<JComponent> textFields = new ArrayList<JComponent>();

  public JTextField matchNum = new JTextField(3);
  public JTextField genNum = new JTextField(4);
	private JTextField tfLoadGenNum;

  private final static String PAUSE_GAMES = "Pause All Games";
  private final static String RESUME_GAMES = "Resume All Games";
  public Gui(Main main) {
    this.program = main;
  }
  
  public void init(Object[][] fields) {
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setTitle("Simulation with chromosome type " + Main.chromoType);

    this.getContentPane().add(getSimPanel(fields));
    this.pack();
    this.setVisible(true);
    assert program != null;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private JPanel getSimPanel(Object[][] fields) {
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();

    gbc.gridy = 0;
    gbc.insets = new Insets(2,2,2,2);

    for (Object[] field : fields) {
      JLabel label = new JLabel(field[0].toString());

      JComponent choice = null;
      if (field[1] instanceof String) {
        choice = new JTextField(field[1].toString(), 6);
      } else {
        choice = new JComboBox((Object[])field[1]);
        setComboBoxSelection((JComboBox) choice, field[0].toString());
      }

      textFields.add(choice);
      if ("Generation to load".equalsIgnoreCase(field[0].toString())) {
      	tfLoadGenNum = (JTextField) choice;
      }
      
      gbc.gridx = 0;
      gbc.anchor = GridBagConstraints.EAST;
      gbc.ipadx = 0;
      panel.add(label, gbc);

      gbc.anchor = GridBagConstraints.WEST;
      gbc.gridx += 1;
      gbc.ipadx = 40;
      panel.add(choice, gbc);
      
      gbc.gridy += 1;
    }

    changeGenNumEditable(program.loadFromDisk);

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
          button.setText(PAUSE_GAMES);
          Thread t = new Thread(new Runnable(){
            @Override
            public void run() {
              setExecutionValues();
              startSimulation();
            }});
          t.start();
        } else if (button.getText().equalsIgnoreCase(PAUSE_GAMES)) {
          // button currently says Pause All Games
          button.setText(RESUME_GAMES);
          program.pause();
        } else {
          // button currently says Resume All Games
          button.setText(PAUSE_GAMES);
          program.resume();
        }
      }
    });

    return panel;
  }

  @SuppressWarnings("rawtypes")
  private void setComboBoxSelection(JComboBox choice, String label) {
    if (label.equalsIgnoreCase(Main.loadFromDiskLabel)) {
    	JComboBox comboBox = (JComboBox) choice;
      comboBox.setSelectedItem(program.loadFromDisk);
      comboBox.addItemListener(new ItemListener(){
				@Override
        public void itemStateChanged(ItemEvent e) {
					changeGenNumEditable(e.getItem());
        }});
    } else if (label.equalsIgnoreCase(Main.randomSeedLabel)) {
      ((JComboBox) choice).setSelectedItem(Main.useRandomSeed);      
    } else if (label.equalsIgnoreCase(Main.fitEvalLabel)){
      ((JComboBox) choice).setSelectedItem(Main.fitnessEvaluator);
    } else if (label.equalsIgnoreCase(Main.debugLabel)) {
      ((JComboBox) choice).setSelectedItem(Main.debug.toString());
    } else if (label.equalsIgnoreCase(Main.chromoLabel)) {
      ((JComboBox) choice).setSelectedItem(Main.chromoType);
    } else if (label.equalsIgnoreCase(Main.allowTradingLabel)) {
      ((JComboBox) choice).setSelectedItem(Main.allowPropertyTrading);
    }
  }

  protected void changeGenNumEditable(Object item) {
		if (item.equals(LoadTypes.NO_LOAD)) {
			tfLoadGenNum.setEditable(false);
		} else {
			tfLoadGenNum.setEditable(true);
		}
  }

	private void setExecutionValues()
  {
    int i = 0;
    for (JComponent choice : textFields) {
      if (choice instanceof JTextField) {
        JTextField field = (JTextField) choice;
        field.setEditable(false);
        String value = field.getText();
        program.setExecutionValue(i++, value);
      } else {
        JComboBox<?> field = (JComboBox<?>) choice;
        field.setEditable(false);
        program.setExecutionValue(i++, field.getSelectedItem());
      }
    }
    startSimulation();
  }

  private void startSimulation() {
    assert program!= null;
    program.startSimulation();
  }
}
