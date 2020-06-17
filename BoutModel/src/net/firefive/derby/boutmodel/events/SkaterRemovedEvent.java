package net.firefive.derby.boutmodel.events;

import net.firefive.derby.boutmodel.Skater;

public class SkaterRemovedEvent extends SkaterStateEvent {

  public SkaterRemovedEvent(Skater skater) {
    super(skater);
  }

}
