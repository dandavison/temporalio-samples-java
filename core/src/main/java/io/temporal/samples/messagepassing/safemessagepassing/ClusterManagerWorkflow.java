package io.temporal.samples.messagepassing.safemessagepassing;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.UpdateMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface ClusterManagerWorkflow {

    @WorkflowMethod
    void run();

    @SignalMethod
    void startCluster();

    @SignalMethod
    void shutdownCluster();

    @UpdateMethod
    void assignNodesToJob(AssignNodesToJobUpdateInput input);

    @UpdateMethod
    void deleteJob(DeleteJobUpdateInput input);

    @QueryMethod
    ClusterManagerStateSummary getClusterStatusQuery();
}
