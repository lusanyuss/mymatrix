

package com.tencent.matrix.apk.model.result;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tencent.matrix.javalib.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;



public class JobJsonResult extends JobResult {

    private static final String TAG = "JobJsonResult";

    private final File outputFile;
    private int elementCount;

    public JobJsonResult(String format, String outputPath) {
        this.format = format;
        this.outputFile = new File(outputPath + "." + TaskResultFactory.TASK_RESULT_TYPE_JSON);
        this.resultList = new ArrayList<>();
    }

    private void writeJsonArrayStart() throws IOException {
        PrintWriter printWriter = null;
        try {
            if (outputFile.exists() && !outputFile.delete()) {
                Log.e(TAG, "file " + outputFile.getName() + " is already exists and delete it failed!");
                return;
            }
            if (!outputFile.createNewFile()) {
                Log.e(TAG, "create output file " + outputFile.getName() + " failed!");
                return;
            }
            printWriter = new PrintWriter(outputFile, "UTF-8");
            printWriter.append("[");
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
        }
    }

    private void writeJsonElement(JsonElement jsonElement) {
        if (jsonElement != null) {
            try {
                FileWriter writer = null;
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                try {
                    writer = new FileWriter(outputFile, true);
                    if (elementCount > 0) {
                        writer.append(",\n" + gson.toJson(jsonElement));
                    } else {
                        writer.append(gson.toJson(jsonElement));
                    }
                    elementCount++;
                } finally {
                    if (writer != null) {
                        writer.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeJsonArrayEnd() {
        try {
            FileWriter writer = null;
            try {
                writer = new FileWriter(outputFile, true);
                writer.append("]");
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void output() {
        try {
            writeJsonArrayStart();
            if (!resultList.isEmpty()) {
                Collections.sort(resultList, new TaskResultComparator());
                for (TaskResult taskResult : resultList) {
                    if (taskResult.getResult() != null && taskResult.getResult() instanceof JsonObject) {
                        writeJsonElement((JsonObject) taskResult.getResult());
                    }
                }
            }
            writeJsonArrayEnd();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
