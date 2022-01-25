package com.example.Tribes.Model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Questions entity class, creates Questions in DB
 */
@Entity
public class Questions {
    @Id
    @Getter
    @Setter
    private int id;
    @Getter
    @Setter
    private String question;
    public Questions() {

    }
    public Questions(int id, String question) {
        this.id = id;
        this.question = question;
    }


}
