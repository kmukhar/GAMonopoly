package edu.uccs.ecgs.play;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;

import edu.uccs.ecgs.ga.PopulationPropagator;
import edu.uccs.ecgs.players.AbstractPlayer;

public class LoadButtonListener implements ActionListener {

	private PlayerPanel playerPanel;
	private final JFileChooser fc = new JFileChooser("D:/Documents and Data/Kevin/git/GAMonopoly/Genetic_Algorithm_Monopoly/data/RGA/finish_order/Generation_00000");

	public LoadButtonListener(PlayerPanel p) {
		playerPanel = p;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		int returnVal = fc.showOpenDialog(playerPanel);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();

			int index = Integer.parseInt(file.getName().substring(6, 10));
			AbstractPlayer player = PopulationPropagator.loadPlayer(
			    file, index);
			
			playerPanel.setPlayer(player);
		}
	}
}
