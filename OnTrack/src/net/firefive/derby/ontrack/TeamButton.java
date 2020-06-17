package net.firefive.derby.ontrack;

import java.awt.Color;

import net.firefive.derby.boutmodel.Team;
import net.firefive.derby.boutmodel.TeamId;
import net.firefive.derby.boutmodel.events.TeamUpdatedEvent;
import net.firefive.interaction.Event;
import net.firefive.interaction.Region;
import net.firefive.interaction.components.Button;

public abstract class TeamButton extends Button {

  private Arena surface;
  private TeamBench bench;

  public TeamButton(String text, TeamBench bench, Arena surface, Region region) {
    super(text, surface, region, Color.WHITE);
    this.bench = bench;
    this.surface = surface;
  }
  
  public TeamBench getBench() {
    return bench;
  }

  @Override
  public void handleEvent(Event e) {
    if (e instanceof AbstractBoutEventWrapper) {
      AbstractBoutEventWrapper evt = (AbstractBoutEventWrapper) e;

      if (evt.unwrap() instanceof TeamUpdatedEvent) {
        TeamId teamId = ((TeamUpdatedEvent) evt.unwrap()).getTeamId();
        if (surface.getBench(teamId) == bench) {
          Team team = surface.getTeam(teamId);
          setBoxColour(team.getPrimaryColour());
          setTextColour(team.getSecondaryColour());
        }
      }
    }
  }
}
