package com.zyeeda.framework.entities.base;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlTransient;

@javax.persistence.MappedSuperclass
public class FlowDomainEntity extends RevisionDomainEntity {

    private static final long serialVersionUID = 5589756241843587540L;
    
    private Long processInstanceId;
    private Integer sessionId;
    private String version;
    private String flowState;
    private Date flowStateTime;
    private String state = "active";


    @Basic
    @Column(name = "F_FLOW_STATE", length = 32, nullable = false)
    public String getFlowState() {
        return flowState;
    }
    public void setFlowState(String flowState) {
        this.flowState = flowState;
    }

    @Basic
    @Column(name = "F_SESSION_ID", nullable = false)
    @XmlTransient
    public Integer getSessionId() {
        return sessionId;
    }
    public void setSessionId(Integer sessionId) {
        this.sessionId = sessionId;
    }

    @Basic
    @Column(name = "F_PROCESS_INSTANCE_ID", nullable = false)
    @XmlTransient
    public Long getProcessInstanceId() {
        return processInstanceId;
    }
    public void setProcessInstanceId(Long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    @Temporal(TemporalType.TIMESTAMP) 
    @Column(name = "F_FLOW_STATE_TIME", length = 21)
    public java.util.Date getFlowStateTime() {
        return flowStateTime;
    }
    public void setFlowStateTime(Date flowStateTime) {
        this.flowStateTime = flowStateTime;
    }

    @Basic
    @Column(name = "F_STATE", length = 32, nullable = false)
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }

    @Basic
    @Column(name = "F_VERSION", length = 32, nullable = false)
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }

}
