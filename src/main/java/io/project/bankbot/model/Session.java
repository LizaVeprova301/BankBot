package io.project.bankbot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity(name = "sessionsDataBase")
public class Session {

    @Id
    private Long sessionChatId;

    private String sessionName;
    private String sessionStage;

    public Session() {
        this.sessionChatId = 0L;
        this.sessionName = "default";
        this.sessionStage = "default";
    }

    public Long getSessionChatId() {
        return sessionChatId;
    }

    public void setSessionChatId(Long sessionChatId) {
        this.sessionChatId = sessionChatId;
    }


    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getSessionStage() {
        return sessionStage;
    }

    public void setSessionStage(String sessionStage) {
        this.sessionStage = sessionStage;
    }

    @Override
    public String toString() {
        return "Session{" +
                "sessionChatId=" + sessionChatId +
                ", sessionName='" + sessionName + '\'' +
                ", sessionStage='" + sessionStage + '\'' +
                '}';
    }

}
