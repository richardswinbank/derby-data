package net.firefive.interaction.components;

import java.awt.Graphics;

import net.firefive.interaction.Drawable;
import net.firefive.interaction.Mobility;
import net.firefive.interaction.Region;
import net.firefive.interaction.Surface;

public abstract class FixedGroup extends Drawable {

  public FixedGroup(Surface surface, Region region) {
    super(surface, region, Mobility.FIXED);
  }

  @Override
  public void draw(Graphics g) {
    // do nothing - this is a transparent grouping device
  }
}
