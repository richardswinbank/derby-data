package net.firefive.derby.ontrack;

import net.firefive.derby.boutmodel.JamPlayState;
import net.firefive.derby.boutmodel.events.JamStateEvent;
import net.firefive.derby.boutmodel.events.NewBoutEvent;
import net.firefive.interaction.Event;
import net.firefive.interaction.Region;
import net.firefive.interaction.Surface;
import net.firefive.interaction.components.Label;

public class JamCounter extends Label {

  private int jamNumber;

  public JamCounter(Surface surface, Region region) {
    super("Period 8", surface, region);
    setJamNumber(1);
  }

  private void setJamNumber(int i) {
    this.jamNumber = i;
    setText("Jam " + jamNumber);
  }

  @Override
  public void handleEvent(Event evt) {
    if (evt instanceof AbstractBoutEventWrapper) {
      AbstractBoutEventWrapper wrapper = (AbstractBoutEventWrapper) evt;
      if (((AbstractBoutEventWrapper) evt).unwrap() instanceof JamStateEvent)
        if (((JamStateEvent) wrapper.unwrap()).getState().getPlayState() == JamPlayState.BEFORE)
          setJamNumber(jamNumber + 1);

      if (((AbstractBoutEventWrapper) evt).unwrap() instanceof NewBoutEvent)
        setJamNumber(1);
    }
  }
}
