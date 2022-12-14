package io.aiven.klaw.clusterapi.models;

public enum ClusterResponseStatus {
  ONLINE("ONLINE"),
  OFFLINE("OFFLINE");

  public final String value;

  private ClusterResponseStatus(String value) {
    this.value = value;
  }
}
