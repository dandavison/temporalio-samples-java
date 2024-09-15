package io.temporal.samples.messagepassing.safemessagepassing;

public class ClusterManagerStateSummary {

    private final int maxAssignedNodes;
    private final int assignedNodes;

    public ClusterManagerStateSummary(int maxAssignedNodes, int assignedNodes) {
        super();
        this.maxAssignedNodes = maxAssignedNodes;
        this.assignedNodes = assignedNodes;
    }

    public int getMaxAssignedNodes() {
        return maxAssignedNodes;
    }

    public int getAssignedNodes() {
        return assignedNodes;
    }
}
