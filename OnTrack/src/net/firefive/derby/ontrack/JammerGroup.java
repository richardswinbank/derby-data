package net.firefive.derby.ontrack;

import java.awt.Graphics;

import net.firefive.derby.boutmodel.JamPlayState;
import net.firefive.derby.boutmodel.PlayRole;
import net.firefive.derby.boutmodel.PlayZone;
import net.firefive.derby.boutmodel.Skater;
import net.firefive.derby.boutmodel.StarSite;
import net.firefive.interaction.Drawable;
import net.firefive.interaction.Point;
import net.firefive.interaction.Region;
import net.firefive.interaction.TimeDriven;

public class JammerGroup extends TrackZone {

  private Arena model;
  private TeamBench bench;

  public JammerGroup(TeamBench bench, Arena surface, Region r) {
    super(surface, r);
    this.bench = bench;
    this.model = surface;

    double height = r.height / 5;
    double yOffset = getVerticalCentreOffset(r) - 3 * height / 2;
    Region buttonRegion = new Region(r.x + r.width / 20, yOffset, 3 * r.width / 10, height);
    this.addChild(new StarButton(bench, PlayRole.JAMMER, StarSite.ON_HELMET, surface, buttonRegion));
    buttonRegion.y += 2 * height;
    this.addChild(new StarButton(bench, PlayRole.JAMMER, StarSite.IN_HAND, surface, buttonRegion));

    buttonRegion.x += 12 * r.width / 20;
    this.addChild(new CallButton(surface, buttonRegion));
    buttonRegion.y -= 2 * height;
    this.addChild(new LeadButton(surface, buttonRegion));
  }

  @Override
  public boolean isContainerFor(Skater s) {
    if (s.getZone() == PlayZone.TRACK && s.getRole() == PlayRole.JAMMER && getSkaterBench(s) == bench)
      return true;
    return false;
  }

  @Override
  public void positionSkaters() {
    Drawable jammer = getJammer();

    if (jammer != null) {
      Region r = getRegion();
      ((AbstractSkater) jammer).setCentre(new Point(r.getCentre().x, getVerticalCentreOffset(r)));
    }

    for (Drawable child : this)
      child.setVisible(jammer != null);
  }

  public AbstractSkater getJammer() {
    for (AbstractSkater jammer : skaters())
      return jammer;
    return null;
  }

  private double getVerticalCentreOffset(Region r) {
    return r.y + r.height * (bench == TeamBench.LEFT ? .6 : .4);
  }

  private class LeadButton extends TeamButton implements TimeDriven {

    public LeadButton(Arena surface, Region region) {
      super("Lead", bench, surface, region);
      
      // start off invisible - will be made visible by 
      // SkaterContainer.positionSkaters() as appropriate
      setVisible(false);
    }

    @Override
    public void buttonPushed() {
      model.setLeadJammer(getBench());
    }

    @Override
    public void draw(Graphics g)
    {
      if(getJammer() != null && getJammer().getSkater().isLeadJammer() && inAlternateDrawingMode())
        return;
      super.draw(g);
    }
  }

  private class CallButton extends TeamButton {

    public CallButton(Arena surface, Region region) {
      super("Call", bench, surface, region);

      // start off invisible - will be made visible by 
      // SkaterContainer.positionSkaters() as appropriate
      setVisible(false);
    }

    @Override
    public void buttonPushed() {
      if (model.getJamState().getPlayState() == JamPlayState.DURING)
        model.callOffJam(getBench());
    }
  }
}
