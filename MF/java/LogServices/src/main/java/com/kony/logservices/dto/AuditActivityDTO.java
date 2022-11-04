package com.kony.logservices.dto;

import java.util.Date;

import com.kony.logservices.core.BaseActivity;

public class AuditActivityDTO extends BaseActivity {

    private static final long serialVersionUID = 1009876783167367247L;

    private String EventId;
    private String EventType;
    private String EventSubType;
    private String Status_Id;
    private String AppId;
    private String UserName;
    private String Customer_Id;
    private String AdminUserName;
    private String AdminUserRole;
    private String Producer;
    private String MoneyMovementRefId;
    private String createdby;
    private String EventData;
    private boolean softdeleteflag = false;
    private java.util.Date createdts;
    private String sessionId;
    private String nonSearchable;
    private Date eventts;

    private String ipAddress;
    private String platform;
    private String appVersion;
    private String channel;
    private String deviceId;
    private String browser;
    private String operatingSystem;
    private String deviceModel;

    private String phoneNumber;
    private String email;

    private String mfa_State;
    private String mfa_ServiceKey;
    private String mfa_Type;
    private String creditcardnumber;

    public AuditActivityDTO() {
        this.createdts = new java.util.Date();

    }

    public Date geteventts() {
        return eventts;
    }

    public void seteventts(Date eventts) {
        this.eventts = eventts;
    }

    public void setmfa_State(String mfa_State) {
        this.mfa_State = mfa_State;
    }

    public String getmfa_State() {
        return mfa_State;
    }

    public void setmfa_Type(String mfa_Type) {
        this.mfa_Type = mfa_Type;
    }

    public String getmfa_Type() {
        return mfa_Type;
    }

    public void setmfa_ServiceKey(String mfa_ServiceKey) {
        this.mfa_ServiceKey = mfa_ServiceKey;
    }

    public String getmfa_ServiceKey() {
        return mfa_ServiceKey;
    }

    public void setphoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getphoneNumber() {
        return phoneNumber;
    }

    public void setemail(String email) {
        this.email = email;
    }

    public String getemail() {
        return email;
    }

    public Date getcreatedts() {
        return createdts;
    }

    public String getsessionId() {
        return sessionId;
    }

    public void setnonSearchable(String nonSearchable) {
        this.nonSearchable = nonSearchable;
    }

    public String getnonSearchable() {
        return nonSearchable;
    }

    public void setsessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setcreatedts(Date createdts) {
        this.createdts = createdts;
    }

    public boolean getsoftdeleteflag() {
        return softdeleteflag;
    }

    public void setsoftdeleteflag(boolean softdeleteflag) {
        this.softdeleteflag = softdeleteflag;
    }

    public String getEventdata() {
        return EventData;
    }

    public void setEventdata(String EventData) {
        this.EventData = EventData;
    }

    public String getEventtype() {
        return EventType;
    }

    public String getEventid() {
        return EventId;
    }

    public void setEventtype(String EventType) {
        this.EventType = EventType;
    }

    public void setEventid(String EventId) {
        this.EventId = EventId;
    }

    public String getEventsubtype() {
        return EventSubType;
    }

    public void setEventsubtype(String EventSubType) {
        this.EventSubType = EventSubType;
    }

    public String getStatus_id() {
        return Status_Id;
    }

    public void setStatus_id(String Status_Id) {
        this.Status_Id = Status_Id;
    }

    public String getAppid() {
        return AppId;
    }

    public void setAppid(String AppId) {
        this.AppId = AppId;
    }

    public String getUsername() {
        return UserName;
    }

    public void setUsername(String UserName) {
        this.UserName = UserName;
    }

    public String getCustomer_id() {
        return Customer_Id;
    }

    public void setCustomer_id(String Customer_Id) {
        this.Customer_Id = Customer_Id;
    }

    public String getAdminusername() {
        return AdminUserName;
    }

    public void setAdminusername(String AdminUserName) {
        this.AdminUserName = AdminUserName;
    }

    public String getAdminuserrole() {
        return AdminUserRole;
    }

    public void setAdminuserrole(String AdminUserRole) {
        this.AdminUserRole = AdminUserRole;
    }

    public String getProducer() {
        return Producer;
    }

    public void setProducer(String Producer) {
        this.Producer = Producer;
    }

    public String getMoneymovementrefid() {
        return MoneyMovementRefId;
    }

    public void setMoneymovementrefid(String MoneyMovementRefId) {
        this.MoneyMovementRefId = MoneyMovementRefId;
    }

    public String getCreatedby() {
        return createdby;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby;
    }

    public String getdeviceModel() {
        return deviceModel;
    }

    public void setdeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public void setoperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getoperatingSystem() {
        return operatingSystem;
    }

    public void setbrowser(String browser) {
        this.browser = browser;
    }

    public String getbrowser() {
        return browser;
    }

    public void setdeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getdeviceId() {
        return deviceId;
    }

    public void setchannel(String channel) {
        this.channel = channel;
    }

    public String getchannel() {
        return channel;
    }

    public void setappVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getappVersion() {
        return appVersion;
    }

    public void setplatform(String platform) {
        this.platform = platform;
    }

    public String getplatform() {
        return platform;
    }

    public void setipAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getipAddress() {
        return ipAddress;
    }

    public void setcreditcardnumber(String creditcardnumber) {
        this.creditcardnumber = creditcardnumber;
    }

    public String getcreditcardnumber() {
        return creditcardnumber;
    }

}
