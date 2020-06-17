package net.firefive.interaction.components;

import java.awt.Color;
import java.awt.Graphics;

import net.firefive.interaction.Mobility;
import net.firefive.interaction.Region;
import net.firefive.interaction.Surface;
import net.firefive.interaction.TimeDriven;

public  class Timer extends TextBox implements TimeDriven {

  public static final boolean INCREASING = true;
  public static final boolean DECREASING = false;

  private long duration;
  private boolean direction;

  private boolean stopped;
  private long elapsedTime;
  private long lastStartTime;

  public Timer(long duration, boolean direction, Surface surface, Region region, Color background,
      Mobility mobility) {
    super("88:88", surface, region, background, mobility);
    setRounded(true);

    this.duration = duration;
    this.direction = direction;
    resetTimer();
  }

  @Override
  public void draw(Graphics g) {
    if (stopped && inAlternateDrawingMode()) {
      setText("");
    }
    else {
      long time = getElapsedTime();
      if (time >= duration) {
        stopTimer();
        time = duration;
      }
      if (direction == DECREASING)
        time = duration - time;
      setText(getTimeString(time));
    }
    super.draw(g);
  }

  private String getTimeString(long time) {
    long secs = (time / 1000) % 86400;
    long mins = secs / 60;

    String s = "00" + (secs % 60);
    return Long.toString(mins) + ":" + s.substring(s.length() - 2);
  }

  public void setElapsedTime(long time) {
    elapsedTime = time > duration ? duration : time;
    // don't want to double-count time since timer started
    // so (in case it's running now):
    lastStartTime = System.currentTimeMillis();
  }

  public void startTimer() {
    if (!stopped)
      return;
    stopped = false;
    lastStartTime = System.currentTimeMillis();
  }

  public void stopTimer() {
    if (stopped)
      return;
    stopped = true;
    elapsedTime += System.currentTimeMillis() - lastStartTime;
  }

  public void resetTimer() {
    stopped = true;
    elapsedTime = 0;
    lastStartTime = 0;
  }

  public boolean isStopped() {
    return stopped;
  }

  public long getElapsedTime() {
    if (stopped)
      return elapsedTime;
    return elapsedTime + System.currentTimeMillis() - lastStartTime;
  }

  public boolean timeUp() {
    return getElapsedTime() >= duration;
  }

  @Override
  public String toString() {
    return this.getClass().getName() + "[duration=" + duration + ",direction=" + direction + ",elapsedTime="
        + elapsedTime + ",lastStartTime=" + lastStartTime + ",stopped=" + stopped + "]";
  }
}
