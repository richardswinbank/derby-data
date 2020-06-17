package net.firefive.derby.boutmodel.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

import net.firefive.derby.boutmodel.BoutObserver;
import net.firefive.derby.boutmodel.events.AbstractBoutEvent;

class BoutTraceWriter implements BoutObserver, AutoCloseable {

  private PrintWriter writer;

  public BoutTraceWriter(String logFileName) throws IOException {
    writer = new PrintWriter(new FileWriter(logFileName, true));
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    if ((new File(logFileName)).length() == 0) {
      writer.println("# ");
      writer.println("# Bout event trace");
      writer.println("# Created " + dateFormatter.format(System.currentTimeMillis()));
      writer.println("# ");
    }else{
      writer.println("# Connected input stream " + dateFormatter.format(System.currentTimeMillis()));
    }
    writer.flush();
  }

  @Override
  public void handleBoutEvent(AbstractBoutEvent evt) {
    writer.println(evt);
    writer.flush();
  }

  @Override
  public void close() throws IOException {
    writer.flush();
    writer.close();
  }

}
