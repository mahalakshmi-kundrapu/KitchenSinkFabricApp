/**
 * 
 */
package com.kony.adminconsole.dto;

/**
 * @author Sowmya Mortha
 *
 */
public class ExportColumnDetailsBean implements Comparable<ExportColumnDetailsBean> {

    Integer order;
    // empty displayName if concat of two fields is required
    String displayName;
    String paramName;
    String type;

    public ExportColumnDetailsBean(Integer order, String displayName, String paramName) {
        this.order = order;
        this.displayName = displayName;
        this.paramName = paramName;
        this.type = "string";
    }

    public ExportColumnDetailsBean(Integer order, String displayName, String paramName, String type) {
        this.order = order;
        this.displayName = displayName;
        this.paramName = paramName;
        this.type = type;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int compareTo(ExportColumnDetailsBean elem) {
        return (this.order - elem.order);
    }

    @Override
    public String toString() {
        return "ExportColumnDetailsBean [order=" + order + ", displayName=" + displayName + ", paramName=" + paramName
                + ", type=" + type + "]";
    }

}
