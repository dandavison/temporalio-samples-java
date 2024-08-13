package io.temporal.samples.scratchpad;

import io.temporal.api.enums.v1.WorkflowIdReusePolicy;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.workflow.UpdateMethod;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ErrorInUpdateHandler {
  static final String TASK_QUEUE = "ScratchpadTaskQueue";
  static final String WORKFLOW_ID = "ScratchpadWorkflow";

  @WorkflowInterface
  public interface MyWorkflow {
    @WorkflowMethod
    void run();

    @UpdateMethod
    void updateThatThrows();
  }

  public static class MyWorkflowImpl implements MyWorkflow {

    boolean cond = false;

    @Override
    public void run() {
      Workflow.await(() -> false);
    }

    @Override
    public void updateThatThrows() {
      throw new RuntimeException("Unhandled exception in update handler");
    }
  }

  public static void log(String msg) {
    Path path = Paths.get("/tmp/java-client");
    try (var writer =
        Files.newBufferedWriter(
            path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
      writer.write(msg + "\n");
    } catch (IOException e) {
      System.err.println("Failed to write to file: " + e.getMessage());
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

    log("about to run workflow");
    WorkflowClient.start(workflow::run);
    log("about to execute update");
    workflow.updateThatThrows();
    log("executed update");
    System.exit(0);
  }
}
