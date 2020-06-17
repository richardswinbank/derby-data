package net.firefive.derby.boutmodel.events;

import net.firefive.derby.boutmodel.JamState;
import net.firefive.derby.boutmodel.TeamId;

public class JamStateEvent extends AbstractBoutEvent {

  private JamState state;
  private TeamId team;

  public JamStateEvent(JamState state, TeamId team) {
    this.state = state;
    this.team = team;
  }

  public JamState getState() {
    return state;
  }
  
  public boolean causedByTeam() {
    return team != null;
  }

  public TeamId getTeam() {
    return this.team;
  }
}
