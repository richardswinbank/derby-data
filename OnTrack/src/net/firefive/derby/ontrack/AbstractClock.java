package net.firefive.derby.ontrack;

import java.awt.Color;

import net.firefive.interaction.Mobility;
import net.firefive.interaction.Region;
import net.firefive.interaction.components.Timer;

public abstract class AbstractClock extends Timer {
  public AbstractClock(long duration, Arena surface, Region region) {
    super(duration // Timer durations are in milliseconds
        , Timer.DECREASING, surface, region, Color.WHITE, Mobility.FIXED);
  }
}
