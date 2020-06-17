package net.firefive.derby.ontrack;

import java.awt.Graphics;
import java.util.ArrayList;

import net.firefive.derby.boutmodel.BoutStateModel;
import net.firefive.derby.boutmodel.JamPlayState;
import net.firefive.derby.boutmodel.PeriodPlayState;
import net.firefive.derby.boutmodel.PeriodState;
import net.firefive.derby.boutmodel.events.JamStateEvent;
import net.firefive.derby.boutmodel.events.PeriodStateEvent;
import net.firefive.interaction.Event;
import net.firefive.interaction.Region;

public class PeriodClock extends AbstractClock {

  private static final String BEFORE = "Start period";
  private static final String RUNNING = "Official timeout";
  private static final String PAUSED = "Restart clock";

  private Arena model;

  public PeriodClock(Arena surface, Region region) {
    super(BoutStateModel.PERIOD_DURATION, surface, region);
    model = surface;

    ArrayList<String> popupOptions = new ArrayList<String>();
    popupOptions.add(BEFORE);
    setPopupOptions(popupOptions);
  }

  @Override
  public void handleEvent(Event evt) {

    if (evt instanceof AbstractBoutEventWrapper) {

      // bout state changed?
      AbstractBoutEventWrapper wrapper = (AbstractBoutEventWrapper) evt;
      if (wrapper.unwrap() instanceof PeriodStateEvent) {
        ArrayList<String> popupOptions = new ArrayList<String>();
        PeriodState ps = ((PeriodStateEvent) wrapper.unwrap()).getState();
        if (ps.getPlayState() == PeriodPlayState.BEFORE) {
          popupOptions.add(BEFORE);
          resetTimer();
        }
        else if (ps.getPlayState() == PeriodPlayState.RUNNING) {
          popupOptions.add(RUNNING);
          startTimer();
        }
        else if (ps.getPlayState() == PeriodPlayState.PAUSED) {
          popupOptions.add(PAUSED);
          stopTimer();
          setElapsedTime(BoutStateModel.PERIOD_DURATION - ps.getRemainingTime());
        }
        // Don't add any popup items in PeriodState.AFTER - next
        // PeriodState.BEFORE only reachable via event fired from frame menu bar
        setPopupOptions(popupOptions);
      }

      // if a jam has started, ensure that the bout is running
      if (wrapper.unwrap() instanceof JamStateEvent)
        if (model.getJamState().getPlayState() == JamPlayState.DURING)
          model.startPlay();
    }
  }

  @Override
  public void draw(Graphics g) {
    super.draw(g);
    // check timer for end at each repaint
    if (timeUp())
      model.endPeriod();
  }

  /*
   * popup menu overrides
   */
  @Override
  public void popupOptionSelected(String option) {

    // Note: newPeriod() only ever called from elsewhere (to avoid user error)

    if (option == BEFORE)
      model.startPlay();
    if (option == RUNNING)
      model.officialTimeout();
    if (option == PAUSED)
      model.startPlay();
  }
}