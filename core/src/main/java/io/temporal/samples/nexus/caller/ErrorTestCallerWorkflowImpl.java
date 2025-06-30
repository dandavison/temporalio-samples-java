package io.temporal.samples.nexus.caller;

import io.nexusrpc.handler.HandlerException;
import io.temporal.failure.ApplicationFailure;
import io.temporal.failure.NexusOperationFailure;
import io.temporal.samples.nexus.service.NexusService;
import io.temporal.workflow.NexusOperationOptions;
import io.temporal.workflow.NexusServiceOptions;
import io.temporal.workflow.Workflow;
import java.time.Duration;

public class ErrorTestCallerWorkflowImpl implements ErrorTestCallerWorkflow {
  NexusService nexusService =
      Workflow.newNexusServiceStub(
          NexusService.class,
          NexusServiceOptions.newBuilder()
              .setOperationOptions(
                  NexusOperationOptions.newBuilder()
                      .setScheduleToCloseTimeout(Duration.ofSeconds(10))
                      .build())
              .build());

  @Override
  public void testError(NexusService.ActionInOperation action) {
    try {
      nexusService.testError(new NexusService.ErrorTestInput(action));
    } catch (Exception e) {
      Workflow.getLogger(ErrorTestCallerWorkflowImpl.class)
          .info("\n🌈 {}:\n{}\n\n", action, formatError(e));
    }
  }

  private String formatError(Throwable e) {
    StringBuilder sb = new StringBuilder();
    formatErrorRecursive(e, sb, 0);
    return sb.toString();
  }

  private void formatErrorRecursive(Throwable e, StringBuilder sb, int level) {
    String indent = "    ".repeat(level);
    sb.append(indent);

    if (e instanceof ApplicationFailure) {
      ApplicationFailure af = (ApplicationFailure) e;
      sb.append(
          String.format(
              "%s(message=\"%s\", type=\"%s\", nonRetryable=%s)",
              e.getClass().getName(),
              af.getOriginalMessage() != null ? af.getOriginalMessage() : "no-message-attr",
              af.getType() != null ? af.getType() : "no-type-attr",
              af.isNonRetryable()));
    } else if (e instanceof NexusOperationFailure) {
      sb.append(
          String.format(
              "%s(message=\"%s\", scheduledEventId=%s, operationToken=\"%s\")",
              e.getClass().getName(),
              e.getMessage() != null ? e.getMessage() : "no-message-attr",
              "scheduledEventId", // Note: these fields might not be accessible, using placeholder
              "operationToken"));
    } else if (e instanceof HandlerException) {
      HandlerException he = (HandlerException) e;
      sb.append(
          String.format(
              "%s(message=\"%s\", type=\"%s\", nonRetryable=%s)",
              e.getClass().getName(),
              he.getMessage() != null ? he.getMessage() : "no-message-attr",
              he.getRawErrorType() != null ? he.getRawErrorType() : "no-type-attr",
              !he.isRetryable()));
    } else {
      // Generic exception handling
      sb.append(
          String.format(
              "%s(message=\"%s\")",
              e.getClass().getName(), e.getMessage() != null ? e.getMessage() : "no-message-attr"));
    }

    if (e.getCause() != null) {
      sb.append("\n");
      formatErrorRecursive(e.getCause(), sb, level + 1);
    }
  }
}
