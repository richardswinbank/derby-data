package net.firefive.interaction.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

import net.firefive.interaction.Drawable;
import net.firefive.interaction.Mobility;
import net.firefive.interaction.Region;
import net.firefive.interaction.Surface;

public abstract class TextBox extends Drawable {

  // reference values for calculations
  private String fontName;
  private int fontStyle;
  private String longestText;
  private Region textRegion100pt;

  // instance members
  private String text;
  private Alignment align;
  private Color textColour;
  private Color boxColour;
  private Color background;
  private boolean rounded;
  private boolean filled;

  public TextBox(String text, Surface surface, Region region, Color background, Mobility mobility) {
    super(surface, region, mobility);
    this.fontName = surface.getFont().getFontName();
    this.fontStyle = surface.getFont().getStyle();
    this.longestText = text;
    rounded = false;
    this.background = background;
    filled = true;

    setText(longestText);
    setTextColour(Color.WHITE);
    setBoxColour(Color.BLACK);
    align = Alignment.CENTRE;
  }

  public void setAlignment(Alignment align) {
    this.align = align;
  }

  public void setRounded(boolean rounded) {
    this.rounded = rounded;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }

  public void setTextColour(Color c) {
    textColour = c;
  }

  public Color getTextColour() {
    return textColour;
  }

  public void setBoxColour(Color c) {
    boxColour = c;
  }

  public void setFilled(boolean b) {
    this.filled = b;
  }

  private double contrastRatio(Color c1, Color c2) {
    double l1 = luminance(c1);
    double l2 = luminance(c2);
    if (l1 > l2)
      return l1 + .05 / l2 + .05;
    return l2 + .05 / l1 + .05;
  }

  private double luminance(Color c) {
    return 0.2126d * luminanceContributor(c.getRed()) + 0.7152 * luminanceContributor(c.getGreen())
        + 0.0722 * luminanceContributor(c.getBlue());
  }

  private double luminanceContributor(double component) {
    component /= 255d;
    if (component < 0.03928)
      return component / 12.92;
    return Math.pow((component + 0.055) / 1.055, 2.4);
  }

  public Color getBoxColour() {
    return boxColour;
  }

  @Override
  public void draw(Graphics g) {

    Region r = getRegion();
    // g.setColor(Color.RED);
    // g.drawRect((int) r.x, (int) r.y, (int) r.width, (int) r.height);

    // fill box
    if (filled) {

      // fill box
      g.setColor(boxColour);
      if (rounded)
        g.fillRoundRect((int) r.x, (int) r.y, (int) r.width, (int) r.height, (int) (r.height / 3),
            (int) (r.height / 3));
      else
        g.fillRect((int) r.x, (int) r.y, (int) r.width, (int) r.height);

      // draw outline
      if (contrastRatio(textColour, background) > contrastRatio(boxColour, background))
        g.setColor(textColour);
      if (rounded)
        g.drawRoundRect((int) r.x, (int) r.y, (int) r.width, (int) r.height, (int) (r.height / 3),
            (int) (r.height / 3));
      else
        g.drawRect((int) r.x, (int) r.y, (int) r.width, (int) r.height);
    }

    // draw text
    if (textRegion100pt == null)
      initialiseFontCalculationBaseline(g);
    Region availableTextRegion = new Region(0, 0, r.width - r.height / 2, r.height / 2);
    boolean scaleByWidth = (textRegion100pt.width / textRegion100pt.height) > (availableTextRegion.width
        / availableTextRegion.height) ? true : false;

    double fontSize = 100d;
    if (scaleByWidth)
      fontSize *= availableTextRegion.width / textRegion100pt.width;
    else
      fontSize *= availableTextRegion.height / textRegion100pt.height;
    Font f = new Font(fontName, fontStyle, (int) (fontSize + .5));
    g.setFont(f);

    FontMetrics fm = g.getFontMetrics();
    int textWidth = fm.stringWidth(text);
    double textHeight = f.createGlyphVector(fm.getFontRenderContext(), text).getVisualBounds().getHeight();
    g.setColor(textColour);

    double x = r.x + (r.width - textWidth) / 2; // CENTRE
    if (align == Alignment.LEFT)
      x = r.x + textHeight / 2;
    if (align == Alignment.RIGHT)
      x = r.x + r.width - textHeight / 2 - textWidth;
    g.drawString(text, (int) x, (int) (r.y + r.height / 2 + textHeight / 2));
  }

  private void initialiseFontCalculationBaseline(Graphics g) {
    Font f = new Font(fontName, fontStyle, 100);
    g.setFont(f);
    FontMetrics fm = g.getFontMetrics();
    Rectangle2D r = f.createGlyphVector(fm.getFontRenderContext(), longestText).getVisualBounds();
    textRegion100pt = new Region(r.getX(), r.getY(), r.getWidth(), r.getHeight());
  }
}
