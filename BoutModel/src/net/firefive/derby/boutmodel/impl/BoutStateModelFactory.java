package net.firefive.derby.boutmodel.impl;

import java.io.FileNotFoundException;

import net.firefive.derby.boutmodel.BoutStateModel;
import net.firefive.derby.boutmodel.events.BoutEventStream;

public class BoutStateModelFactory {

  public static BoutEventStream getStream(String fileName) throws FileNotFoundException
  {
    return new BoutTraceReader(fileName);
  }
  
  public static BoutStateModel getModel(String logFileDir) {
    return new BoutStateModelImpl(logFileDir);
  }

}
