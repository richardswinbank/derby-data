package net.firefive.derby.boutmodel;

@SuppressWarnings("serial")
public class BoutConfigurationException extends RuntimeException {

  public BoutConfigurationException(String msg) {
    super(msg);
  }

  public BoutConfigurationException(Exception e) {
    super(e);
  }

}
