package net.firefive.derby.boutmodel.impl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.firefive.derby.boutmodel.Skater;
import net.firefive.derby.boutmodel.Team;
import net.firefive.derby.boutmodel.events.AbstractBoutEvent;
import net.firefive.derby.boutmodel.events.BoutEventStream;

class BoutTraceReader implements BoutEventStream, AutoCloseable {

  private AbstractBoutEvent nextEvent;
  private BufferedReader reader;
  private Gson gson;
  private int line;

  public BoutTraceReader(String logFile) throws FileNotFoundException {
    reader = new BufferedReader(new FileReader(logFile));
    line = 0;

    GsonBuilder gb = new GsonBuilder();
    gb.registerTypeAdapter(Team.class, TeamImpl.getGsonDeserializer());
    gb.registerTypeAdapter(Skater.class, SkaterImpl.getGsonDeserializer());
    gson = gb.create();
  }

  @Override
  public boolean hasMoreEvents() {
    try {
      while (true) {
        String eventText = reader.readLine();
        if (eventText == null || eventText.length() == 0)
          return false;
        if(eventText.charAt(0) == '#')
          continue; // skip comment line
        line++;

        int i = eventText.indexOf(':');
        @SuppressWarnings("rawtypes")
        Class eventClass = Class.forName(eventText.substring(0, i));
        @SuppressWarnings("unchecked")
        Object event = gson.fromJson(eventText.substring(i + 1), eventClass);
        nextEvent = (AbstractBoutEvent) event;

        // read next event first, then:
        return nextEvent != null;
      }
    } catch (Exception e) {
      throw new RuntimeException("Error at line " + line, e);
    }

  }

  @Override
  public AbstractBoutEvent getNextEvent() {
    return nextEvent;
  }

  @Override
  public void close() throws Exception {
    reader.close();
  }

}
