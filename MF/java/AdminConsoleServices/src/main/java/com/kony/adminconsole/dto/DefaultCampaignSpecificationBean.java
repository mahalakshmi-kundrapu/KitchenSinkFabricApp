package com.kony.adminconsole.dto;

public class DefaultCampaignSpecificationBean {

	private String campaignPlaceholderId;
	private String channel;
	private String screen;
	private String imageIndex;
	private String imageResolution;
	private String imageURL;
	private String destinationURL;

	public String getCampaignPlaceholderId() {
		return campaignPlaceholderId;
	}

	public void setCampaignPlaceholderId(String campaignPlaceholderId) {
		this.campaignPlaceholderId = campaignPlaceholderId;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getScreen() {
		return screen;
	}

	public void setScreen(String screen) {
		this.screen = screen;
	}

	public String getImageIndex() {
		return imageIndex;
	}

	public void setImageIndex(String imageIndex) {
		this.imageIndex = imageIndex;
	}

	public String getImageResolution() {
		return imageResolution;
	}

	public void setImageResolution(String imageResolution) {
		this.imageResolution = imageResolution;
	}

	public String getImageURL() {
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}

	public String getDestinationURL() {
		return destinationURL;
	}

	public void setDestinationURL(String destinationURL) {
		this.destinationURL = destinationURL;
	}
}
