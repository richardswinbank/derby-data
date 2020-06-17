package net.firefive.derby.boutmodel.impl;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import net.firefive.derby.boutmodel.BoutConfigurationException;
import net.firefive.derby.boutmodel.BoutObserver;
import net.firefive.derby.boutmodel.BoutStateModel;
import net.firefive.derby.boutmodel.JamPlayState;
import net.firefive.derby.boutmodel.JamState;
import net.firefive.derby.boutmodel.PeriodPlayState;
import net.firefive.derby.boutmodel.PeriodState;
import net.firefive.derby.boutmodel.PlayRole;
import net.firefive.derby.boutmodel.PlayZone;
import net.firefive.derby.boutmodel.Skater;
import net.firefive.derby.boutmodel.StarSite;
import net.firefive.derby.boutmodel.TeamId;
import net.firefive.derby.boutmodel.events.AbstractBoutEvent;
import net.firefive.derby.boutmodel.events.JamStateEvent;
import net.firefive.derby.boutmodel.events.JammerPassEvent;
import net.firefive.derby.boutmodel.events.NewBoutEvent;
import net.firefive.derby.boutmodel.events.PackLapEvent;
import net.firefive.derby.boutmodel.events.PeriodStateEvent;
import net.firefive.derby.boutmodel.events.ScoreChangedEvent;
import net.firefive.derby.boutmodel.events.SkaterAddedEvent;
import net.firefive.derby.boutmodel.events.SkaterRemovedEvent;
import net.firefive.derby.boutmodel.events.SkaterStateEvent;
import net.firefive.derby.boutmodel.events.TeamUpdatedEvent;

class BoutStateModelImpl implements BoutStateModel {

  private ArrayList<BoutObserver> observers;
  private String logFileDir;

  @Override
  public void addObserver(BoutObserver obs) {
    for (BoutObserver o : observers)
      if (o == obs) // already added
        return;
    observers.add(obs);
    // Don't notify new observer of current bout state -
    // let them request sync if/when they need one.
  }

  @Override
  public void removeObserver(BoutObserver obs) {
    for (BoutObserver o : observers)
      if (o == obs) {
        observers.remove(obs);
        return;
      }
  }

  public void publishEvent(AbstractBoutEvent evt) {
    for (BoutObserver o : observers)
      o.handleBoutEvent(evt);
  }

  private TeamImpl home;
  private TeamImpl visitors;
  private ArrayList<Skater> skaters;

  private JamPlayState jamPlayState;
  private long lastJamClockStart;
  private long jamTimeRemaining;

  private PeriodPlayState periodPlayState;
  private long lastPeriodClockStart;
  private long periodTimeRemaining;

  public BoutStateModelImpl(String logFileDir) {
    observers = new ArrayList<BoutObserver>();
    skaters = new ArrayList<Skater>();

    new File(logFileDir).mkdirs();
    this.logFileDir = logFileDir;

    initialiseProperties();
  }

  private void initialiseProperties() {
    SkaterImpl.newBout();
    lastJamClockStart = 0;
    lastPeriodClockStart = 0;

    jamPlayState = JamPlayState.AFTER;
    periodPlayState = PeriodPlayState.AFTER;
    newPeriod();

    updateTeam(TeamId.HOME, "(Name not set)", Color.BLACK, Color.WHITE);
    adjustScore(TeamId.HOME, -getTeam(TeamId.HOME).getPoints());
    updateTeam(TeamId.VISITORS, "(Name not set)", Color.WHITE, Color.BLACK);
    adjustScore(TeamId.VISITORS, -getTeam(TeamId.VISITORS).getPoints());

    // publish event for any observers not governed by jam/period/team updates
    publishEvent(new NewBoutEvent());
  }

