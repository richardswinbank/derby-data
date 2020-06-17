package net.firefive.interaction.components;

import java.awt.Color;

import net.firefive.interaction.Mobility;
import net.firefive.interaction.Region;
import net.firefive.interaction.Surface;

public abstract class Button extends TextBox {

  private Color box;
  private Color text;

  public Button(String text, Surface surface, Region region, Color background) {
    super(text, surface, region, background, Mobility.FIXED);
    setRounded(true);
  }

  @Override
  public void handleMousePress() {
    initialiseColours();
    super.setBoxColour(text);
    super.setTextColour(box);
  }

  @Override
  public void handleMouseRelease() {
    initialiseColours();
    super.setBoxColour(box);
    super.setTextColour(text);
  }

  @Override
  public void handleMouseClick() {
    buttonPushed();
  }

  private void initialiseColours() {
    if (box == null) {
      box = getBoxColour();
      text = getTextColour();
    }
  }

  @Override
  public void setBoxColour(Color c) {
    box = c;
    super.setBoxColour(c);
  }

  @Override
  public void setTextColour(Color c) {
    text = c;
    super.setTextColour(c);
  }

  public abstract void buttonPushed();
}
