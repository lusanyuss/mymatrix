

package com.tencent.matrix.apk.model.result;


import com.tencent.matrix.apk.model.task.TaskFactory;

import java.util.Comparator;



public class TaskResultComparator implements Comparator<TaskResult> {


    private static final int TASK_IMPORT_LEVEL_1 = 1;
    private static final int TASK_IMPORT_LEVEL_2 = 2;
    private static final int TASK_IMPORT_LEVEL_3 = 3;
    private static final int TASK_IMPORT_LEVEL_LOWEST = 0;

    @Override
    public int compare(TaskResult taskResult1, TaskResult taskResult2) {
        return getImportLevel(taskResult1.taskType) - getImportLevel(taskResult2.taskType);
    }

    public static int getImportLevel(int taskType) {
        int level = TASK_IMPORT_LEVEL_LOWEST;
        switch (taskType) {
            case TaskFactory.TASK_TYPE_UNZIP:
            case TaskFactory.TASK_TYPE_MANIFEST:
                level = TASK_IMPORT_LEVEL_1;
                break;
            case TaskFactory.TASK_TYPE_CHECK_RESGUARD:
            case TaskFactory.TASK_TYPE_DUPLICATE_FILE:
            case TaskFactory.TASK_TYPE_FIND_NON_ALPHA_PNG:
            case TaskFactory.TASK_TYPE_UNSTRIPPED_SO:
            case TaskFactory.TASK_TYPE_UNUSED_ASSETS:
            case TaskFactory.TASK_TYPE_UNUSED_RESOURCES:
            case TaskFactory.TASK_TYPE_UNCOMPRESSED_FILE:
                level = TASK_IMPORT_LEVEL_2;
                break;
            case TaskFactory.TASK_TYPE_CHECK_MULTILIB:
            case TaskFactory.TASK_TYPE_CHECK_MULTISTL:
            case TaskFactory.TASK_TYPE_COUNT_METHOD:
            case TaskFactory.TASK_TYPE_COUNT_R_CLASS:
            case TaskFactory.TASK_TYPE_SHOW_FILE_SIZE:
                level = TASK_IMPORT_LEVEL_3;
                break;
            default:
                break;
        }
        return level;
    }
}
