

package com.tencent.matrix.apk.model.result;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;



public class TaskHtmlResult extends TaskResult {

    private static final String TAG = "Matrix.TaskHtmlResult";

    protected final Document document;

    protected JsonObject config;

    public TaskHtmlResult(int taskType, JsonObject config) throws ParserConfigurationException {
        super(taskType);
        this.config = config;
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        document = documentBuilder.newDocument();
    }

    private void add(Element root) {
        document.appendChild(root);
    }

    public void format(JsonObject jsonObject) throws ParserConfigurationException {
        add(toElement(document, jsonObject));
    }


    private Element toElement(Document document, JsonElement jsonElement) throws ParserConfigurationException {

        if (jsonElement == null) {
            return null;
        }

        if (jsonElement.isJsonPrimitive()) {
            Element value = document.createElement("span");
            value.setTextContent(jsonElement.getAsString());
            return value;
        } else if (jsonElement.isJsonObject()) {
            Element table = document.createElement("table");
            table.setAttribute("border", "1");
            table.setAttribute("width", "100%");
            for (Map.Entry<String, JsonElement> entry : ((JsonObject) jsonElement).entrySet()) {
                JsonElement jsonValue = entry.getValue();
                if (jsonValue.isJsonPrimitive()) {
                    Element tr = document.createElement("tr");
                    Element name = document.createElement("td");
                    name.setAttribute("valign", "top");
                    name.setAttribute("width", "30%");
                    name.setTextContent(entry.getKey());
                    Element value = document.createElement("td");
                    value.setTextContent(jsonValue.getAsString());
                    tr.appendChild(name);
                    tr.appendChild(value);
                    table.appendChild(tr);
                } else if (jsonValue.isJsonObject()) {
                    Element tr = document.createElement("tr");
                    Element name = document.createElement("td");
                    name.setAttribute("valign", "top");
                    name.setAttribute("width", "30%");
                    name.setTextContent(entry.getKey());
                    Element value = document.createElement("td");
                    value.appendChild(toElement(document, jsonValue));
                    tr.appendChild(name);
                    tr.appendChild(value);
                    table.appendChild(tr);
                } else if (jsonValue.isJsonArray()) {
                    Element tr = document.createElement("tr");
                    Element name = document.createElement("td");
                    name.setAttribute("valign", "top");
                    name.setAttribute("width", "30%");
                    name.setTextContent(entry.getKey());
                    Element value = document.createElement("td");
                    JsonArray array = (JsonArray) jsonValue;
                    Element ul = document.createElement("ul");
                    ul.setAttribute("style", "list-style-type:none");
                    value.appendChild(ul);
                    for (int i = 0; i < array.size(); i++) {
                        Element li = document.createElement("li");
                        li.appendChild(toElement(document, array.get(i)));
                        ul.appendChild(li);
                    }
                    tr.appendChild(name);
                    tr.appendChild(value);
                    table.appendChild(tr);
                }
            }
            return table;
        }
        return null;
    }

    @Override
    public String toString() {
        return document.toString();
    }

    @Override
    public Document getResult() {
        return document;
    }
}
