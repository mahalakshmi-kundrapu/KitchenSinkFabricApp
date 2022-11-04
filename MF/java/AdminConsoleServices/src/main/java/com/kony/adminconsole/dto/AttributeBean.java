package com.kony.adminconsole.dto;

public class AttributeBean {

    private String id;
    private String endpointAttributeId;
    private String name;
    private String type;
    private String options;
    private String range;
    private String criterias;
    private String helpText;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEndpointAttributeId() {
        return endpointAttributeId;
    }

    public void setEndpointAttributeId(String endpointAttributeId) {
        this.endpointAttributeId = endpointAttributeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getCriterias() {
        return criterias;
    }

    public void setCriterias(String criterias) {
        this.criterias = criterias;
    }

    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }
}
