package io.temporal.samples.hello;

import io.temporal.activity.ActivityInterface;
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

public class CommandProcessor {
  static final String TASK_QUEUE = "MyTaskQueue";
  static final String WORKFLOW_ID = "MyWorkflowId";

  @WorkflowInterface
  public interface CommandProcessorWorkflow {
    @WorkflowMethod
    String startProcessing();

    @UpdateMethod
    String submitCommand(String command);
  }

  @ActivityInterface
  public interface MyActivities {
    String processCommand(String command);
  }

  public static class MyWorkflowImpl implements CommandProcessorWorkflow {

    private ArrayList<String> commandQueue;

    public MyWorkflowImpl() {
      this.commandQueue = new ArrayList<>();
    }

    // private final MyActivities activities =
    //     Workflow.newActivityStub(
    //         MyActivities.class,
    //         ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());

    @Override
    public String startProcessing() {
      System.out.println("starting workflow");
      Workflow.await(
          () -> {
            System.out.println("evaluating await condition: " + this.commandQueue.size());
            return this.commandQueue.size() > 0;
          });
      System.out.println("passed await");
      return "done";
    }

    @Override
    public String submitCommand(String command) {
      System.out.println("submitting: " + command);
      this.commandQueue.add(command);
      // activities.processCommand(command); // TODO
      String result = command + " [result]";
      return result;
    }
  }

  static class MyActivitiesImpl implements MyActivities {
    @Override
    public String processCommand(String command) {
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
    String result = commandProcessor.submitCommand("my-command");
    System.out.println(result);

    String output = WorkflowStub.fromTyped(commandProcessor).getResult(String.class);
    System.out.println(output);
    System.exit(0);
  }
}
