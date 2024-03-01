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
import io.temporal.workflow.CompletablePromise;
import io.temporal.workflow.UpdateMethod;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class CommandProcessor {
  static final String TASK_QUEUE = "MyTaskQueue";
  static final String WORKFLOW_ID = "MyWorkflowId";

  @WorkflowInterface
  public interface CommandProcessorWorkflow {
    @WorkflowMethod
    void startProcessing();

    @UpdateMethod
    String processCommand(int command);

    @UpdateMethod
    void stop();
  }

  @ActivityInterface
  public interface MyActivities {
    String processCommand(int command);
  }

  public static class CommandProcessorWorkflowImpl implements CommandProcessorWorkflow {

    private final MyActivities activities =
        Workflow.newActivityStub(
            MyActivities.class,
            ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(10)).build());

    private ArrayList<CompletablePromise<Void>> queue;
    private boolean done;

    @Override
    public void startProcessing() {
      Workflow.await(() -> this.done);
    }

    @Override
    public String processCommand(int command) {
      _wait(command); // [p1, p2]
      String result = activities.processCommand(command);
      _notify(command);
      return result;
    }

    private void _wait(int command) {
      System.out.printf("_xwait(%d): queue = %s\n", command, this.queue);
      boolean first = this.queue.isEmpty();
      CompletablePromise<Void> p = Workflow.newPromise();
      this.queue.add(p);
      if (first) {
        return;
      }
      System.out.println("p.get()... " + p);
      p.get();
      System.out.printf("... done p.get()\n");
    }

    private void _notify(int command) {
      System.out.printf("_xnotify(%d)... queue = %s\n", command, queue);
      this.queue.remove(0); // remove self
      if (this.queue.isEmpty()) {
        return;
      }
      CompletablePromise<Void> next = this.queue.get(0);
      System.out.printf("_xnotify(%d)... completing %s\n", command, next);
      next.complete(null);
      System.out.printf("... done _xnotify(%d)\n", command);
    }

    @Override
    public void stop() {
      this.done = true;
    }

    public CommandProcessorWorkflowImpl() {
      this.queue = new ArrayList<>();
      this.done = false;
    }
  }

  static class MyActivitiesImpl implements MyActivities {
    @Override
    public String processCommand(int commandNum) {
      try {
        // Earlier commands are slower, so we must serialize if they are to complete in order of
        // receipt.
        Thread.sleep(1000L * (3 - commandNum));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      String result = commandNum + " [processed]";
      System.out.println(result);
      return result;
    }
  }

  public static void main(String[] args) {
    WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
    WorkflowClient client = WorkflowClient.newInstance(service);
    WorkerFactory factory = WorkerFactory.newInstance(client);
    Worker worker = factory.newWorker(TASK_QUEUE);
    worker.registerWorkflowImplementationTypes(CommandProcessorWorkflowImpl.class);
    worker.registerActivitiesImplementations(new MyActivitiesImpl());
    factory.start();
    CommandProcessorWorkflow commandProcessor =
        client.newWorkflowStub(
            CommandProcessorWorkflow.class,
            WorkflowOptions.newBuilder()
                .setWorkflowId(WORKFLOW_ID)
                .setTaskQueue(TASK_QUEUE)
                .setWorkflowIdReusePolicy(
                    // This is just for convenience during command-line development!
                    WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_TERMINATE_IF_RUNNING)
                .build());

    WorkflowStub untypedWorkflowStub = WorkflowStub.fromTyped(commandProcessor);

    WorkflowClient.start(commandProcessor::startProcessing);

    CompletableFuture.allOf(
            untypedWorkflowStub.startUpdate("processCommand", String.class, 1).getResultAsync(),
            untypedWorkflowStub.startUpdate("processCommand", String.class, 2).getResultAsync(),
            untypedWorkflowStub.startUpdate("processCommand", String.class, 3).getResultAsync())
        .join();
    commandProcessor.stop();
    untypedWorkflowStub.getResult(String.class);
    System.exit(0);
  }
}
