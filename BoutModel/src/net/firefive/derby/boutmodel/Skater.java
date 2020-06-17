package net.firefive.derby.boutmodel;

public interface Skater {
	
  // get the internal ID of this skater object
  public int getSkaterId();

	public String getNumber();

  public String getName();

  public TeamId getTeamId();

	public PlayZone getZone();

  public PlayRole getRole();

  public StarSite getStar();

  public boolean isLeadJammer();

	// properties related to penalties & IN_BOX state

  public int getPenalties();

  // if in box, return time spent in there so far
  // otherwise zero
  public long getTimeInBox();
}
