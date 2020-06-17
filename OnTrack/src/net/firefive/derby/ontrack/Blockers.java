package net.firefive.derby.ontrack;

import net.firefive.derby.boutmodel.PlayRole;
import net.firefive.derby.boutmodel.PlayZone;
import net.firefive.derby.boutmodel.Skater;
import net.firefive.interaction.Drawable;
import net.firefive.interaction.Point;
import net.firefive.interaction.Region;

public class Blockers extends TrackZone {

  public Blockers(Arena surface, Region r) {
    super(surface, r);
  }

  @Override
  public boolean isContainerFor(Skater skater) {
    if (skater.getZone() == PlayZone.TRACK && skater.getRole() == PlayRole.BLOCKER)
      return true;
    return false;
  }

  @Override
  public void positionSkaters(){
    Region r = getRegion();

    double off = r.width/5;
    
    double x = r.x + r.width - off/2;
    for (Drawable e : this) {
      AbstractSkater s = (AbstractSkater) e;
      if(s.getBench() == TeamBench.LEFT)
        continue;
      s.setCentre(new Point(x, r.y + getHorizontalCentre(s.getBench(), r)));
      x -= off;
    }

    x = r.x + r.width - off/2;
    for (Drawable e : this) {
      AbstractSkater s = (AbstractSkater) e;
      if(s.getBench() == TeamBench.RIGHT)
        continue;
      s.setCentre(new Point(x, r.y + getHorizontalCentre(s.getBench(), r)));
      x -= off;
    }
  }

  private double getHorizontalCentre(TeamBench bench, Region r) {
    if (bench == TeamBench.LEFT)
      return r.height * (60d / 200);
    return r.height * (140d / 200);
  }
}
