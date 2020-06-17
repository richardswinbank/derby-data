package net.firefive.derby.ontrack;

import java.awt.Color;

import net.firefive.derby.boutmodel.JamPlayState;
import net.firefive.derby.boutmodel.events.AbstractBoutEvent;
import net.firefive.derby.boutmodel.events.JamStateEvent;
import net.firefive.derby.boutmodel.events.PackLapEvent;
import net.firefive.interaction.Event;
import net.firefive.interaction.Region;
import net.firefive.interaction.components.Button;
import net.firefive.interaction.components.FixedGroup;

public class LapCounter extends FixedGroup {

  public LapCounter(Arena arena, Region r) {
    super(arena, r);

    addChild(new Counter(arena, new Region(r.x + r.width/5, r.y, r.width * 3/5, r.height /2)) {
      @Override
      public void handleEvent(Event evt) {
        if (evt instanceof AbstractBoutEventWrapper) {
          AbstractBoutEvent abe = ((AbstractBoutEventWrapper) evt).unwrap();
          if (abe instanceof JamStateEvent)
            if (((JamStateEvent) abe).getState().getPlayState() == JamPlayState.BEFORE)
              reset();
          if (abe instanceof PackLapEvent)
            increment();
        }
      }
    });

    Button lapButton = new Button("Pack lap", arena, new Region(r.x, r.y + 11 * r.height / 16, r.width, 5*r.height / 16),
        Color.WHITE) {
      @Override
      public void buttonPushed() {
        arena.packCompletedLap();
      }
    };
    lapButton.setBoxColour(Color.LIGHT_GRAY);
    lapButton.setTextColour(Color.BLACK);
    addChild(lapButton);
  }
}
