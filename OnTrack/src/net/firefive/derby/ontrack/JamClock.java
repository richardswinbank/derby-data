package net.firefive.derby.ontrack;

import java.awt.Graphics;
import java.util.ArrayList;

import net.firefive.derby.boutmodel.BoutStateModel;
import net.firefive.derby.boutmodel.JamPlayState;
import net.firefive.derby.boutmodel.JamState;
import net.firefive.derby.boutmodel.events.JamStateEvent;
import net.firefive.interaction.Event;
import net.firefive.interaction.Region;

public class JamClock extends AbstractClock {

  private static final String BEFORE = "Start jam";
  private static final String DURING = "Stop jam";
  private static final String AFTER = "Reset";

  private Arena model;
  
  public JamClock(Arena surface, Region region) {
    super(BoutStateModel.JAM_DURATION, surface, region);
    model = surface;
    
    ArrayList<String> popupOptions = new ArrayList<String>();
    popupOptions.add(BEFORE);
    setPopupOptions(popupOptions);
  }

  @Override
  public void handleEvent(Event evt) {
    if (evt instanceof AbstractBoutEventWrapper) {
      AbstractBoutEventWrapper wrapper = (AbstractBoutEventWrapper) evt;

      if (wrapper.unwrap() instanceof JamStateEvent) {
        ArrayList<String> popupOptions = new ArrayList<String>();
        JamState js = ((JamStateEvent)wrapper.unwrap()).getState();
        if (js.getPlayState() == JamPlayState.BEFORE) {
          popupOptions.add(BEFORE);
          resetTimer();
        } else if (js.getPlayState() == JamPlayState.DURING) {
          popupOptions.add(DURING);
          startTimer();
        } else if (js.getPlayState() == JamPlayState.AFTER) {
          popupOptions.add(AFTER);
          stopTimer();
          setElapsedTime(BoutStateModel.JAM_DURATION - js.getRemainingTime());
        }
        setPopupOptions(popupOptions);
      }
    }
  }
  
  @Override
  public void draw(Graphics g)
  {
    super.draw(g);
    // check timer for end at each repaint
   if(timeUp())
      model.stopJam();
  }

  /*
   * popup menu overrides
   */
  @Override
  public void popupOptionSelected(String option) {
    if (option == BEFORE)
      model.startJam();
    if (option == DURING)
      model.stopJam();
    if (option == AFTER)
      model.resetJam();;
  }
}