package net.firefive.derby.boutmodel.events;

public interface BoutEventStream {

  public boolean hasMoreEvents();
  
  public AbstractBoutEvent getNextEvent();

}
