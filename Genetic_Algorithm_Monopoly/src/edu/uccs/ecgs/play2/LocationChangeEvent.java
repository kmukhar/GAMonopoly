package edu.uccs.ecgs.play2;

import javax.swing.event.ChangeEvent;
import edu.uccs.ecgs.ga.Location;

public class LocationChangeEvent extends ChangeEvent {

  private Location previous;

  public LocationChangeEvent(Object arg0, Location oldLocation) {
    super(arg0);
    previous = oldLocation;
  }

  public Location getPreviousLocation() {
    return previous;
  }
}
