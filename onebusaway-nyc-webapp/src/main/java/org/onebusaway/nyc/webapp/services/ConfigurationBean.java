package org.onebusaway.nyc.webapp.services;

public class ConfigurationBean {

  private int noProgressTimeout = 2 * 60;

  private double offRouteDistance = 500;

  private int staleDataTimeout = 300;

  private int staleDataGhostTimeout = 120;

  public ConfigurationBean() {

  }

  public ConfigurationBean(ConfigurationBean bean) {
    applyPropertiesFromBean(bean);
  }

  public void applyPropertiesFromBean(ConfigurationBean bean) {
    this.noProgressTimeout = bean.noProgressTimeout;
    this.offRouteDistance = bean.offRouteDistance;
    this.staleDataTimeout = bean.staleDataTimeout;
    this.staleDataGhostTimeout = bean.staleDataGhostTimeout;
  }

  public int getNoProgressTimeout() {
    return noProgressTimeout;
  }

  public void setNoProgressTimeout(int noProgressTimeout) {
    this.noProgressTimeout = noProgressTimeout;
  }

  public double getOffRouteDistance() {
    return offRouteDistance;
  }

  public void setOffRouteDistance(double offRouteDistance) {
    this.offRouteDistance = offRouteDistance;
  }

  public int getStaleDataTimeout() {
    return staleDataTimeout;
  }

  public void setStaleDataTimeout(int staleDataTimeout) {
    this.staleDataTimeout = staleDataTimeout;
  }

  public int getStaleDataGhostTimeout() {
    return staleDataGhostTimeout;
  }

  public void setStaleDataGhostTimeout(int staleDataGhostTimeout) {
    this.staleDataGhostTimeout = staleDataGhostTimeout;
  }
}