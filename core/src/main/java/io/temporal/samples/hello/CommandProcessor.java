package io.temporal.samples.hello;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityOptions;
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
import java.time.Duration;
import java.util.ArrayList;

public class CommandProcessor {
  static final String TASK_QUEUE = "MyTaskQueue";
  static final String WORKFLOW_ID = "MyWorkflowId";

  @WorkflowInterface
  public interface CommandProcessorWorkflow {
    @WorkflowMethod
    String startProcessing();

    @UpdateMethod
    String submitCommand(int command);
  }

  @ActivityInterface
  public interface MyActivities {
    String processCommand(int command);
  }

  public static class MyWorkflowImpl implements CommandProcessorWorkflow {

    private ArrayList<Integer> commandQueue;
    private boolean done;

    public MyWorkflowImpl() {
      this.commandQueue = new ArrayList<>();
      this.done = false;
    }

    private final MyActivities activities =
        Workflow.newActivityStub(
            MyActivities.class,
            ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(10)).build());

    @Override
    public String startProcessing() {
      Workflow.await(() -> this.done);
      return "done";
    }

    @Override
    public String submitCommand(int command) {
      if (command < 0) {
        this.done = true;
        return "stopping workflow";
      }
      this.commandQueue.add(command);
      String result = activities.processCommand(command);
      return result;
    }
  }

  static class MyActivitiesImpl implements MyActivities {
    @Override
    public String processCommand(int command) {
      try {
        // Earlier commands are slower, so we must serialize if they are to complete in order of
        // receipt.
        Thread.sleep(1000L * (3 - command));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      return command + " [processed]";
    }
  }

  public static void main(String[] args) {
    WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
    WorkflowClient client = WorkflowClient.newInstance(service);
    WorkerFactory factory = WorkerFactory.newInstance(client);
    Worker worker = factory.newWorker(TASK_QUEUE);
    worker.registerWorkflowImplementationTypes(MyWorkflowImpl.class);
    worker.registerActivitiesImplementations(new MyActivitiesImpl());
    factory.start();
    CommandProcessorWorkflow commandProcessor =
        client.newWorkflowStub(
            CommandProcessorWorkflow.class,
            WorkflowOptions.newBuilder()
                .setWorkflowId(WORKFLOW_ID)
                .setTaskQueue(TASK_QUEUE)
                .setWorkflowIdReusePolicy(
                    WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_TERMINATE_IF_RUNNING)
                .build());

    WorkflowClient.start(commandProcessor::startProcessing);
    String result = commandProcessor.submitCommand(1);
    System.out.println(result);
    result = commandProcessor.submitCommand(2);
    System.out.println(result);
    result = commandProcessor.submitCommand(-1);
    System.out.println(result);

    String output = WorkflowStub.fromTyped(commandProcessor).getResult(String.class);
    System.out.println(output);
    System.exit(0);
  }
}
