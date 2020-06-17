package net.firefive.interaction.demo;

import java.awt.Color;
import java.awt.Graphics;

import net.firefive.interaction.Mobility;
import net.firefive.interaction.Region;
import net.firefive.interaction.Surface;
import net.firefive.interaction.TimeDriven;

public class FlashyBlob extends Blob implements TimeDriven {

  private Color color1;
  private Color color2;

  public FlashyBlob(Color c1, Color c2, Surface surface, Region r, Mobility mobility) {
    super(c1, surface, r, mobility);
    this.color1 = c1;
    this.color2 = c2;
  }

  @Override
  public void draw(Graphics g) {
    if (inAlternateDrawingMode())
      setColor(color2);
    else
      setColor(color1);
    super.draw(g);
  }
}
