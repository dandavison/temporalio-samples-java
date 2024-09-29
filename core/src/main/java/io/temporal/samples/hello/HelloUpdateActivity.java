/*
 *  Copyright (c) 2020 Temporal Technologies, Inc. All Rights Reserved
 *
 *  Copyright 2012-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *  Modifications copyright (C) 2017 Uber Technologies, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"). You may not
 *  use this file except in compliance with the License. A copy of the License is
 *  located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 *  or in the "license" file accompanying this file. This file is distributed on
 *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 */

package io.temporal.samples.hello;

import io.temporal.activity.Activity;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.ActivityOptions;
import io.temporal.api.enums.v1.WorkflowIdReusePolicy;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.common.RetryOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.workflow.UpdateMethod;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import java.time.Duration;

public class HelloUpdateActivity {

  // Define the task queue name
  static final String TASK_QUEUE = "HelloUpdateTaskQueue";

  // Define the workflow unique id
  static final String WORKFLOW_ID = "HelloUpdateWorkflow";

  @ActivityInterface
  public interface MyActivity {

    @ActivityMethod
    int myActivityMethod();
  }

  @WorkflowInterface
  public interface MyWorkflow {
    @WorkflowMethod
    void run();

    @UpdateMethod
    int callActivity();
  }

  // Define the workflow implementation which implements the getGreetings workflow method.
  public static class MyWorkflowImpl implements MyWorkflow {

    private final MyActivity activity =
        Workflow.newActivityStub(
            MyActivity.class,
            ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofSeconds(2))
                .setRetryOptions(RetryOptions.newBuilder().setMaximumAttempts(1).build())
                .build());

    private boolean done;

    @Override
    public void run() {
      Workflow.await(() -> done);
    }

    @Override
    public int callActivity() {
      int result;
      try {
        result = activity.myActivityMethod();
        done = true;
        return result;
      } catch (Throwable t) {
        throw new RuntimeException("hello");
        // throw new RuntimeException(t.getCause().getMessage());
      }
    }
  }

  static class MyActivityImpl implements MyActivity {
    @Override
    public int myActivityMethod() {
      throw Activity.wrap(new Exception("deliberate error"));
    }
  }

  public static void main(String[] args) throws Exception {

    WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

    WorkflowClient client = WorkflowClient.newInstance(service);

    WorkerFactory factory = WorkerFactory.newInstance(client);
    Worker worker = factory.newWorker(TASK_QUEUE);
    worker.registerWorkflowImplementationTypes(MyWorkflowImpl.class);
    worker.registerActivitiesImplementations(new MyActivityImpl());
    factory.start();

    WorkflowOptions workflowOptions =
        WorkflowOptions.newBuilder()
            .setTaskQueue(TASK_QUEUE)
            .setWorkflowId(WORKFLOW_ID)
            .setWorkflowIdReusePolicy(
                WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_TERMINATE_IF_RUNNING)
            .build();
    MyWorkflow workflow = client.newWorkflowStub(MyWorkflow.class, workflowOptions);

    WorkflowClient.start(workflow::run);

    int result = workflow.callActivity();
    System.out.println(result);
    System.exit(0);
  }
}
