package net.firefive.derby.boutmodel;

public class PeriodState {

  private PeriodPlayState state;
  private long timeRemaining;

  public PeriodState(PeriodPlayState state, long timeRemaining) {
    this.state = state;
    this.timeRemaining = timeRemaining;
  }

  public PeriodPlayState getPlayState() {
    return state;
  }
  
  public long getRemainingTime() {
    return timeRemaining;
  }
  
  public String toString()
  {
	  return state + " " + timeRemaining;
  }
}
