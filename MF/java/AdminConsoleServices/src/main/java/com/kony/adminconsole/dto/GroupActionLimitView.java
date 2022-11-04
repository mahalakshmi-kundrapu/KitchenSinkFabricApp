package com.kony.adminconsole.dto;

import java.util.HashMap;
import java.util.Map;

public class GroupActionLimitView {

	private String Group_id;
	private String Action_id;
	private String LimitType_id;
	private String value;
	private String groupactionlimit_id;
	private String Action_name;
	private String Action_description;
	private String Action_Type_id;
	private String Feature_id;
	private String isMFAApplicable;
	private String isAccountLevel;
	private String Feature_name;
	private String Feature_description;
	private String Feature_Type_id;
	private String Feature_Status_id;
	private Map<String,String> limits;
	private boolean isSelected = false;
	
	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public GroupActionLimitView(){
		limits = new HashMap<>();
	}
	
	public Map<String, String> getLimits() {
		return limits;
	}
	public void setLimits(Map<String, String> limits) {
		this.limits = limits;
	}
	public void insertLimit(String limitId, String value) {
		if(limits.containsKey(limitId)) {
			limits.put(limitId, String.valueOf(Math.min(Double.parseDouble(limits.get(limitId)), Double.parseDouble(value))));	
		}else {
			limits.put(limitId, value);
		}
	}
	public String getGroup_id() {
		return Group_id;
	}
	public void setGroup_id(String group_id) {
		Group_id = group_id;
	}
	public String getAction_id() {
		return Action_id;
	}
	public void setAction_id(String action_id) {
		Action_id = action_id;
	}
	public String getLimitType_id() {
		return LimitType_id;
	}
	public void setLimitType_id(String limitType_id) {
		LimitType_id = limitType_id;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getGroupactionlimit_id() {
		return groupactionlimit_id;
	}
	public void setGroupactionlimit_id(String groupactionlimit_id) {
		this.groupactionlimit_id = groupactionlimit_id;
	}
	public String getAction_name() {
		return Action_name;
	}
	public void setAction_name(String action_name) {
		Action_name = action_name;
	}
	public String getAction_description() {
		return Action_description;
	}
	public void setAction_description(String action_description) {
		Action_description = action_description;
	}
	public String getAction_Type_id() {
		return Action_Type_id;
	}
	public void setAction_Type_id(String action_Type_id) {
		Action_Type_id = action_Type_id;
	}
	public String getFeature_id() {
		return Feature_id;
	}
	public void setFeature_id(String feature_id) {
		Feature_id = feature_id;
	}
	public String getIsMFAApplicable() {
		return isMFAApplicable;
	}
	public void setIsMFAApplicable(String isMFAApplicable) {
		this.isMFAApplicable = isMFAApplicable;
	}
	public String getIsAccountLevel() {
		return isAccountLevel;
	}
	public void setIsAccountLevel(String isAccountLevel) {
		this.isAccountLevel = isAccountLevel;
	}
	public String getFeature_name() {
		return Feature_name;
	}
	public void setFeature_name(String feature_name) {
		Feature_name = feature_name;
	}
	public String getFeature_description() {
		return Feature_description;
	}
	public void setFeature_description(String feature_description) {
		Feature_description = feature_description;
	}
	public String getFeature_Type_id() {
		return Feature_Type_id;
	}
	public void setFeature_Type_id(String feature_Type_id) {
		Feature_Type_id = feature_Type_id;
	}
	public String getFeature_Status_id() {
		return Feature_Status_id;
	}
	public void setFeature_Status_id(String feature_Status_id) {
		Feature_Status_id = feature_Status_id;
	}
}
