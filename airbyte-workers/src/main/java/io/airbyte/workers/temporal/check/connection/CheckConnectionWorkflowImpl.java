/*
 * Copyright (c) 2022 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.workers.temporal.check.connection;

import io.airbyte.config.ConnectorJobOutput;
import io.airbyte.config.ConnectorJobOutput.OutputType;
import io.airbyte.config.StandardCheckConnectionInput;
import io.airbyte.config.StandardCheckConnectionOutput;
import io.airbyte.persistence.job.models.IntegrationLauncherConfig;
import io.airbyte.persistence.job.models.JobRunConfig;
import io.airbyte.workers.temporal.annotations.TemporalActivityStub;
import io.airbyte.workers.temporal.check.connection.CheckConnectionActivity.CheckConnectionInput;
import io.temporal.workflow.Workflow;
import javax.inject.Singleton;

@Singleton
public class CheckConnectionWorkflowImpl implements CheckConnectionWorkflow {

  private static final String CHECK_JOB_OUTPUT_TAG = "check_job_output";
  private static final int CHECK_JOB_OUTPUT_TAG_CURRENT_VERSION = 1;

  @TemporalActivityStub(activityOptionsBeanName = "checkActivityOptions")
  private CheckConnectionActivity activity;

  @Override
  public ConnectorJobOutput run(final JobRunConfig jobRunConfig,
                                final IntegrationLauncherConfig launcherConfig,
                                final StandardCheckConnectionInput connectionConfiguration) {

    final CheckConnectionInput checkInput = new CheckConnectionInput(jobRunConfig, launcherConfig, connectionConfiguration);

    final int jobOutputVersion =
        Workflow.getVersion(CHECK_JOB_OUTPUT_TAG, Workflow.DEFAULT_VERSION, CHECK_JOB_OUTPUT_TAG_CURRENT_VERSION);

    if (jobOutputVersion < CHECK_JOB_OUTPUT_TAG_CURRENT_VERSION) {
      final StandardCheckConnectionOutput checkOutput = activity.run(checkInput);
      return new ConnectorJobOutput().withOutputType(OutputType.CHECK_CONNECTION).withCheckConnection(checkOutput);
    }

    return activity.runWithJobOutput(checkInput);
  }

}
