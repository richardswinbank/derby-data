package net.firefive.derby.boutmodel.events;

import net.firefive.derby.boutmodel.PeriodState;
import net.firefive.derby.boutmodel.TeamId;

public class PeriodStateEvent extends AbstractBoutEvent {

  private PeriodState state;
  private TeamId team;
  private boolean isReview;

  public PeriodStateEvent(PeriodState state, TeamId team, boolean isReview) {
    this.state = state;
    this.team = team;
    this.isReview = isReview;
  }

  public PeriodState getState() {
    return state;
  }

  public TeamId getTeam() {
    return team;
  }

  public boolean isReview() {
    return isReview;
  }
}
