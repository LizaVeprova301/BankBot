package io.project.bankbot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.sql.Date;


@Entity(name = "applicationsDataTable")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicationId;

    private Date applicationDate;
    private String applicationAction;
    private Long applicationOwner;
    private Long applicationInvestor;
    private String applicationStatus;
    private Long applicationAmount;
    private Long applicationAmountProcess;
    private Long applicationAmountTemp;

    public Application() {
        this.applicationDate = new Date(System.currentTimeMillis()); // Устанавливаем текущую дату
        this.applicationAction = "default";
        this.applicationOwner = 0L;
        this.applicationInvestor = 0L;
        this.applicationStatus = "default";
        this.applicationAmount = 0L;
        this.applicationAmountProcess = 0L;
        this.applicationAmountTemp = 0L;

    }

    public Date getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(Date registered) {
        this.applicationDate = registered;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationAction() {
        return applicationAction;
    }

    public void setApplicationAction(String applicationAction) {
        this.applicationAction = applicationAction;
    }

    public Long getApplicationOwner() {
        return applicationOwner;
    }

    public void setApplicationOwner(Long applicationOwner) {
        this.applicationOwner = applicationOwner;
    }

    public Long getApplicationInvestor() {
        return applicationInvestor;
    }

    public void setApplicationInvestor(Long applicationInvestor) {
        this.applicationInvestor = applicationInvestor;
    }

    public String getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(String applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public Long getApplicationAmount() {
        return applicationAmount;
    }

    public void setApplicationAmount(Long applicationAmount) {
        this.applicationAmount = applicationAmount;
    }

    public Long getApplicationAmountProcess() {
        return applicationAmountProcess;
    }

    public void setApplicationAmountProcess(Long applicationAmountProcess) {
        this.applicationAmountProcess = applicationAmountProcess;
    }

    public Long getApplicationAmountTemp() {
        return applicationAmountTemp;
    }

    public void setApplicationAmountTemp(Long applicationAmountTemp) {
        this.applicationAmountTemp = applicationAmountTemp;
    }
}
