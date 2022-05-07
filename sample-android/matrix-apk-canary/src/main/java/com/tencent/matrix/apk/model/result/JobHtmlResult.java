

package com.tencent.matrix.apk.model.result;

import com.tencent.matrix.javalib.util.Log;

import org.w3c.dom.Document;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;



public class JobHtmlResult extends JobResult {

    private static final String TAG = "JobHtmlResult";

    private final File outputFile;

    public JobHtmlResult(String format, String outputPath) {
        this.format = format;
        outputFile = new File(outputPath + "." + TaskResultFactory.TASK_RESULT_TYPE_HTML);
        this.resultList = new ArrayList<>();
    }

    private void writeHtmlStart() throws IOException {
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
            printWriter.append("<html>");
            printWriter.append("<body>");
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
        }
    }

    private void writeDocument(DOMSource domSource) throws Exception {
        Transformer transformer = null;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
            if (outputFile.isFile() && outputFile.exists()) {
                FileWriter writer = new FileWriter(outputFile, true);
                writer.append("<br/>");
                StreamResult result = new StreamResult(outputFile);
                result.setWriter(writer);
                transformer.transform(domSource, result);
            }
        } catch (TransformerException | IOException e) {
            throw e;
        }
    }

    private void writeHtmlEnd() throws IOException {
        try {
            FileWriter writer = null;
            try {
                writer = new FileWriter(outputFile, true);
                writer.append("</body>");
                writer.append("</html>");
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
            writeHtmlStart();
            if (!resultList.isEmpty()) {
                Collections.sort(resultList, new TaskResultComparator());
                for (TaskResult taskResult : resultList) {
                    if (taskResult.getResult() != null && taskResult.getResult() instanceof Document) {
                        writeDocument(new DOMSource((Document) taskResult.getResult()));
                    }
                }
            }
            writeHtmlEnd();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
