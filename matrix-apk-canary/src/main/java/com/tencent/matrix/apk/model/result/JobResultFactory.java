

package com.tencent.matrix.apk.model.result;

import com.tencent.matrix.apk.model.job.JobConfig;



public final class JobResultFactory {

    public static JobResult factory(String format, JobConfig config) {

        JobResult jobResult = null;
        if (config != null) {
            if (TaskResultFactory.isJsonResult(format)) {
                jobResult = new JobJsonResult(format, config.getOutputPath());
            } else if (TaskResultFactory.isHtmlResult(format)) {
                jobResult = new JobHtmlResult(format, config.getOutputPath());
            }
        }
        return jobResult;
    }
}
