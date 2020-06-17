package net.firefive.interaction.components;

import java.awt.Color;

import net.firefive.interaction.Event;
import net.firefive.interaction.Mobility;
import net.firefive.interaction.Region;
import net.firefive.interaction.Surface;

public class Label extends TextBox {
  
  public Label(String text, Surface surface, Region region) {
    super(text, surface, region, Color.WHITE, Mobility.FIXED);
    setTextColour(Color.BLACK);
    setFilled(false);
  }

  @Override
  public void handleEvent(Event evt) {
    // default implementation - override if label text needs to change
  }
}
