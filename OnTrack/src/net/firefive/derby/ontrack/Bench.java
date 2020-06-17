package net.firefive.derby.ontrack;

import net.firefive.derby.boutmodel.PlayZone;
import net.firefive.derby.boutmodel.Skater;
import net.firefive.interaction.Point;
import net.firefive.interaction.Region;

public class Bench extends SkaterContainer {

  private TeamBench bench; // which bench this is

  public Bench(Arena surface, Region region, TeamBench bench) {
    super(surface, region);
    this.bench = bench;
  }

  public TeamBench getTeamBench() {
    return bench;
  }

  @Override
  public boolean isContainerFor(Skater skater) {
    if (skater.getZone() == PlayZone.BENCH && getSkaterBench(skater) == this.bench)
      return true;
    return false;
  }

  @Override
  public void positionSkaters() {

    Region r = getRegion();

    double xUnit = r.width / 26;
    double yUnit = r.height / 16;

    int i = 0;
    for (AbstractSkater s : skaters()) {
      if(!this.isContainerFor(s.getSkater()))
        continue; // possible if other container has taken ownership but not yet called removed() here
      int x = i % 5;
      int y = i / 5;
      s.setCentre(new Point(r.x + (xUnit * 3) + (xUnit * 5 * x), r.y + (yUnit * 3) + (yUnit * 5 * y)));
      i++;
    }
  }
}
