package net.firefive.derby.boutmodel.events;

import net.firefive.derby.boutmodel.Skater;

public class SkaterAddedEvent extends SkaterStateEvent {

  public SkaterAddedEvent(Skater skater) {
    super(skater);
  }

}