  @Override
  public void newBout() {

    // remove log file writer observer(s)
    for (Iterator<BoutObserver> it = observers.iterator(); it.hasNext();) {
      BoutObserver o = it.next();
      if (o instanceof BoutTraceWriter) {
        BoutTraceWriter writer = (BoutTraceWriter) o;
        it.remove();

        try {
          writer.close();
        } catch (IOException e) {
          throw new BoutConfigurationException(e);
        }
      }
    }

    // remove all skaters
    while (skaters.size() > 0)
      removeSkater(skaters.get(0).getSkaterId());

    // reset bout properties
    initialiseProperties();

    // initialise new log file
    String logFileName = getLogFileName();
    File oldLogFile = new File(logFileName);

    try {
      if (oldLogFile.exists() && oldLogFile.length() > 0)
        oldLogFile.renameTo(new File(logFileDir + File.separator + "BoutTrace" + System.currentTimeMillis() + ".txt"));
      BoutTraceWriter writer = new BoutTraceWriter(logFileName);
      addObserver(writer);
    } catch (Exception e) {
      throw new BoutConfigurationException(e);
    }
  }

  public String getLogFileName() {
    return logFileDir + File.separator + "BoutTrace.txt";
  }

  @Override
  public void recoverState() {
    try (BoutTraceReader stream = new BoutTraceReader(getLogFileName())) {
      while (stream.hasMoreEvents())
        playEvent(stream.getNextEvent());
    } catch (FileNotFoundException e) {
      // no existing log file
    } catch (Exception e) {
      throw new BoutConfigurationException(e);
    }

    // do this in a separate try/catch to make sure it happens even
    // after an exception is thrown when reading the event trace.
    try {
      BoutTraceWriter writer = new BoutTraceWriter(getLogFileName());
      addObserver(writer);
    } catch (Exception e) {
      throw new BoutConfigurationException(e);
    }

    officialTimeout(); // pause period clock
    resetJam();  // set jam state to BEFORE
  }

  private void playEvent(AbstractBoutEvent evt) throws BoutConfigurationException {

    if (evt instanceof TeamUpdatedEvent) {
      TeamUpdatedEvent e = (TeamUpdatedEvent) evt;
      updateTeam(e.getTeamId(), e.getTeam().getName(), e.getTeam().getPrimaryColour(),
          e.getTeam().getSecondaryColour());
      return;
    }

    if (evt instanceof PeriodStateEvent) {
      PeriodStateEvent pse = (PeriodStateEvent) evt;
      PeriodState ps = pse.getState();
      PeriodPlayState state = ps.getPlayState();
      if (state == PeriodPlayState.RUNNING) {
        // allow RUNNING event to be detected
        setPeriodPlayState(state, pse.getTeam(), pse.isReview());
        state = PeriodPlayState.PAUSED;
      }

      // make sure that the state we impose causes a change (and thus an event)
      periodTimeRemaining = ps.getRemainingTime();
      lastPeriodClockStart = System.currentTimeMillis();
      if (periodPlayState == PeriodPlayState.PAUSED)
        periodPlayState = PeriodPlayState.RUNNING;
      setPeriodPlayState(state, pse.getTeam(), pse.isReview());

      return;
    }

    if (evt instanceof JamStateEvent) {
      JamState js = ((JamStateEvent) evt).getState();
      JamPlayState state = js.getPlayState();
      if (state == JamPlayState.DURING) {
        // allow DURING event to be detected
        setJamPlayState(state, ((JamStateEvent) evt).getTeam());
        state = JamPlayState.AFTER;
      }

      // make sure that the state we impose causes a change (and thus an event)
      jamTimeRemaining = js.getRemainingTime();
      lastJamClockStart = System.currentTimeMillis();
      if (jamPlayState == JamPlayState.AFTER)
        jamPlayState = JamPlayState.DURING;
      setJamPlayState(state, ((JamStateEvent) evt).getTeam());

      return;
    }

    /*
     * SkaterStateEvent hierarchy
     */
    if (evt instanceof SkaterAddedEvent) {
      addSkater(((SkaterAddedEvent) evt).getSkater());
      return;
    }
    else if (evt instanceof SkaterRemovedEvent) {
      removeSkater(((SkaterRemovedEvent) evt).getSkater().getSkaterId());
      return;
    }
    else if (evt instanceof SkaterStateEvent) {
      // this skater is an object constructed from log data - NOT the skater
      // actually
      // in the model's skaters list. So we **must** call setSkaterRole/Zone
      // with an ID.
      SkaterImpl skaterCopy = (SkaterImpl) (((SkaterStateEvent) evt).getSkater());
      // System.out.println(skaterCopy);
      setSkaterRole(skaterCopy.getSkaterId(), skaterCopy.getRole());
      setSkaterZone(skaterCopy.getSkaterId(), skaterCopy.getZone());
      return;
    }

    /*
     * ScoreChangedEvent hierarchy
     */
    if (evt instanceof JammerPassEvent) {
      JammerPassEvent e = (JammerPassEvent) evt;
      completePass(e.getTeamId(), e.getPoints());
      return;
    }
    else if (evt instanceof ScoreChangedEvent) {
      ScoreChangedEvent e = (ScoreChangedEvent) evt;
      adjustScore(e.getTeamId(), e.getPoints());
      return;
    }
    else if (evt instanceof PackLapEvent) {
      packCompletedLap();
      return;
    }
    throw new BoutConfigurationException("Unsupported event type: " + evt.getClass());
  }

