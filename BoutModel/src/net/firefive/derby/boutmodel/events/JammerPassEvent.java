package net.firefive.derby.boutmodel.events;

import net.firefive.derby.boutmodel.TeamId;

public class JammerPassEvent extends ScoreChangedEvent {

  public JammerPassEvent(TeamId teamId, int points) {
    super(teamId, points);
  }
}
