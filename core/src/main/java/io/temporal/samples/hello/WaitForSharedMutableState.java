package io.temporal.samples.hello;

import io.temporal.api.enums.v1.WorkflowIdReusePolicy;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

public class WaitForSharedMutableState {
  static final String TASK_QUEUE = "ScratchpadTaskQueue";
  static final String WORKFLOW_ID = "ScratchpadWorkflow";

  @WorkflowInterface
  public interface MyWorkflow {
    @WorkflowMethod
    void run();
  }

  public static class MyWorkflowImpl implements MyWorkflow {

    boolean cond = false;

    @Override
    public void run() {
      cond = true;
      Promise<Void> p1 = Async.procedure(this::coro, "1");
      Promise<Void> p2 = Async.procedure(this::coro, "2");
      Promise.allOf(p1, p2).get();
    }

    public void coro(String id) {
      Workflow.await(() -> cond);
      System.out.println("coro " + id + " after wait, sees " + cond);
      cond = false;
      Workflow.sleep(1000);
      cond = true;
    }
  }

  public static void main(String[] args) {
    WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
    WorkflowClient client = WorkflowClient.newInstance(service);
    WorkerFactory factory = WorkerFactory.newInstance(client);
    Worker worker = factory.newWorker(TASK_QUEUE);
    worker.registerWorkflowImplementationTypes(MyWorkflowImpl.class);
    factory.start();
    MyWorkflow workflow =
        client.newWorkflowStub(
            MyWorkflow.class,
            WorkflowOptions.newBuilder()
                .setWorkflowId(WORKFLOW_ID)
                .setTaskQueue(TASK_QUEUE)
                .setWorkflowIdReusePolicy(
                    WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_TERMINATE_IF_RUNNING)
                .build());

    workflow.run();
    System.exit(0);
  }
}
