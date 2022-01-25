package com.example.Tribes.Model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * User entity class for user table
 */
@Entity
public class User {

    @Id
    private Long uniqueId;
    private String name;
    private Long tribeId;
    private String programmingLanguage;
    private String gitHubId;
    private String tribeLanguage;
    private int portNumber;


    public User(Long uniqueId,String name, Long tribeId, String programmingLanguage,String gitHubId,String tribeLanguage,int portNumber) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.tribeId = tribeId;
        this.programmingLanguage = programmingLanguage;
        this.gitHubId = gitHubId;
        this.tribeLanguage = tribeLanguage;
        this.portNumber = portNumber;
    }

    public User(){}

    public Long getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(Long uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getTribeId() {
        return tribeId;
    }

    public void setTribeId(Long tribeId) {
        this.tribeId = tribeId;
    }

    public String getProgrammingLanguage() {
        return programmingLanguage;
    }

    public void setProgrammingLanguage(String programmingLanguage) {
        this.programmingLanguage = programmingLanguage;
    }

    public String getGitHubId() {
        return gitHubId;
    }

    public void setGitHubId(String gitHubId) {
        this.gitHubId = gitHubId;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    public String getTribeLanguage() {
        return tribeLanguage;
    }

    public void setTribeLanguage(String tribeLanguage) {
        this.tribeLanguage = tribeLanguage;
    }

}