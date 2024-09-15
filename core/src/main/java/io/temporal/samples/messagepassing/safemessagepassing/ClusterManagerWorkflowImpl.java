package io.temporal.samples.messagepassing.safemessagepassing;

import io.temporal.client.*;
import io.temporal.workflow.*;

public class ClusterManagerWorkflowImpl implements ClusterManagerWorkflow {

  private ClusterManagerState state;

  public ClusterManagerWorkflowImpl() {
    this.state = new ClusterManagerState(false, false, new HashMap<>(), 0);
  }

  @Override
  public void run() {
    this.state.setClusterStarted(true);
  }

  @Override
  public void startCluster() {
    this.state.setClusterStarted(true);
  }

  @Override
  public void shutdownCluster() {
    this.state.setClusterShutdown(true);
  }

  @Override
  public void assignNodesToJob(AssignNodesToJobUpdateInput input) {
    this.state.setMaxAssignedNodes(input.getMaxAssignedNodes());
  }

  @Override
  public void deleteJob(DeleteJobUpdateInput input) {
    this.state.setMaxAssignedNodes(0);
  }
}
