package net.firefive.derby.ontrack;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;

import net.firefive.derby.boutmodel.BoutStateModel;
import net.firefive.derby.boutmodel.PlayRole;
import net.firefive.derby.boutmodel.PlayZone;
import net.firefive.derby.boutmodel.Skater;
import net.firefive.derby.boutmodel.StarSite;
import net.firefive.interaction.Point;
import net.firefive.interaction.Region;
import net.firefive.interaction.TimeDriven;

public class SkaterGraphic extends AbstractSkater implements TimeDriven {

  private Skater skater;

  @Override
  public String toString() {
    return "Skater " + skater.getNumber() + " " + skater.getZone() + " " + getRegion();
  }

  public SkaterGraphic(Skater s) {
    super(s);
    skater = s;
  }

  @Override
  public void draw(Graphics g) {

    Color primary = getPrimaryColour();
    Color secondary = getSecondaryColour();

    // for flashing decorations only, comment this out...
    // (see below)
    if (skater.getZone() == PlayZone.BOX && skater.getTimeInBox() >= BoutStateModel.PENALTY_DURATION
        && inAlternateDrawingMode()) {
      primary = getSecondaryColour();
      secondary = getPrimaryColour();
    }

    Region r = getRegion();
    g.setColor(primary);
    // g.drawOval((int) r.x, (int) r.y, (int) r.width, (int) r.height);
    g.fillOval((int) r.x, (int) r.y, (int) r.width, (int) r.height);

    // ...and comment this in!
    // if (isWarning() && inAlternateDrawingMode())
    // return;
    g.setColor(secondary);

    // draw number
    Font defaultFont = getDefaultFont();
    FontMetrics fm = g.getFontMetrics();
    String number = skater.getNumber();
    // String number = Long.toString(skater.getTimeInBox()/1000);
    double textHeight = 2
        * defaultFont.createGlyphVector(fm.getFontRenderContext(), number).getVisualBounds().getHeight();
    double scale = (r.height / 2) / textHeight;
    double fontSize = defaultFont.getSize2D() * scale;
    g.setFont(new Font(defaultFont.getFontName(), Font.BOLD, (int) (fontSize + .5)));
    double width = g.getFontMetrics().stringWidth(number); // use revised
    // FontMetrics!

    int x = (int) (.5 + r.x + r.width / 2 - width / 2);
    int y = (int) (.5 + r.y + 1.5 * textHeight * scale);
    // System.out.println(number + " " + r + " " + x + " " + y);
    g.drawString(number, x, y);

    // draw jammer star
    if (skater.getStar() != StarSite.NO_STAR) {
      Point ctr = new Point(r.x + (7 * r.width / 10), r.y + (3 * r.width / 10));
      double radius = r.width / 6;
      if (skater.getStar() == StarSite.ON_HELMET)
        StarButton.fillStar(g, ctr, radius);
      else if (skater.getStar() == StarSite.IN_HAND)
        StarButton.drawStar(g, ctr, radius);
    }

    // draw pivot stripe
    if (skater.getRole() == PlayRole.PIVOT)
      drawStripe(g, r);
  }

  private void drawStripe(Graphics g, Region r) {
    GeneralPath stripe = new GeneralPath();

    double x = r.x + r.width / 2 // offset around ellipse centre
        + r.width / 2 * Math.cos(2 * Math.PI * (-5d / 24)); // half-width *
                                                            // cos(theta)
    double y = r.y + r.height / 2 // offset around ellipse centre
        + r.height / 2 * Math.sin(2 * Math.PI * (-5d / 24)); // half-height *
                                                             // sin(theta)

    stripe.moveTo(x, y);

    x = r.x + r.width / 2 // offset around ellipse centre
        + r.width / 2 * Math.cos(2 * Math.PI * (-6d / 24)); // half-width *
                                                            // cos(theta)
    y = r.y + r.height / 2 // offset around ellipse centre
        + r.height / 2 * Math.sin(2 * Math.PI * (-6d / 24)); // half-height *
                                                             // sin(theta)

    stripe.lineTo(x, y);

    x = r.x + r.width / 2 // offset around ellipse centre
        + r.width / 2 * Math.cos(2 * Math.PI * (12d / 24)); // half-width *
                                                            // cos(theta)
    y = r.y + r.height / 2 // offset around ellipse centre
        + r.height / 2 * Math.sin(2 * Math.PI * (12d / 24)); // half-height *
                                                             // sin(theta)

    stripe.lineTo(x, y);

    x = r.x + r.width / 2 // offset around ellipse centre
        + r.width / 2 * Math.cos(2 * Math.PI * (11d / 24)); // half-width *
                                                            // cos(theta)
    y = r.y + r.height / 2 // offset around ellipse centre
        + r.height / 2 * Math.sin(2 * Math.PI * (11d / 24)); // half-height *
                                                             // sin(theta)

    stripe.lineTo(x, y);

    stripe.closePath();
    Graphics2D g2 = (Graphics2D) g;
    g2.fill(stripe);
  }
}
