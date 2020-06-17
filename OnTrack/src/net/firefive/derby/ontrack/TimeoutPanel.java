package net.firefive.derby.ontrack;

import java.awt.Color;

import net.firefive.derby.boutmodel.PeriodPlayState;
import net.firefive.derby.boutmodel.Team;
import net.firefive.derby.boutmodel.TeamId;
import net.firefive.derby.boutmodel.events.AbstractBoutEvent;
import net.firefive.derby.boutmodel.events.NewBoutEvent;
import net.firefive.derby.boutmodel.events.PeriodStateEvent;
import net.firefive.derby.boutmodel.events.TeamUpdatedEvent;
import net.firefive.interaction.Drawable;
import net.firefive.interaction.Event;
import net.firefive.interaction.Mobility;
import net.firefive.interaction.Region;
import net.firefive.interaction.components.FixedGroup;
import net.firefive.interaction.components.TextBox;

public class TimeoutPanel extends FixedGroup {

  private Arena arena;
  private TeamBench bench;

  public TimeoutPanel(TeamBench bench, Arena arena, Region r) {
    super(arena, r);
    this.arena = arena;
    this.bench = bench;

    double buttonWidth = r.width / 6;
    Region buttonRegion = new Region(r.x, r.y, buttonWidth, r.height);
    double incr = r.width / 5;
    if (bench == TeamBench.RIGHT) {
      buttonRegion.x += r.width - buttonWidth;
      incr *= -1;
    }

    addChild(new ReviewButton(buttonRegion));
    buttonRegion.x += incr * 2;
    addChild(new TimeoutButton(buttonRegion));
    buttonRegion.x += incr;
    addChild(new TimeoutButton(buttonRegion));
    buttonRegion.x += incr;
    addChild(new TimeoutButton(buttonRegion));
  }

  @Override
  public void handleEvent(Event ae) {
    if (ae instanceof AbstractBoutEventWrapper) {
      AbstractBoutEvent abe = ((AbstractBoutEventWrapper) ae).unwrap();

      // reset colours at team update
      if (abe instanceof TeamUpdatedEvent) {
        TeamId teamId = ((TeamUpdatedEvent) abe).getTeamId();
        if (arena.getBench(teamId) == bench)
          for (Drawable d : this)
            ((TimeoutButton) d).setColours();
      }

      else if (abe instanceof PeriodStateEvent) {
        PeriodStateEvent pse = (PeriodStateEvent) ((AbstractBoutEventWrapper) ae).unwrap();

        // reinstate official review for new period
        if (pse.getState().getPlayState() == PeriodPlayState.BEFORE) {
          for (Drawable d : this)
            if (d instanceof ReviewButton)
              d.setVisible(true);
        }

        // hide button for used review/timeout
        else if (pse.getState().getPlayState() == PeriodPlayState.PAUSED && arena.getBench(pse.getTeam()) == bench) {
          //System.out.println(pse);
          for (Drawable d : this) {
//            System.out.println(d);
            if (pse.isReview() && d instanceof ReviewButton)
              d.setVisible(false);
            if (!pse.isReview() && !(d instanceof ReviewButton) && d.isVisible()) {
              d.setVisible(false);
              break;
            }
          }
        }
      }

      // reinstate all timeouts for a new bout
      else if (abe instanceof NewBoutEvent) {
        for (Drawable d : this)
          d.setVisible(true);
      }
    }
  }

  public class TimeoutButton extends TextBox {

    public TimeoutButton(Region region) {
      super("XX", arena, region, Color.WHITE, Mobility.BOUND);
      setText("TO");
      setRounded(true);
      setColours();
    }

    public void setColours() {
      Team team = arena.getTeam(bench);
      setBoxColour(team.getPrimaryColour());
      setTextColour(team.getSecondaryColour());
    }

    public TeamBench getBench() {
      return bench;
    }
  }

  public class ReviewButton extends TimeoutButton {

    public ReviewButton(Region region) {
      super(region);
      setText("OR");
    }

  }
}
