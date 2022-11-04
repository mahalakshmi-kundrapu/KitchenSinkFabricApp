package com.kony.adminconsole.commons.dto;

import java.util.List;

public class Header {

    private String name;
    private String value;
    private List<HeaderElement> elements;

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

    public List<HeaderElement> getElements() {
        return elements;
    }

    public void setElements(List<HeaderElement> elements) {
        this.elements = elements;
    }

}
