package com.kony.adminconsole.commons.dto;

import java.util.List;

public class HeaderElement {

    String name;
    String value;
    List<NameValuePair> parameters;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<NameValuePair> getParameters() {
        return parameters;
    }

    public void setParameters(List<NameValuePair> parameters) {
        this.parameters = parameters;
    }

    public NameValuePair getParameterByName(String name) {

        for (NameValuePair parameter : parameters) {
            if (parameter.getName().equalsIgnoreCase(name))
                return parameter;
        }
        return null;
    }

    public int getParameterCount() {
        return parameters.size();
    }

    public NameValuePair getParameter(int index) {
        return parameters.get(index);
    }

}
