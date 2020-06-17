package net.firefive.derby.ontrack;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;

import net.firefive.interaction.Drawable;
import net.firefive.interaction.Mobility;
import net.firefive.interaction.Region;
import net.firefive.interaction.Surface;

public class Tape extends Drawable {

  private HazardTapeSquare tape;

  public Tape(Surface surface, Region region) {
    super(surface, region, Mobility.FIXED);
  }

  @Override
  public void draw(Graphics g) {
    Region r = getRegion();
    tape = new HazardTapeSquare(20);
    final Graphics2D g2 = (Graphics2D) g.create();
    try {
      g2.setPaint(new TexturePaint(tape, new Rectangle(0, 0, tape.getWidth(), tape.getHeight())));
      g2.fillRect((int) r.x, (int) r.y, (int) r.width, (int) r.height);
    } finally {
      g2.dispose();
    }
  }

  private class HazardTapeSquare extends BufferedImage {

    public HazardTapeSquare(int width) {
      super(width, width, BufferedImage.TYPE_INT_RGB);
      Graphics2D g2 = createGraphics();
      for (int x = -width; x < width; x++) {
        if (x % 10 == 0)
          g2.setColor(g2.getColor() == Color.BLACK ? Color.YELLOW : Color.BLACK);
        g2.drawLine(x, 0, x + width, width);
      }
    }
  }
}
