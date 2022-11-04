package com.kony.logservices.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class TransactionValueVolumeTypeDTO implements Serializable {

    private static final long serialVersionUID = -2632711015058528381L;

    private Long volume;
    private BigDecimal value;
    private String type;
    private String channel;
    private String serviceName;
    private Date transactionDate;

    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

}
