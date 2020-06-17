package net.firefive.derby.ontrack;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Comparator;

import net.firefive.derby.boutmodel.Skater;
import net.firefive.derby.boutmodel.events.SkaterAddedEvent;
import net.firefive.derby.boutmodel.events.SkaterRemovedEvent;
import net.firefive.derby.boutmodel.events.SkaterStateEvent;
import net.firefive.interaction.Drawable;
import net.firefive.interaction.Event;
import net.firefive.interaction.Mobility;
import net.firefive.interaction.Region;

public abstract class SkaterContainer extends Drawable {

  private static final Comparator<AbstractSkater> comparator = new Comparator<AbstractSkater>() {
    @Override
    public int compare(AbstractSkater s1, AbstractSkater s2) {
      return s1.getSkater().getNumber().compareTo(s2.getSkater().getNumber());
    }
  };

  private Arena arena;

  public SkaterContainer(Arena arena, Region region) {
    super(arena, region, Mobility.FIXED);
    this.arena = arena;
  }

  // return a list of skaters in lexicographic number order
  public ArrayList<AbstractSkater> skaters() {
    ArrayList<AbstractSkater> skaters = new ArrayList<AbstractSkater>();
    for (Drawable e : this)
      if (e instanceof AbstractSkater)
        skaters.add((AbstractSkater) e);
    skaters.sort(comparator);
    return skaters;
  }

  public abstract boolean isContainerFor(Skater skater);

  public abstract void positionSkaters();

  @Override
  public void handleEvent(Event evt) {
    if (evt instanceof AbstractBoutEventWrapper) {
      AbstractBoutEventWrapper wrapper = (AbstractBoutEventWrapper) evt;

      if (wrapper.unwrap() instanceof SkaterStateEvent) {
      //  System.out.println(this);
        Skater s = ((SkaterStateEvent) wrapper.unwrap()).getSkater();
        if (this.isContainerFor(s)) {

          if (wrapper.unwrap() instanceof SkaterRemovedEvent)
            arena.removeSkater(s);
          else if (wrapper.unwrap() instanceof SkaterAddedEvent)
            addChild(AbstractSkater.newInstance(s));
          else {
            Drawable as = arena.getSkater(s);
            if (as == null)
              throw new RuntimeException();
            addChild(as);
          }

        }
//        System.out.println(this);
//        for(AbstractSkater as:skaters())
//          System.out.println(" - " + as);
        positionSkaters();
      }
    }
  }

  @Override
  public void draw(Graphics g) {
    Region r = getRegion();
    g.setColor(Color.LIGHT_GRAY);
    g.fillRect((int) r.x - 1, (int) r.y - 1, (int) r.width + 2, (int) r.height + 2);
  }

  protected TeamBench getSkaterBench(Skater s) {
    return arena.getBench(s.getTeamId());
  }
}