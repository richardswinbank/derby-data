package net.firefive.derby.ontrack;

import java.awt.Color;

import net.firefive.derby.boutmodel.events.AbstractBoutEvent;
import net.firefive.derby.boutmodel.events.ScoreChangedEvent;
import net.firefive.interaction.Event;
import net.firefive.interaction.Mobility;
import net.firefive.interaction.Region;
import net.firefive.interaction.components.FixedGroup;
import net.firefive.interaction.components.TextBox;

public class ScoreBoard extends FixedGroup {

  private static final boolean UP = true;
  private static final boolean DOWN = false;

  private Arena arena;

  public ScoreBoard(Arena a, Region r) {
    super(a, r);
    this.arena = a;
    addChild(new ScoreDisplay(a, new Region(r.x + 3 * r.width / 26, r.y, 20 * r.width / 26, r.height)));

    double spacing = r.height / 7;
    Region buttonRegion = new Region(0, 0, 2 * spacing, 2 * spacing);

    buttonRegion.x = r.x;
    buttonRegion.y = r.y + spacing;
    addChild(new ScoreAdjustButton(TeamBench.LEFT, a, buttonRegion, UP));
    buttonRegion.y += buttonRegion.height + spacing;
    addChild(new ScoreAdjustButton(TeamBench.LEFT, a, buttonRegion, DOWN));
    buttonRegion.x += r.width - buttonRegion.width;
    addChild(new ScoreAdjustButton(TeamBench.RIGHT, a, buttonRegion, DOWN));
    buttonRegion.y -= buttonRegion.height + spacing;
    addChild(new ScoreAdjustButton(TeamBench.RIGHT, a, buttonRegion, UP));
  }

  private class ScoreDisplay extends TextBox {

    private int leftPoints;
    private int rightPoints;

    public ScoreDisplay(Arena surface, Region region) {
      super("888-888", surface, region, Color.WHITE, Mobility.FIXED);
      setRounded(true);
      leftPoints = 0;
      rightPoints = 0;
      updateBoard();
    }

    @Override
    public void handleEvent(Event evt) {
      if (evt instanceof AbstractBoutEventWrapper) {
        AbstractBoutEvent abe = ((AbstractBoutEventWrapper) evt).unwrap();
        if (abe instanceof ScoreChangedEvent)
          updateScore((ScoreChangedEvent) abe);
      }
    }

    private void updateScore(ScoreChangedEvent sce) {
      TeamBench bench = arena.getBench(sce.getTeamId());
      if (bench == TeamBench.LEFT)
        leftPoints += sce.getPoints();
      else
        rightPoints += sce.getPoints();
      updateBoard();
    }

    private void updateBoard() {
      setText(leftPoints + "-" + rightPoints);
    }
  }

  private class ScoreAdjustButton extends TeamButton {

    private Arena model;
    private int adjustment;

    public ScoreAdjustButton(TeamBench bench, Arena surface, Region region, boolean direction) {
      super("X", bench, surface, region);
      model = surface;
      setText("");
      if (direction == UP) {
        setText("+");
        adjustment = 1;
      }
      else {
        setText("-");
        adjustment = -1;
      }
    }

    @Override
    public void buttonPushed() {
      model.adjustScore(getBench(), adjustment);
    }
  }
}