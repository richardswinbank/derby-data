package net.firefive.derby.ontrack;

import net.firefive.derby.boutmodel.PlayZone;
import net.firefive.derby.boutmodel.Skater;
import net.firefive.derby.boutmodel.StarSite;
import net.firefive.interaction.Drawable;
import net.firefive.interaction.Point;
import net.firefive.interaction.Region;

public class PenaltyBox extends SkaterContainer {

  public PenaltyBox(Arena surface, Region region) {
    super(surface, region);
  }

  @Override
  public boolean isContainerFor(Skater skater) {
    if (skater.getZone() == PlayZone.BOX)
      return true;
    return false;
  }

  @Override
  public void positionSkaters() {
    Region r = getRegion();

    double xUnit = r.width / 31;
    double yUnit = r.height / 16;

    int leftBlockers = 1;
    double jammerOffsetLeft = r.x + xUnit * 13;
    double yOffsetLeft = r.y + yUnit * 3;

    int rightBlockers = 1;
    double jammerOffsetRight = r.x + xUnit * 18;
    double yOffsetRight = r.y + yUnit * 3;

    for (Drawable e : this) {
      double x = 0;
      double y = 0;
      AbstractSkater s = (AbstractSkater) e;

      // seat positions for skater on LEFT bench
      if (s.getBench() == TeamBench.LEFT) {
        x = jammerOffsetLeft;
        y = yOffsetLeft;
        if (s.getSkater().getStar() == StarSite.NO_STAR) {
          x -= xUnit * 5 * (leftBlockers % 3);
          y += yUnit * 5 * (leftBlockers / 3);
          leftBlockers++;
        }
      }

      // seat positions for skater on RIGHT bench
      if (s.getBench() == TeamBench.RIGHT) {
        x = jammerOffsetRight;
        y = yOffsetRight;
        if (s.getSkater().getStar() == StarSite.NO_STAR) {
          x += xUnit * 5 * (rightBlockers % 3);
          y += yUnit * 5 * (rightBlockers / 3);
          rightBlockers++;
        }
      }

      // set seat position
      s.setCentre(new Point(x, y));
    }
  }
}
