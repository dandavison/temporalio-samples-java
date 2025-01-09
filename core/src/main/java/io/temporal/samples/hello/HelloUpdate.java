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

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.workflow.UpdateMethod;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import java.util.ArrayList;
import java.util.List;

public class HelloUpdate {
  static final String TASK_QUEUE = "HelloUpdateTaskQueue";
  static final String WORKFLOW_ID = "HelloUpdateWorkflow";

  @WorkflowInterface
  public interface MyWorkflow {

    @WorkflowMethod
    void run();

    @UpdateMethod
    int addItem(String item);
  }

  public static class MyWorkflowImpl implements MyWorkflow {
    private final List<String> queue = new ArrayList<>(10);

    @Override
    public int addItem(String item) {
      queue.add(item);
      return queue.size();
    }

    @Override
    public void run() {
      Workflow.await(() -> !queue.isEmpty());
    }
  }

  public static void main(String[] args) throws Exception {
    WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
    WorkflowClient client = WorkflowClient.newInstance(service);
    WorkerFactory factory = WorkerFactory.newInstance(client);
    Worker worker = factory.newWorker(TASK_QUEUE);
    worker.registerWorkflowImplementationTypes(MyWorkflowImpl.class);
    factory.start();
    WorkflowOptions workflowOptions =
        WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).setWorkflowId(WORKFLOW_ID).build();
    MyWorkflow workflow = client.newWorkflowStub(MyWorkflow.class, workflowOptions);
    WorkflowClient.start(workflow::run);
    int queueSize = workflow.addItem("my-item");
    System.out.println(queueSize);
    System.exit(0);
  }
}
