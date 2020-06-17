package net.firefive.derby.ontrack;

import net.firefive.derby.boutmodel.PlayRole;
import net.firefive.derby.boutmodel.PlayZone;
import net.firefive.derby.boutmodel.Skater;
import net.firefive.derby.boutmodel.StarSite;
import net.firefive.interaction.Drawable;
import net.firefive.interaction.Point;
import net.firefive.interaction.Region;

public class PivotGroup extends TrackZone {

  private TeamBench bench;

  public PivotGroup(TeamBench bench, Arena surface, Region r) {
    super(surface, r);
    this.bench = bench;

    double height = r.height / 5;
    double yOffset = getVerticalCentreOffset(r) - 3 * height / 2;
    Region buttonRegion = new Region(r.x + r.width / 2, yOffset, 3 * r.width / 7, height);
    this.addChild(new StarButton(bench, PlayRole.PIVOT, StarSite.ON_HELMET, surface, buttonRegion));
    buttonRegion.y += 2 * height;
    this.addChild(new StarButton(bench, PlayRole.PIVOT, StarSite.IN_HAND, surface, buttonRegion));
  }

  @Override
  public boolean isContainerFor(Skater s) {
    if (s.getZone() == PlayZone.TRACK && s.getRole() == PlayRole.PIVOT && getSkaterBench(s) == bench)
      return true;
    return false;
  }

  @Override
  public void positionSkaters() {
    Drawable pivot = null;
    for (AbstractSkater skater : skaters())
      pivot = skater;

    if (pivot != null)
      placePivot((AbstractSkater) pivot);

    for (Drawable child : this)
      child.setVisible(pivot != null);
  }

  private void placePivot(AbstractSkater pivot) {
    Region r = getRegion();
    double x = r.x + (double) (2 * r.width) / 7;
    double y = getVerticalCentreOffset(r);
    pivot.setCentre(new Point(x, y));
  }

  private double getVerticalCentreOffset(Region r) {
    return r.y + r.height * (bench == TeamBench.LEFT ? .6 : .4);
  }
}
