package net.firefive.derby.ontrack;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFrame;

import net.firefive.derby.boutmodel.BoutObserver;
import net.firefive.derby.boutmodel.BoutStateModel;
import net.firefive.derby.boutmodel.JamState;
import net.firefive.derby.boutmodel.PeriodState;
import net.firefive.derby.boutmodel.PlayRole;
import net.firefive.derby.boutmodel.PlayZone;
import net.firefive.derby.boutmodel.Skater;
import net.firefive.derby.boutmodel.StarSite;
import net.firefive.derby.boutmodel.Team;
import net.firefive.derby.boutmodel.TeamId;
import net.firefive.derby.boutmodel.events.AbstractBoutEvent;
import net.firefive.derby.ontrack.TimeoutPanel.ReviewButton;
import net.firefive.derby.ontrack.TimeoutPanel.TimeoutButton;
import net.firefive.interaction.Drawable;
import net.firefive.interaction.Region;
import net.firefive.interaction.Surface;

@SuppressWarnings("serial")
public class Arena extends Surface implements BoutObserver {

  private BoutStateModel boutModel;

  public Arena(BoutStateModel m, JFrame container, Dimension arenaSize, Dimension skaterSize, Font font) {
    super(container, arenaSize, font);
    homeBench = TeamBench.LEFT;
    visitorsBench = TeamBench.RIGHT;
    AbstractSkater.initialise(this, new Region(0, 0, skaterSize.width, skaterSize.height));

    this.boutModel = m;
    boutModel.addObserver(this);
  }

  /*
   * handle relationship between teams (model) and benches (UI)
   */
  private TeamBench homeBench;
  private TeamBench visitorsBench;

  // translate IDs into benches (for Drawables handling events containing
  // TeamIds)
  public TeamBench getBench(TeamId teamId) {
    if (teamId == TeamId.HOME)
      return homeBench;
    if (teamId == TeamId.VISITORS)
      return visitorsBench;
    return null;
  }

  // translate benches into IDs (allowing this class to translate method calls
  // into terms understood by the BoutStateModel)
  private TeamId getTeamId(TeamBench bench) {
    return bench == homeBench ? TeamId.HOME : TeamId.VISITORS;
  }

  // translate UI interactions into actions on the model
  @Override
  public void handleInteraction(Drawable actor, Drawable recipient) {
    // // System.out.println(getClass().getSimpleName() + ".getInteractionEvent
    // ("
    // // + actor + ", " + recipient + ")");
    if (actor instanceof AbstractSkater) {

      Skater skater = ((AbstractSkater) actor).getSkater();
      // System.out.println(recipient);

      if (recipient instanceof PenaltyBox) {
        setSkaterZone(skater.getSkaterId(), PlayZone.BOX);
      }
      else if (recipient instanceof Bench) {
        // if (((Bench) recipient).getTeamBench() == ((AbstractSkater)
        // actor).getBench())
        setSkaterZone(skater.getSkaterId(), PlayZone.BENCH);
      }
      else if (recipient instanceof TrackZone) {
        int skaterId = skater.getSkaterId();
        if (skater.getZone() == PlayZone.BOX)
          setSkaterZone(skaterId, PlayZone.TRACK);
        else if (recipient instanceof JammerGroup)
          setJammer(skaterId);
        else if (recipient instanceof PivotGroup)
          setPivot(skaterId);
        else if (recipient instanceof Blockers)
          setBlocker(skaterId);
      }
    }

    if (actor instanceof TimeoutButton && recipient instanceof AbstractClock) {
      if (actor instanceof ReviewButton)
        officialReview(((TimeoutButton) actor).getBench());
      else
        teamTimeout(((TimeoutButton) actor).getBench());
    }
  }

  /*
   * BoutStateModel method wrappers (often to wrap translation between TeamBench
   * and TeamId)
   */
  public Team getTeam(TeamId teamId) {
    return boutModel.getTeam(teamId);
  }

  public Team getTeam(TeamBench bench) {
    return getTeam(getTeamId(bench));
  }

  public JamState getJamState() {
    return boutModel.getJamState();
  }

  public PeriodState getPeriodState() {
    return boutModel.getPeriodState();
  }

  public void newPeriod() {
    boutModel.newPeriod();
  }

  public void startPlay() {
    boutModel.startPlay();
  }

  public void officialTimeout() {
    boutModel.officialTimeout();
  }

