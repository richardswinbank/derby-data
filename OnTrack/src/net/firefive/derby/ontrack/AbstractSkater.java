package net.firefive.derby.ontrack;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;

import net.firefive.derby.boutmodel.JamPlayState;
import net.firefive.derby.boutmodel.PlayRole;
import net.firefive.derby.boutmodel.PlayZone;
import net.firefive.derby.boutmodel.Skater;
import net.firefive.derby.boutmodel.Team;
import net.firefive.derby.boutmodel.events.SkaterStateEvent;
import net.firefive.derby.boutmodel.events.TeamUpdatedEvent;
import net.firefive.interaction.Drawable;
import net.firefive.interaction.Event;
import net.firefive.interaction.Mobility;
import net.firefive.interaction.Point;
import net.firefive.interaction.Region;

public abstract class AbstractSkater extends Drawable {

  private static final String JAMMER = "Jammer";
  private static final String PIVOT = "Pivot";

  private static Region skaterRegion;
  private static Arena arena;
  private static Font defaultFont;

  public static AbstractSkater newInstance(Skater s) {
    if (AbstractSkater.arena == null)
      throw new RuntimeException("must call initialise() before any calls to newInstance()");
    return new SkaterGraphic(s);
  }

  public static void initialise(Arena arena, Region skaterRegion) {
    if (AbstractSkater.arena != null)
      throw new RuntimeException("multiple calls to initialise() not permitted");
    AbstractSkater.arena = arena;
    AbstractSkater.skaterRegion = skaterRegion;
    AbstractSkater.defaultFont = arena.getFont();
  }

  protected static Font getDefaultFont() {
    return AbstractSkater.defaultFont;
  }

  private Skater skater;
  private Color primaryColour;
  private Color secondaryColour;

  public AbstractSkater(Skater s) {
    super(arena, skaterRegion, Mobility.BOUND);
    this.skater = s;
    setColours();

    ArrayList<String> popupOptions = new ArrayList<String>();
    popupOptions.add(JAMMER);
    popupOptions.add(PIVOT);
    setPopupOptions(popupOptions);
  }

  public Skater getSkater() {
    return skater;
  }

  public Color getPrimaryColour() {
    return primaryColour;
  }

  public Color getSecondaryColour() {
    return secondaryColour;
  }

  private void setColours() {
    Team t = arena.getTeam(skater.getTeamId());
    primaryColour = t.getPrimaryColour();
    secondaryColour = t.getSecondaryColour();
  }

  public TeamBench getBench() {
    return arena.getBench(skater.getTeamId());
  }

  // override Drawable.contains for more precise definition
  @Override
  public boolean contains(Point p) {
    Region r = getRegion();
    return p.distance(r.getCentre()) <= r.radius();
  }

  public void setCentre(Point ctr) {
    Region r = getRegion();
    Point topLeft = new Point(ctr.x - r.width / 2, ctr.y - r.height / 2);
    setPosition(topLeft);
  }

  // handle mouse clicks
  @Override
  public void handleMouseClick() {
    //System.out.println("clicked");
    if (skater.getZone() == PlayZone.BENCH)
      arena.setSkaterRole(skater.getSkaterId(), PlayRole.BLOCKER);
    else if(arena.getJamState().getPlayState() != JamPlayState.DURING)
      arena.setSkaterZone(skater.getSkaterId(), PlayZone.BENCH);
    else if (skater.getZone() == PlayZone.BOX)
      arena.setSkaterZone(skater.getSkaterId(), PlayZone.TRACK);
    else
      arena.setSkaterZone(skater.getSkaterId(), PlayZone.BOX);
  }

  // handle events
  @Override
  public void handleEvent(Event e) {
    if (e instanceof AbstractBoutEventWrapper) {
      AbstractBoutEventWrapper evt = (AbstractBoutEventWrapper) e;

      if (evt.unwrap() instanceof TeamUpdatedEvent)
        if (skater.getTeamId() == ((TeamUpdatedEvent) evt.unwrap()).getTeamId())
          setColours();

      if (evt.unwrap() instanceof SkaterStateEvent) {
        // sync skater details with model
        Skater s = ((SkaterStateEvent) evt.unwrap()).getSkater();
        if (s.getSkaterId() == skater.getSkaterId())
          skater = s;
      }
    }
  }

  // popup menu actions
  @Override
  public void popupOptionSelected(String selected) {
    if (selected == JAMMER)
      arena.setJammer(skater.getSkaterId());
    if (selected == PIVOT)
      arena.setPivot(skater.getSkaterId());
  }
  
  @Override
  public String getTooltip()
  {
    return skater.getName();
  }
}
