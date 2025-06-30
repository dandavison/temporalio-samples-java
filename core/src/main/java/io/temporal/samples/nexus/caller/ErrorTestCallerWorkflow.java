package io.temporal.samples.nexus.caller;

import io.temporal.samples.nexus.service.NexusService;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface ErrorTestCallerWorkflow {
  @WorkflowMethod
  void testError(NexusService.ActionInOperation action);
}
