package net.firefive.derby.boutmodel.impl;

import java.awt.Color;
import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.firefive.derby.boutmodel.Team;

class TeamImpl implements Team {

  private String name;
  private Color primaryColour;
  private Color secondaryColour;

  // used to sanity-check downwards adjustments
  private int points;

  public TeamImpl(String name, Color c1, Color c2) {
    this.name = name == null ? "" : name;
    primaryColour = c1 == null ? Color.BLACK : c1;
    secondaryColour = c2 == null ? Color.BLACK : c2;
  }

  @Override
  public Color getPrimaryColour() {
    return primaryColour;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Color getSecondaryColour() {
    return secondaryColour;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPrimaryColor(Color c) {
    if (c == null)
      return;
    this.primaryColour = c;
  }

  public void setSecondaryColor(Color c) {
    if (c == null)
      return;
    this.secondaryColour = c;
  }

  public void addPoints(int points) {
    this.points += points;
  }

  public int getPoints() {
    return points;
  }

  /*
   * support for Gson deserialisation
   */
  public static JsonDeserializer<Team> getGsonDeserializer() {
    return new TeamImpl("", null, null).new Deserialiser();
  }

  private class Deserialiser implements JsonDeserializer<Team> {
    @Override
    public Team deserialize(JsonElement e, Type type, JsonDeserializationContext jdc) throws JsonParseException {
      JsonObject o = e.getAsJsonObject();

      TeamImpl t = new TeamImpl(o.get("name").getAsString(), null, null);
      t.setPrimaryColor(new Color(o.getAsJsonObject("primaryColour").get("value").getAsInt()));
      t.setSecondaryColor(new Color(o.getAsJsonObject("secondaryColour").get("value").getAsInt()));
      t.points = o.get("points").getAsInt();

      return t;
    }
  }
}