  @Override
  public TeamImpl getTeam(TeamId teamId) {
    if (teamId == TeamId.HOME)
      return home;
    if (teamId == TeamId.VISITORS)
      return visitors;
    return null;
  }

  @Override
  public void updateTeam(TeamId teamId, String name, Color primary, Color secondary) throws BoutConfigurationException {
    TeamImpl t = getTeam(teamId);

    if (t == null) {
      t = new TeamImpl(name, primary, secondary);
      if (teamId == TeamId.HOME)
        home = t;
      else if (teamId == TeamId.VISITORS)
        visitors = t;
    }

    t.setName(name);
    t.setPrimaryColor(primary);
    t.setSecondaryColor(secondary);

    publishEvent(new TeamUpdatedEvent(teamId, t));
  }

  @Override
  public SkaterImpl getSkater(int skaterId) {
    for (Skater s : skaters)
      if (s.getSkaterId() == skaterId)
        return (SkaterImpl) s;
    return null;
  }

  @Override
  public void addSkater(String number, String name, TeamId teamId) {
    addSkater(new SkaterImpl(number, name, teamId));
  }

  private void addSkater(Skater s) {
    for (Skater s1 : skaters) {
      if (s1.getTeamId() == s.getTeamId() && s1.getNumber().equals(s.getNumber()))
        throw new BoutConfigurationException("Duplicate skater: " + s.getNumber() + ", " + s.getTeamId());
      if (s1.getSkaterId() == s.getSkaterId())
        throw new BoutConfigurationException("Duplicate skater ID: " + s.getSkaterId());
    }

    skaters.add(s);
    publishEvent(new SkaterAddedEvent(s));
  }

  @Override
  public void removeSkater(int skaterId) {
    Skater skater = null;
    for (Skater s : skaters)
      if (s.getSkaterId() == skaterId)
        skater = s;
    skaters.remove(skater);
    publishEvent(new SkaterRemovedEvent(skater));
  }

  @Override
  public PeriodState getPeriodState() {
    if (periodPlayState != PeriodPlayState.RUNNING)
      return new PeriodState(periodPlayState, periodTimeRemaining);
    return new PeriodState(periodPlayState, periodTimeRemaining - (System.currentTimeMillis() - lastPeriodClockStart));
  }

  @Override
  public void newPeriod() {
    setPeriodPlayState(PeriodPlayState.BEFORE, null, false);
  }

  @Override
  public void startPlay() {
    setPeriodPlayState(PeriodPlayState.RUNNING, null, false);
  }

