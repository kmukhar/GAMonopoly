package edu.uccs.ecgs.play;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JTable;

import edu.uccs.ecgs.ga.Location;
import edu.uccs.ecgs.ga.PropertyFactory;

public class RemoveLotActionListener implements ActionListener {
  JTable table;

  public RemoveLotActionListener(JComboBox<Location> comboBox, JTable table) {
    super();
    this.table = table;
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    if (table.getSelectedRow() == -1) {
      return;
    }

    MTableModel tm = (MTableModel) table.getModel();
    Location lot = (Location) tm.getValueAt(table.getSelectedRow(), table.getSelectedColumn());
    lot.setOwner(null);
    PropertyFactory.getPropertyFactory(PlayerGui.factoryKey).checkForMonopoly();

    tm.removeItem(lot);
    PlayerGui.addLotToList(lot);
  }

}
