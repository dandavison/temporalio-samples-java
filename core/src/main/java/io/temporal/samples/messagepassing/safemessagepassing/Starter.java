package io.temporal.samples.messagepassing.safemessagepassing;

import io.temporal.api.enums.v1.WorkflowIdReusePolicy;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

public class Starter {

    public static final String TASK_QUEUE = "clustermanager";
    private static final WorkflowServiceStubs service;
    private static final WorkflowClient client;
    private static final WorkerFactory factory;

    static {
        service = WorkflowServiceStubs.newLocalServiceStubs();
        client = WorkflowClient.newInstance(service);
        factory = WorkerFactory.newInstance(client);
    }

    private Starter() {
        super();
    }

    public static void main(String[] args) {
        Worker worker = factory.newWorker(TASK_QUEUE);
        worker.registerWorkflowImplementationTypes(ClusterManagerWorkflowImpl.class);
        factory.start();

        WorkflowOptions workflowOptions = WorkflowOptions.newBuilder()
                .setWorkflowId("clustermanager")
                .setTaskQueue(TASK_QUEUE)
                .setWorkflowIdReusePolicy(WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_TERMINATE_IF_RUNNING)
                .build();

        ClusterManagerWorkflow workflowStub = client.newWorkflowStub(ClusterManagerWorkflow.class, workflowOptions);

        workflowStub.run();

        System.exit(0);
    }
}
