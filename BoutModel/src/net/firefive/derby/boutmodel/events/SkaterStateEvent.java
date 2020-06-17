package net.firefive.derby.boutmodel.events;

import net.firefive.derby.boutmodel.Skater;

public class SkaterStateEvent extends AbstractBoutEvent {

  private Skater skater;

  public SkaterStateEvent(Skater skater) {
    this.skater =  skater;
  }

  public Skater getSkater() {
    return skater;
  }
}
