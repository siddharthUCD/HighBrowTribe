package com.example.Tribes.Model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * TribersQuestionsDetails entity class for tribersQuestionDetials table
 */
@Entity
public class TribeQuestionDetails {
    @Id
    @Getter
    @Setter
    private long tribeId;
    @Getter
    @Setter
    private String question;
    @Getter
    @Setter
    private Timestamp challengeStartTime;

    public TribeQuestionDetails(){}

    public TribeQuestionDetails(long tribeId, String question, Timestamp challengeStartTime) {
        this.tribeId = tribeId;
        this.question = question;
        this.challengeStartTime = challengeStartTime;
    }

}
