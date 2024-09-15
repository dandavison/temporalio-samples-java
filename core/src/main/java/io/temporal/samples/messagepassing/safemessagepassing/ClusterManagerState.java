package io.temporal.samples.messagepassing.safemessagepassing;

import java.util.Map;

public class ClusterManagerState {

  private boolean clusterStarted;
  private boolean clusterShutdown;
  private Map<String, String> nodes;
  private int maxAssignedNodes;

  public ClusterManagerState(
      boolean clusterStarted,
      boolean clusterShutdown,
      Map<String, String> nodes,
      int maxAssignedNodes) {
    super();
    this.clusterStarted = clusterStarted;
    this.clusterShutdown = clusterShutdown;
    this.nodes = nodes;
    this.maxAssignedNodes = maxAssignedNodes;
  }
}
