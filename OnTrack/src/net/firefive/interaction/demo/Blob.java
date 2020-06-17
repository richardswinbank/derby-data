package net.firefive.interaction.demo;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import net.firefive.interaction.Drawable;
import net.firefive.interaction.Event;
import net.firefive.interaction.Mobility;
import net.firefive.interaction.Region;
import net.firefive.interaction.Surface;

public class Blob extends Drawable {

  private Color color;

  public Blob(Color c, Surface surface, Region r, Mobility mobility) {
    super(surface, r, mobility);
    this.color = c;
    
    ArrayList<String> popupOptions = new ArrayList<String>();
    popupOptions.add("Hide");
    setPopupOptions(popupOptions);
  }

  public void setColor(Color c) {
    color = c;
  }

  public Color getColor() {
    return color;
  }

  @Override
  public void draw(Graphics g) {
    g.setColor(color);
    Region r = getRegion();
    // System.out.println(r);
    g.fillOval((int) r.x, (int) r.y, (int) r.width, (int) r.height);
    g.drawOval((int) r.x, (int) r.y, (int) r.width, (int) r.height);
  }  

  @Override
  public void handleEvent(Event evt) {
    if (evt instanceof ShowBlobsEvent)
      setVisible(true);
  }

  @Override
  public void popupOptionSelected(String option) {
    if(option.equals("Hide"))
      setVisible(false);
  }
}
