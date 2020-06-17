package net.firefive.derby.boutmodel;

import net.firefive.derby.boutmodel.events.AbstractBoutEvent;

public interface BoutObserver {
  
  public void handleBoutEvent(AbstractBoutEvent evt);

}
