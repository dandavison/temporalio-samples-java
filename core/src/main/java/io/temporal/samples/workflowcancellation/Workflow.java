package io.temporal.workflowcancellation;

import io.temporal.workflow.UpdateMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface Workflow {
  @WorkflowMethod
  void run();

  @UpdateMethod
  void updateThatThrows();
}
