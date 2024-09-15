package io.temporal.samples.messagepassing.safemessagepassing;

public class DeleteJobUpdateInput {

  private final String jobName;

  public DeleteJobUpdateInput(String jobName) {
    super();
    this.jobName = jobName;
  }

  public String getJobName() {
    return jobName;
  }
}