  public void teamTimeout(TeamBench team) {
    boutModel.teamTimeout(getTeamId(team));
  }

  public void officialReview(TeamBench team) {
    boutModel.officialReview(getTeamId(team));
  }

  public void endPeriod() {
    boutModel.endPeriod();
  }

  public void startJam() {
    boutModel.startJam();
  }

  public void callOffJam(TeamBench bench) {
    boutModel.callOffJam(getTeamId(bench));
  }

  public void stopJam() {
    boutModel.stopJam();
  }

  public void resetJam() {
    boutModel.resetJam();
  }

  public void completePass(TeamBench bench, int points) {
    boutModel.completePass(getTeamId(bench), points);
  }

  public void setJammer(int skaterId) {
    boutModel.setSkaterRole(skaterId, PlayRole.JAMMER);
  }

  public void setPivot(int skaterId) {
    boutModel.setSkaterRole(skaterId, PlayRole.PIVOT);
  }

  public void setBlocker(int skaterId) {
    boutModel.setSkaterRole(skaterId, PlayRole.BLOCKER);
  }

  public void setSkaterRole(int skaterId, PlayRole role) {
    boutModel.setSkaterRole(skaterId, role);
  }

  void setSkaterZone(int skaterId, PlayZone zone) {
    boutModel.setSkaterZone(skaterId, zone);
  }

  public void adjustScore(TeamBench bench, int adjustment) {
    boutModel.adjustScore(getTeamId(bench), adjustment);
  }

  public Skater getJammer(TeamBench bench) {
    return boutModel.getJammer(getTeamId(bench));
  }

  public void setLeadJammer(TeamBench bench) {
    boutModel.setLeadJammer(getTeamId(bench));
  }

  public Skater getPivot(TeamBench bench) {
    return boutModel.getPivot(getTeamId(bench));
  }

  public void passStar(TeamBench bench, PlayRole role, StarSite site) {
    boutModel.passStar(getTeamId(bench), role, site);
  }

  public void packCompletedLap() {
    boutModel.packCompletedLap();
  }

  /*
   * event management
   */
  // implementation of BoutObserver - handle events raised by the BoutStateModel
  @Override
  public void handleBoutEvent(AbstractBoutEvent evt) {
    // Wrap up bout model event and publish it to the UI model
    // (by calling Surface's publishEvent method)
    super.publishEvent(new AbstractBoutEventWrapper(evt));
  }

  public Drawable getSkater(Skater s) {
    for (Drawable d : this)
      if (d instanceof AbstractSkater)
        if (((AbstractSkater) d).getSkater().getSkaterId() == s.getSkaterId())
          return d;
    return null;
  }

  public void removeSkater(Skater s) {
    removeDrawable(getSkater(s));
  }

  // Implementation of Surface
  // (handle any events published to the UI model and not explicitly handled in
  // other Drawables - typically events which don't "belong" to anything else
  // or which need some or all of the collection of drawables to be considered)
  // @Override
  // public void handleEvent(Event ae) {
  //
  // if (ae instanceof AbstractBoutEventWrapper) {
  // AbstractBoutEventWrapper evt = (AbstractBoutEventWrapper) ae;
  //
  // // add new skater
  // if (evt.unwrap() instanceof SkaterAddedEvent) {
  // Skater s = ((SkaterStateEvent) evt.unwrap()).getSkater();
  // if (!onList(s)) {
  // Region r = new Region(-skaterWidth, -skaterWidth, skaterWidth,
  // skaterWidth);
  // addDrawable(AbstractSkater.newInstance(s, this, r));
  // }
  // this.repaint();
  // }
  //
  // // remove a skater
  // if (evt.unwrap() instanceof SkaterRemovedEvent) {
  // Skater s = ((SkaterStateEvent) evt.unwrap()).getSkater();
  // for (Drawable e : this)
  // if (e instanceof AbstractSkater)
  // if (((AbstractSkater) e).getSkater().getSkaterId() == s.getSkaterId())
  // removeDrawable(e);
  // this.repaint();
  // }
  // }
  // }

  // find out if we have this skater
  // private boolean onList(Skater s) {
  // for (Drawable e : this)
  // if (e instanceof AbstractSkater)
  // if (((AbstractSkater) e).getSkater().getSkaterId() == s.getSkaterId())
  // return true;
  // return false;
  // }
}
