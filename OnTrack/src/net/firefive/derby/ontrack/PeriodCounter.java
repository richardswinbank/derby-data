package net.firefive.derby.ontrack;

import net.firefive.derby.boutmodel.PeriodPlayState;
import net.firefive.derby.boutmodel.events.NewBoutEvent;
import net.firefive.derby.boutmodel.events.PeriodStateEvent;
import net.firefive.interaction.Event;
import net.firefive.interaction.Region;
import net.firefive.interaction.Surface;
import net.firefive.interaction.components.Label;

public class PeriodCounter extends Label {

  private int periodNumber;

  public PeriodCounter(Surface surface, Region region) {
    super("Period 8", surface, region);
    setPeriodNumber(1);
  }

  private void setPeriodNumber(int i) {
    this.periodNumber = i;
    setText("Period " + periodNumber);
  }

  @Override
  public void handleEvent(Event evt) {
    if (evt instanceof AbstractBoutEventWrapper) {
      AbstractBoutEventWrapper wrapper = (AbstractBoutEventWrapper) evt;
      
      if (((AbstractBoutEventWrapper) evt).unwrap() instanceof PeriodStateEvent)
        if (((PeriodStateEvent) wrapper.unwrap()).getState().getPlayState() == PeriodPlayState.BEFORE)
          setPeriodNumber(periodNumber + 1);
      
      if (((AbstractBoutEventWrapper) evt).unwrap() instanceof NewBoutEvent)
        setPeriodNumber(1);
    }
  }
}
