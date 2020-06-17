package net.firefive.derby.boutmodel.events;

import net.firefive.derby.boutmodel.Team;
import net.firefive.derby.boutmodel.TeamId;

public class TeamUpdatedEvent extends AbstractBoutEvent {

  private TeamId teamId;
  private Team team;

  public TeamUpdatedEvent(TeamId teamId, Team team) {
    this.teamId = teamId;
    this.team = team;
  }

  public TeamId getTeamId() {
    return teamId;
  }
  
  public Team getTeam() {
    return team;
  }
}
