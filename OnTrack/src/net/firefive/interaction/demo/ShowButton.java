package net.firefive.interaction.demo;

import java.awt.Color;

import net.firefive.interaction.Region;
import net.firefive.interaction.Surface;
import net.firefive.interaction.components.Button;

public class ShowButton extends Button {

  private Surface surface;

  public ShowButton(Surface surface, Region region) {
    super("Show all", surface, region, Color.WHITE);
    this.surface = surface;
    setBoxColour(Color.GRAY);
    setTextColour(Color.WHITE);
  }

  @Override
  public void buttonPushed() {
    surface.publishEvent(new ShowBlobsEvent());
  }
}
