package net.firefive.derby.ontrack;

import net.firefive.derby.boutmodel.events.AbstractBoutEvent;
import net.firefive.interaction.Event;

public class AbstractBoutEventWrapper extends Event {
  
  private AbstractBoutEvent wrapped;
  
  public AbstractBoutEventWrapper(AbstractBoutEvent wrapped)
  {
    this.wrapped = wrapped;
  }
  
  public AbstractBoutEvent unwrap() {
    return wrapped;
  }
  
  public String toString()
  {
    return wrapped.getClass().getSimpleName() + " (in " + getClass().getSimpleName() + ")";
  }
}
