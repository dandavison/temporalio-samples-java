package io.temporal.workflowcancellation;

import io.temporal.workflow.Workflow;

public static class WorkflowImpl implements Workflow {

  @Override
  public void run() {
    Workflow.await(() -> false);
  }

  @Override
  public void updateThatThrows() {
    throw new RuntimeException("Unhandled exception in update handler");
  }
}
