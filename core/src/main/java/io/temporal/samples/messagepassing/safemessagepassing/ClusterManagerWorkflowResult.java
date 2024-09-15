package io.temporal.samples.messagepassing.safemessagepassing;

public class ClusterManagerWorkflowResult {

    private int maxAssignedNodes;
    private int numCurrentlyAssignedNodes;
    private int numBadNodes;

    public ClusterManagerWorkflowResult(int maxAssignedNodes, int numCurrentlyAssignedNodes, int numBadNodes) {
        super();
        this.maxAssignedNodes = maxAssignedNodes;
        this.numCurrentlyAssignedNodes = numCurrentlyAssignedNodes;
        this.numBadNodes = numBadNodes;
    }

    public int getMaxAssignedNodes() {
        return maxAssignedNodes;
    }

    public void setMaxAssignedNodes(int maxAssignedNodes) {
        this.maxAssignedNodes = maxAssignedNodes;
    }

    public int getNumCurrentlyAssignedNodes() {
        return numCurrentlyAssignedNodes;
    }

    public void setNumCurrentlyAssignedNodes(int numCurrentlyAssignedNodes) {
        this.numCurrentlyAssignedNodes = numCurrentlyAssignedNodes;
    }

    public int getNumBadNodes() {
        return numBadNodes;
    }

    public void setNumBadNodes(int numBadNodes) {
        this.numBadNodes = numBadNodes;
    }
}
