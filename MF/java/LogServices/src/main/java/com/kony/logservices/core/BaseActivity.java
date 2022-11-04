package com.kony.logservices.core;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.kony.logservices.dto.AdminActivityDTO;
import com.kony.logservices.dto.AdminCustomerActivityDTO;
import com.kony.logservices.dto.AuditActivityDTO;
import com.kony.logservices.dto.CustomerActivityDTO;
import com.kony.logservices.dto.MoneyMovementLogDTO;
import com.kony.logservices.dto.TransactionActivityDTO;

/**
 * 
 * Base class for all types of logs
 * 
 * @see <a href=
 *      "https://github.com/FasterXML/jackson-docs/wiki/JacksonPolymorphicDeserialization">JacksonPolymorphicDeserialization</a>
 * 
 * @author Venkateswara Rao Alla
 *
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "logType")
@JsonSubTypes({ @JsonSubTypes.Type(value = TransactionActivityDTO.class, name = "TransactionActivity"),
        @JsonSubTypes.Type(value = CustomerActivityDTO.class, name = "CustomerActivity"),
        @JsonSubTypes.Type(value = AdminActivityDTO.class, name = "AdminActivity"),
        @JsonSubTypes.Type(value = AdminCustomerActivityDTO.class, name = "AdminCustomerActivity"),
        @JsonSubTypes.Type(value = MoneyMovementLogDTO.class, name = "MoneyMovementLogDTO"),
        @JsonSubTypes.Type(value = AuditActivityDTO.class, name = "AuditActivityDTO") })
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_NULL)
public abstract class BaseActivity implements Serializable {

    private static final long serialVersionUID = -5651395066551481017L;

    private String id;
    private String createdBy;
    private Date createdOn;

    public BaseActivity() {
        this.id = UUID.randomUUID().toString();
        this.createdOn = new Date();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BaseActivity other = (BaseActivity) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
