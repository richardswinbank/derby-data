package net.firefive.derby.boutmodel.events;

import com.google.gson.Gson;

public abstract class AbstractBoutEvent {

  private static final Gson gson = new Gson();

  private long eventTime;

  public AbstractBoutEvent() {
    this.eventTime = System.currentTimeMillis();
  }

  public String toString() {
    return getClass().getCanonicalName() + ":" + gson.toJson(this);
  }

  public long getEventTime() {
    return eventTime;
  }
}
