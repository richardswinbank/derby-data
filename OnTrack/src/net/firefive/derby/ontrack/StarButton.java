package net.firefive.derby.ontrack;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;

import net.firefive.derby.boutmodel.PlayRole;
import net.firefive.derby.boutmodel.Skater;
import net.firefive.derby.boutmodel.StarSite;
import net.firefive.derby.boutmodel.events.SkaterStateEvent;
import net.firefive.interaction.Event;
import net.firefive.interaction.Point;
import net.firefive.interaction.Region;
import net.firefive.interaction.components.Alignment;

public class StarButton extends TeamButton {

  public static void drawStar(Graphics g, Point ctr, double re) {
    draw(g, ctr, re, false);
  }

  public static void fillStar(Graphics g, Point ctr, double re) {
    draw(g, ctr, re, true);
  }

  private static void draw(Graphics g, Point ctr, double re, boolean fill) {
    GeneralPath star = new GeneralPath();
    star.moveTo(ctr.x, ctr.y - re); // move to top point

    double theta = Math.PI;
    double ri = re / 2.277;
    for (int i = 0; i < 10; i++) {
      theta += (2 * Math.PI) / 10;
      double r = i % 2 == 0 ? ri : re;
      star.lineTo(ctr.x + (r * Math.sin(theta)), ctr.y + (r * Math.cos(theta)));
    }

    star.closePath();
    Graphics2D g2 = (Graphics2D) g;

    g2.draw(star);
    if (fill)
      g2.fill(star);
  }

  private Arena model;
  private PlayRole role;
  private StarSite site;
  private boolean hasStar;

  public StarButton(TeamBench bench, PlayRole role, StarSite site, Arena surface, Region region) {
    super(site == StarSite.ON_HELMET ? "Helmet" : "In hand", bench, surface, region);
    model = surface;

    if (role == PlayRole.JAMMER)
      setAlignment(Alignment.RIGHT);
    else
      setAlignment(Alignment.LEFT);

    this.role = role;
    this.site = site;
    hasStar = false;

    // start off invisible - will be made visible by 
    // SkaterContainer.positionSkaters() as appropriate
    setVisible(false);
  }

  @Override
  public void buttonPushed() {
    model.passStar(getBench(), role, site);
  }

  @Override
  public void handleEvent(Event e) {
    super.handleEvent(e);

    if (e instanceof AbstractBoutEventWrapper) {
      AbstractBoutEventWrapper wrapper = (AbstractBoutEventWrapper) e;

      if (wrapper.unwrap() instanceof SkaterStateEvent) {
        Skater s = ((SkaterStateEvent) wrapper.unwrap()).getSkater();
        if (model.getBench(s.getTeamId()) == this.getBench() && s.getRole() == this.role) {
          //System.out.println(this + " " + s);
          if (s.getStar() == this.site)
            hasStar = true;
          else
            hasStar = false;
        }
      }
    }
  }

  @Override
  public void draw(Graphics g) {
    super.draw(g);
    if (hasStar) {// and draw star if required
      g.setColor(getTextColour());
      Region r = getRegion();
      double x = r.x;
      x += this.role == PlayRole.JAMMER ? r.height / 3 : r.width - r.height / 2;
      Point ctr = new Point(x, r.y + r.height / 2);
      fillStar(g, ctr, r.height / 3);
    }
  }
}
