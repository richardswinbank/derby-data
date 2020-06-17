package net.firefive.derby.boutmodel;

import java.awt.Color;

public interface BoutStateModel {

  public void recoverState();

  public void newBout();

  public int TEAM_TIMEOUTS = 3;
  // all durations reported IN MILLISECONDS
  public long PERIOD_DURATION = 1800000;
  public long JAM_DURATION = 120000;
  public long PENALTY_DURATION = 30000; // time server for a penalty

  public void addObserver(BoutObserver observer);

  public void removeObserver(BoutObserver observer);

  // public void syncObserver(BoutObserver observer);

  /*
   * bout participants
   */
  public void updateTeam(TeamId teamId, String name, Color primary, Color secondary) throws BoutConfigurationException;

  public Team getTeam(TeamId teamId);

  public Skater getSkater(int skaterId);

  public void addSkater(String number, String name, TeamId teamId);

  public void removeSkater(int skaterId);

  /*
   * bout states
   */
  public PeriodState getPeriodState();

  // public void setPeriodPlayState(PeriodPlayState state);

  public void newPeriod();

  public void startPlay();

  public void officialTimeout();

  public void teamTimeout(TeamId team);

  public void officialReview(TeamId team);

  public void endPeriod();

  public JamState getJamState();

  // public void setJamPlayState(JamPlayState state);

  public void startJam();

  public void callOffJam(TeamId team);

  public void stopJam();

  public void resetJam();

  public void packCompletedLap();

  /*
   * skater information
   */
  public void setSkaterZone(int skaterId, PlayZone zone);

  public void setSkaterRole(int skaterId, PlayRole role);

  public void setLeadJammer(TeamId teamId);

  public void passStar(TeamId teamId, PlayRole role, StarSite site);

  public Skater getJammer(TeamId teamId);

  public Skater getPivot(TeamId teamId);

  /*
   * team information
   */
  public void completePass(TeamId teamId, int points);

  public void adjustScore(TeamId teamId, int adjustment);

}