  @Override
  public void officialTimeout() {
    setPeriodPlayState(PeriodPlayState.PAUSED, null, false);
  }

  public void teamTimeout(TeamId team) {
    setPeriodPlayState(PeriodPlayState.PAUSED, team, false);
  }

  public void officialReview(TeamId team) {
    setPeriodPlayState(PeriodPlayState.PAUSED, team, true);
  }

  @Override
  public void endPeriod() {
    setPeriodPlayState(PeriodPlayState.AFTER, null, false);
  }

  private void setPeriodPlayState(PeriodPlayState newState, TeamId team, boolean isReview) {
    PeriodPlayState oldState = periodPlayState;
    if (oldState == newState)
      return;

    // actions dependent on current (old) state
    if (oldState == PeriodPlayState.RUNNING) // RUNNING -> not running
      periodTimeRemaining -= System.currentTimeMillis() - lastPeriodClockStart;

    // actions dependent on new state
    if (newState == PeriodPlayState.BEFORE) {
      periodTimeRemaining = BoutStateModel.PERIOD_DURATION;
      resetJam();
    }
    else if (newState == PeriodPlayState.RUNNING) // started
      lastPeriodClockStart = System.currentTimeMillis();
    else if (newState == PeriodPlayState.PAUSED)
      if (jamPlayState == JamPlayState.DURING)
        stopJam();

    periodPlayState = newState;
    publishEvent(new PeriodStateEvent(getPeriodState(), team, isReview));
  }

  @Override
  public JamState getJamState() {
    long remaining = jamTimeRemaining; // AFTER
    if (jamPlayState == JamPlayState.BEFORE)
      remaining = JAM_DURATION;
    if (jamPlayState == JamPlayState.DURING)
      remaining -= (System.currentTimeMillis() - lastJamClockStart);

    // System.out.println("jtr2:" + jamTimeRemaining);
    return new JamState(jamPlayState, remaining);
  }

  @Override
  public void startJam() {
    setJamPlayState(JamPlayState.DURING, null);
  }

  @Override
  public void callOffJam(TeamId team) {
    setJamPlayState(JamPlayState.AFTER, team);
  }

  @Override
  public void stopJam() {
    setJamPlayState(JamPlayState.AFTER, null);
  }

  @Override
  public void resetJam() {
    setJamPlayState(JamPlayState.BEFORE, null);
  }

  private void setJamPlayState(JamPlayState newState, TeamId team) {
    // printCallTrace();
    // System.out.println(this.getClass().getName() + ".setJamPlayState(" +
    // newState + ")");
    JamPlayState oldState = jamPlayState;
    if (oldState == newState)
      return; // no change

    jamPlayState = newState;

    // (for convenience, just to obviate littering the code with downcasts)
    ArrayList<SkaterImpl> skaters = new ArrayList<SkaterImpl>();
    for (Skater s : this.skaters)
      skaters.add((SkaterImpl) s);

    if (jamPlayState == JamPlayState.DURING) // jam started
    {
      lastJamClockStart = System.currentTimeMillis();
      for (SkaterImpl s : skaters)
        // restart the timers on anyone in the box
        if (s.getZone() == PlayZone.BOX)
          s.startBoxTimer();
    }
    else if (jamPlayState == JamPlayState.AFTER) // jam stopped
    {
      jamTimeRemaining -= System.currentTimeMillis() - lastJamClockStart;
      // System.out.println("jtr1:" + jamTimeRemaining);
      for (SkaterImpl s : skaters) {
        // pause timers on anyone in the box
        if (s.getZone() == PlayZone.BOX)
          s.stopBoxTimer();
      }
    }
    else if (jamPlayState == JamPlayState.BEFORE) // jam reset {
    {
      jamTimeRemaining = JAM_DURATION;

      for (SkaterImpl s : skaters) {
        // return skaters on track to their benches
        if (s.getZone() == PlayZone.TRACK)
          setSkaterZone(s.getSkaterId(), PlayZone.BENCH);
        // star-passed pivot in the box?
        // make her into a true jammer for the next jam
        if (s.getZone() == PlayZone.BOX && s.getRole() == PlayRole.PIVOT && s.getStar() != StarSite.NO_STAR)
          setSkaterRole(s.getSkaterId(), PlayRole.JAMMER);
      }
    }

    publishEvent(new JamStateEvent(getJamState(), team));
  }

