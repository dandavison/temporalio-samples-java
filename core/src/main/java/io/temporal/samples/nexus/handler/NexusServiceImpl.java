package io.temporal.samples.nexus.handler;

import io.nexusrpc.OperationException;
import io.nexusrpc.handler.HandlerException;
import io.nexusrpc.handler.OperationHandler;
import io.nexusrpc.handler.OperationImpl;
import io.nexusrpc.handler.ServiceImpl;
import io.temporal.client.WorkflowOptions;
import io.temporal.failure.ApplicationFailure;
import io.temporal.nexus.Nexus;
import io.temporal.nexus.WorkflowRunOperation;
import io.temporal.samples.nexus.service.NexusService;

// To create a service implementation, annotate the class with @ServiceImpl and provide the
// interface that the service implements. The service implementation class should have methods that
// return OperationHandler that correspond to the operations defined in the service interface.
@ServiceImpl(service = NexusService.class)
public class NexusServiceImpl {
  @OperationImpl
  public OperationHandler<NexusService.EchoInput, NexusService.EchoOutput> echo() {
    // OperationHandler.sync is a meant for exposing simple RPC handlers.
    return OperationHandler.sync(
        // The method is for making arbitrary short calls to other services or databases, or
        // perform simple computations such as this one. Users can also access a workflow client by
        // calling
        // Nexus.getOperationContext().getWorkflowClient(ctx) to make arbitrary calls such as
        // signaling, querying, or listing workflows.
        (ctx, details, input) -> new NexusService.EchoOutput(input.getMessage()));
  }

  @OperationImpl
  public OperationHandler<NexusService.HelloInput, NexusService.HelloOutput> hello() {
    // Use the WorkflowRunOperation.fromWorkflowMethod constructor, which is the easiest
    // way to expose a workflow as an operation. To expose a workflow with a different input
    // parameters then the operation or from an untyped stub, use the
    // WorkflowRunOperation.fromWorkflowHandler constructor and the appropriate constructor method
    // on WorkflowHandle.
    return WorkflowRunOperation.fromWorkflowMethod(
        (ctx, details, input) ->
            Nexus.getOperationContext()
                    .getWorkflowClient()
                    .newWorkflowStub(
                        HelloHandlerWorkflow.class,
                        // Workflow IDs should typically be business meaningful IDs and are used to
                        // dedupe workflow starts.
                        // For this example, we're using the request ID allocated by Temporal when
                        // the
                        // caller workflow schedules
                        // the operation, this ID is guaranteed to be stable across retries of this
                        // operation.
                        //
                        // Task queue defaults to the task queue this operation is handled on.
                        WorkflowOptions.newBuilder().setWorkflowId(details.getRequestId()).build())
                ::hello);
  }

  public static class MyCustomException extends RuntimeException {
    public MyCustomException(String message) {
      super(message);
    }
  }

  @OperationImpl
  public OperationHandler<NexusService.ErrorTestInput, NexusService.ErrorTestOutput> testError() {
    return OperationHandler.sync(
        (ctx, details, input) -> {
          switch (input.getAction()) {
            case RAISE_APPLICATION_ERROR:
              throw ApplicationFailure.newNonRetryableFailure(
                  "application error 1", "my-application-error-type");
            case RAISE_CUSTOM_ERROR:
              throw new MyCustomException("Custom error 1");
            case RAISE_CUSTOM_ERROR_WITH_CAUSE_OF_CUSTOM_ERROR:
              // ** THIS DOESN'T WORK **: CHAINED CUSTOM EXCEPTIONS DON'T SERIALIZE
              MyCustomException customError = new MyCustomException("Custom error 1");
              customError.initCause(new MyCustomException("Custom error 2"));
              throw customError;
            case RAISE_APPLICATION_ERROR_WITH_CAUSE_OF_CUSTOM_ERROR:
              throw ApplicationFailure.newNonRetryableFailureWithCause(
                  "application error 1",
                  "my-application-error-type",
                  new MyCustomException("Custom error 2"));
            case RAISE_NEXUS_HANDLER_ERROR:
              throw new HandlerException(HandlerException.ErrorType.NOT_FOUND, "Handler error 1");
            case RAISE_NEXUS_HANDLER_ERROR_WITH_CAUSE_OF_CUSTOM_ERROR:
              // ** THIS DOESN'T WORK **
              // Can't overwrite cause with
              // io.temporal.samples.nexus.handler.NexusServiceImpl$MyCustomException: Custom error
              // 2
              HandlerException handlerErr =
                  new HandlerException(HandlerException.ErrorType.NOT_FOUND, "Handler error 1");
              handlerErr.initCause(new MyCustomException("Custom error 2"));
              throw handlerErr;
            case RAISE_NEXUS_OPERATION_ERROR_WITH_CAUSE_OF_CUSTOM_ERROR:
              throw OperationException.failure(
                  ApplicationFailure.newNonRetryableFailureWithCause(
                      "application error 1",
                      "my-application-error-type",
                      new MyCustomException("Custom error 2")));
          }
          return new NexusService.ErrorTestOutput("Unreachable");
        });
  }
}
