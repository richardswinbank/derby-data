package net.firefive.derby.boutmodel.impl;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.firefive.derby.boutmodel.JamPlayState;
import net.firefive.derby.boutmodel.PlayRole;
import net.firefive.derby.boutmodel.PlayZone;
import net.firefive.derby.boutmodel.Skater;
import net.firefive.derby.boutmodel.StarSite;
import net.firefive.derby.boutmodel.TeamId;

class SkaterImpl implements Skater {

  private static int nextId = 0;

  private static int getNewId() {
    return nextId++;
  }

  public static void newBout() {
    nextId = 0;
  }

  private int id;
  private String name;
  private String number;
  private TeamId teamId;
  private PlayRole role;
  private boolean isLeadJammer;
  private StarSite hasStar;
  private PlayZone zone;

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[id=" + id + ",name=" + name + ",number=" + number + ",teamId=" + teamId
        + ",zone=" + zone + ",role=" + role + ",isLeadJammer=" + isLeadJammer + ",hasStar=" + hasStar + "]";
  }

  public SkaterImpl(String number, String name, TeamId teamId) {
    id = SkaterImpl.getNewId();
    this.number = number;
    this.name = name;
    this.teamId = teamId;

    setZone(PlayZone.BENCH, JamPlayState.BEFORE);
    resetBoxTimer();
    penalties = 0;
  }

  public boolean setZone(PlayZone zone, JamPlayState jamState) {
    if (zone == this.zone)
      return false;

    this.zone = zone;
    if (zone == PlayZone.BENCH)
      setRole(PlayRole.NONE);

    if (zone == PlayZone.BOX) {
      penalties++;
      isLeadJammer = false; // rule 3.4.7.2
      if (jamState == JamPlayState.DURING)
        startBoxTimer(); // should only really happen DURING
    }
    else
      resetBoxTimer();

    return true;
  }

  public boolean setRole(PlayRole newRole) {
    PlayRole oldRole = this.role;
    if (newRole == oldRole)
      return false;

    // role is changing - assume outside jam
    isLeadJammer = false; // at any role change
    if (newRole == PlayRole.JAMMER)
      hasStar = StarSite.ON_HELMET; // on becoming jammer
    else
      hasStar = StarSite.NO_STAR;

    if (zone == PlayZone.BENCH && newRole != PlayRole.NONE)
      zone = PlayZone.TRACK;

    this.role = newRole;
    return true;
  }

  public boolean setLeadJammer(boolean b) {
    if (role == PlayRole.JAMMER && hasStar == StarSite.ON_HELMET && isLeadJammer != b) {
      isLeadJammer = b;
      return true;
    }
    return false;
  }

  public boolean setStar(StarSite site) {
    if (hasStar == site)
      return false;

    hasStar = site;
    if (role == PlayRole.JAMMER && site != StarSite.ON_HELMET)
      isLeadJammer = false; // rule 3.4.7.1/3

    return true;
  }

  /*
   * penalty management
   */
  private int penalties;
  private boolean timerRunning;
  private long timerLastStarted;
  private long timeServed;

  public void startBoxTimer() {
    timerLastStarted = System.currentTimeMillis();
    timerRunning = true;
  }

  public void stopBoxTimer() {
    timerRunning = false;
    timeServed += System.currentTimeMillis() - timerLastStarted;
  }

  public void resetBoxTimer() {
    timerRunning = false;
    timeServed = 0;
  }

  public long getTimeInBox() {
    if (!timerRunning)
      return timeServed;
    return timeServed + System.currentTimeMillis() - timerLastStarted;
  }

  @Override
  public boolean equals(Object o) {
    return ((SkaterImpl) o).id == id;
  }

  @Override
  public int getSkaterId() {
    return id;
  }

  @Override
  public String getNumber() {
    return number;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public TeamId getTeamId() {
    return teamId;
  }

  @Override
  public PlayZone getZone() {
    return zone;
  }

  @Override
  public PlayRole getRole() {
    return role;
  }

  @Override
  public StarSite getStar() {
    return hasStar;
  }

  @Override
  public boolean isLeadJammer() {
    return isLeadJammer;
  }

  @Override
  public int getPenalties() {
    return penalties;
  }

  /*
   * support for Gson deserialisation
   */
  public static JsonDeserializer<Skater> getGsonDeserializer() {
    return new SkaterImpl("", "", null).new Deserialiser();
  }

  private class Deserialiser implements JsonDeserializer<Skater> {
    @Override
    public Skater deserialize(JsonElement e, Type t, JsonDeserializationContext jdc) throws JsonParseException {
      // System.out.println(getClass().getName() + ".deserialize() (1)nextId=" +
      // nextId);
      JsonObject o = e.getAsJsonObject();

      SkaterImpl s = new SkaterImpl(o.get("number").getAsString(), o.get("name").getAsString(),
          TeamId.valueOf(o.get("teamId").getAsString()));

      s.id = o.get("id").getAsInt();
      if (nextId <= s.id)
        nextId = s.id + 1;

      s.zone = PlayZone.valueOf(o.get("zone").getAsString());
      s.role = PlayRole.valueOf(o.get("role").getAsString());
      s.hasStar = StarSite.valueOf(o.get("hasStar").getAsString());
      s.isLeadJammer = o.get("isLeadJammer").getAsBoolean();
      s.penalties = o.get("penalties").getAsInt();
      s.timerRunning = o.get("timerRunning").getAsBoolean();
      s.timeServed = o.get("timeServed").getAsLong();
      s.timerLastStarted = o.get("timerLastStarted").getAsLong();
      s.timeServed = o.get("timeServed").getAsLong();

      // System.out.println(getClass().getName() + ".deserialize() (1)nextId=" +
      // nextId);
      return s;
    }
  }
}
