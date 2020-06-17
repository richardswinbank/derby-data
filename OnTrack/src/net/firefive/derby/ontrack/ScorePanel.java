package net.firefive.derby.ontrack;

import net.firefive.derby.boutmodel.JamPlayState;
import net.firefive.derby.boutmodel.TeamId;
import net.firefive.derby.boutmodel.events.AbstractBoutEvent;
import net.firefive.derby.boutmodel.events.JamStateEvent;
import net.firefive.derby.boutmodel.events.JammerPassEvent;
import net.firefive.interaction.Event;
import net.firefive.interaction.Region;
import net.firefive.interaction.components.FixedGroup;

public class ScorePanel extends FixedGroup {

  private Arena arena;
  private TeamBench bench;

  public ScorePanel(TeamBench bench, Arena arena, Region r) {
    super(arena, r);
    this.arena = arena;
    this.bench = bench;

    double incr = r.width / 7;
    double x = r.x;
    if (bench == TeamBench.LEFT)
      x += r.width - incr;

    addChild(new Counter(arena, new Region(x, r.y, incr, r.height)) {
      @Override
      public void handleEvent(Event evt) {
        if (evt instanceof AbstractBoutEventWrapper) {
          AbstractBoutEvent abe = ((AbstractBoutEventWrapper) evt).unwrap();
          if (abe instanceof JamStateEvent)
            if (((JamStateEvent) abe).getState().getPlayState() == JamPlayState.BEFORE)
              reset();
          if (abe instanceof JammerPassEvent) {
            JammerPassEvent jpe = (JammerPassEvent) abe;
            if (jpe.getPoints() >= 0 && getTeamBench(jpe.getTeamId()) == bench)
              increment();
          }
        }
      }
    });

    double width = 7 * incr / 9;
    if (bench == TeamBench.RIGHT)
      x += incr - width;
    Region buttonRegion = new Region(x, r.y + r.height / 8, width, r.height * .75);

    if (bench == TeamBench.LEFT)
      incr *= -1;
    for (int i = 0; i < 6; i++) {
      buttonRegion.x += incr;
      addChild(new ScoreButton(i, buttonRegion));
    }

  }

  private TeamBench getTeamBench(TeamId teamId) {
    return arena.getBench(teamId);
  }

  public class ScoreButton extends TeamButton {

    private int score;

    public ScoreButton(int score, Region region) {
      super("+5", bench, arena, region);
      setText(score >= 0 ? "+" + score : Integer.toString(score));
      this.score = score;
    }

    @Override
    public void buttonPushed() {
      arena.completePass(bench, score);
    }
  }
}