  // diagnostic traces
  @SuppressWarnings("unused")
  private void printCallTrace() {
    try {
      throw new RuntimeException();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void setSkaterZone(int skaterId, PlayZone zone) {
    // System.out.println(getClass().getName() + ".setSkaterZone()");
    SkaterImpl s = getSkater(skaterId);
    if (s.setZone(zone, jamPlayState))
      publishEvent(new SkaterStateEvent(s));
  }

  @Override
  public void setSkaterRole(int skaterId, PlayRole role) {
    SkaterImpl s = getSkater(skaterId);
    // assume actual role changes only take place outside a jam - so
    // change to/from jammer requires gain/loss of star

    // System.out.println(s);
    if (s.setRole(role)) {

      publishEvent(new SkaterStateEvent(s));

      // flip singleton roles
      if (role == PlayRole.PIVOT || role == PlayRole.JAMMER)
        for (Skater s2 : skaters)
          if (s2.getTeamId() == s.getTeamId() && s2.getRole() == role && !s2.equals(s))
            if (((SkaterImpl) s2).setRole(PlayRole.BLOCKER))
              publishEvent(new SkaterStateEvent(s2));
    }
  }

  @Override
  public void setLeadJammer(TeamId teamId) {
    if (jamPlayState != JamPlayState.DURING)
      return;
    SkaterImpl jammer = getSkaterByRole(teamId, PlayRole.JAMMER);
    if (jammer == null || !jammer.setLeadJammer(true))
      return;
    completePass(teamId, 0);

    publishEvent(new SkaterStateEvent(jammer));
    for (Skater s : skaters)
      if (s.isLeadJammer() && !s.equals(jammer))
        if (((SkaterImpl) s).setLeadJammer(false))
          publishEvent(new SkaterStateEvent(s));
  }

  @Override
  public void passStar(TeamId teamId, PlayRole role, StarSite site) {
    // System.out.println(getClass().getName() + "() teamId=" + teamId + "
    // role=" + role + " site=" + site );
    SkaterImpl si = getSkaterByRole(teamId, role);
    if (si == null || !si.setStar(site))
      return;

    publishEvent(new SkaterStateEvent(si));
    for (Skater s : skaters)
      if (s.getTeamId() == si.getTeamId() && !s.equals(si))
        if (((SkaterImpl) s).setStar(StarSite.NO_STAR))
          publishEvent(new SkaterStateEvent(s));
  }

  @Override
  public SkaterImpl getJammer(TeamId teamId) {
    return getSkaterByRole(teamId, PlayRole.JAMMER);
  }

  @Override
  public SkaterImpl getPivot(TeamId teamId) {
    return getSkaterByRole(teamId, PlayRole.PIVOT);
  }

  private SkaterImpl getSkaterByRole(TeamId teamId, PlayRole role) {
    for (Skater s : skaters)
      if (s.getTeamId() == teamId && s.getRole() == role)
        return (SkaterImpl) s;
    return null;
  }

  @Override
  public void completePass(TeamId teamId, int points) {
    getTeam(teamId).addPoints(points);
    publishEvent(new JammerPassEvent(teamId, points));
  }

  @Override
  public void adjustScore(TeamId teamId, int adjustment) {
    TeamImpl team = getTeam(teamId);
    if (team.getPoints() + adjustment < 0)
      return;
    team.addPoints(adjustment);
    publishEvent(new ScoreChangedEvent(teamId, adjustment));
  }

  @Override
  public void packCompletedLap() {
    publishEvent(new PackLapEvent());
  }
}
