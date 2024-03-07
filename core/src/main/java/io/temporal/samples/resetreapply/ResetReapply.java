package io.temporal.samples.resetreapply;

import io.temporal.api.enums.v1.WorkflowIdReusePolicy;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.workflow.UpdateMethod;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import java.util.ArrayList;

public class ResetReapply {
  static final String TASK_QUEUE = "MyTaskQueue";
  static final String WORKFLOW_ID = "MyWorkflowId";

  @WorkflowInterface
  public interface ResetReapplyWorkflow {
    @WorkflowMethod
    ArrayList<String> myWorkflow();

    @UpdateMethod
    ArrayList<String> myUpdate(String arg);
  }

  public static class ResetReapplyWorkflowImpl implements ResetReapplyWorkflow {

    private ArrayList<String> updateArgs;

    @Override
    public ArrayList<String> myWorkflow() {
      Workflow.await(() -> this.updateArgs.size() > 0);
      return updateArgs;
    }

    @Override
    public ArrayList<String> myUpdate(String arg) {
      this.updateArgs.add(arg);
      return this.updateArgs;
    }

    public ResetReapplyWorkflowImpl() {
      this.updateArgs = new ArrayList<>();
    }
  }

  public static void main(String[] args) {
    WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
    WorkflowClient client = WorkflowClient.newInstance(service);
    WorkerFactory factory = WorkerFactory.newInstance(client);
    Worker worker = factory.newWorker(TASK_QUEUE);
    worker.registerWorkflowImplementationTypes(ResetReapplyWorkflowImpl.class);
    factory.start();
    ResetReapplyWorkflow resetReapply =
        client.newWorkflowStub(
            ResetReapplyWorkflow.class,
            WorkflowOptions.newBuilder()
                .setWorkflowId(WORKFLOW_ID)
                .setTaskQueue(TASK_QUEUE)
                .setWorkflowIdReusePolicy(
                    WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_TERMINATE_IF_RUNNING)
                .build());

    WorkflowClient.start(resetReapply::myWorkflow);
    ArrayList<String> updateResult = resetReapply.myUpdate("1");
    System.out.println("before reset: update result: " + updateResult);
    ArrayList<String> wfResult =
        (ArrayList<String>) WorkflowStub.fromTyped(resetReapply).getResult(ArrayList.class);
    System.out.println("before reset: wf result: " + wfResult);

    System.exit(0);
  }
}
