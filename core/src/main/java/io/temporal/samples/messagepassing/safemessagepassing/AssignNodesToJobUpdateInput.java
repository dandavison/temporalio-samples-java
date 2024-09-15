package io.temporal.samples.messagepassing.safemessagepassing;

public class AssignNodesToJobUpdateInput {

  private int numNodes;
  private String jobName;

  public AssignNodesToJobUpdateInput(int numNodes, String jobName) {
    super();
    this.numNodes = numNodes;
    this.jobName = jobName;
  }

  public int getNumNodes() {
    return numNodes;
  }
}
