package net.firefive.derby.boutmodel;

public class JamState {

  private JamPlayState state;
  private long timeRemaining;

  public JamState(JamPlayState state, long timeRemaining) {
    this.state = state;
    this.timeRemaining = timeRemaining;
  }

  public JamPlayState getPlayState() {
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
