package net.firefive.derby.ontrack;

import java.awt.Color;

import net.firefive.interaction.Mobility;
import net.firefive.interaction.Region;
import net.firefive.interaction.components.TextBox;

public abstract class Counter extends TextBox {

  private int count;

  public Counter(Arena surface, Region region) {
    super("88", surface, region, Color.WHITE, Mobility.FIXED);
    setRounded(true);
    reset();
  }

  private void setCount(int count) {
    this.count = count;
    setText(Integer.toString(count));
  }

  public void reset() {
    setCount(0);
  }

  public void increment() {
    setCount(count + 1);
  }
}
