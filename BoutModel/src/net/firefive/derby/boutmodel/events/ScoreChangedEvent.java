package net.firefive.derby.boutmodel.events;

import net.firefive.derby.boutmodel.TeamId;

public class ScoreChangedEvent extends AbstractBoutEvent {

  private TeamId teamId;
  private int points;

  public ScoreChangedEvent(TeamId teamId, int points) {
    this.teamId = teamId;
    this.points = points;
  }

  public TeamId getTeamId() {
    return teamId;
  }

  public int getPoints() {
    return points;
  }

}
