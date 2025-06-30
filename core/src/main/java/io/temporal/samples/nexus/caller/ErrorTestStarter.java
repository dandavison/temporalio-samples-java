package io.temporal.samples.nexus.caller;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.samples.nexus.options.ClientOptions;
import io.temporal.samples.nexus.service.NexusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorTestStarter {
  private static final Logger logger = LoggerFactory.getLogger(ErrorTestStarter.class);

  public static void main(String[] args) {
    WorkflowClient client = ClientOptions.getWorkflowClient(args);

    WorkflowOptions workflowOptions =
        WorkflowOptions.newBuilder().setTaskQueue(CallerWorker.DEFAULT_TASK_QUEUE_NAME).build();

    // Test all error scenarios
    NexusService.ActionInOperation[] actions = {
      NexusService.ActionInOperation.RAISE_APPLICATION_ERROR,
      NexusService.ActionInOperation.RAISE_CUSTOM_ERROR,
      NexusService.ActionInOperation.RAISE_APPLICATION_ERROR_WITH_CAUSE_OF_CUSTOM_ERROR,
      NexusService.ActionInOperation.RAISE_NEXUS_HANDLER_ERROR,
      NexusService.ActionInOperation.RAISE_NEXUS_OPERATION_ERROR_WITH_CAUSE_OF_CUSTOM_ERROR
    };

    for (NexusService.ActionInOperation action : actions) {
      ErrorTestCallerWorkflow workflow =
          client.newWorkflowStub(ErrorTestCallerWorkflow.class, workflowOptions);

      logger.info("Starting error test workflow for action: {}", action);
      workflow.testError(action);
      logger.info("Completed error test workflow for action: {}", action);
    }
  }
}
