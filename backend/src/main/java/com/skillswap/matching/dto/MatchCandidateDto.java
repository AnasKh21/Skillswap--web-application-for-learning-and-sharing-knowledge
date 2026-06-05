package com.skillswap.matching.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MatchCandidateDto {

    private UUID userId;
    private String displayName;
    private String bio;
    private String avatarUrl;
    private Double averageRating;
    private long theyTeachMe;
    private long iTeachThem;
    private double score;
    private List<String> teachSkills;
    private List<String> learnSkills;

    public MatchCandidateDto(UUID userId, String displayName, String bio, String avatarUrl,
                             Double averageRating, long theyTeachMe, long iTeachThem, double score) {
        this.userId       = userId;
        this.displayName  = displayName;
        this.bio          = bio;
        this.avatarUrl    = avatarUrl;
        this.averageRating = averageRating;
        this.theyTeachMe  = theyTeachMe;
        this.iTeachThem   = iTeachThem;
        this.score        = score;
        this.teachSkills  = new ArrayList<>();
        this.learnSkills  = new ArrayList<>();
    }

    public UUID getUserId()          { return userId; }
    public String getDisplayName()   { return displayName; }
    public String getBio()           { return bio; }
    public String getAvatarUrl()     { return avatarUrl; }
    public Double getAverageRating() { return averageRating; }
    public long getTheyTeachMe()     { return theyTeachMe; }
    @JsonProperty("iTeachThem")
    public long getITeachThem()      { return iTeachThem; }
    public double getScore()         { return score; }
    public List<String> getTeachSkills() { return teachSkills; }
    public List<String> getLearnSkills() { return learnSkills; }

    public void setUserId(UUID userId)               { this.userId = userId; }
    public void setDisplayName(String displayName)   { this.displayName = displayName; }
    public void setBio(String bio)                   { this.bio = bio; }
    public void setAvatarUrl(String avatarUrl)       { this.avatarUrl = avatarUrl; }
    public void setAverageRating(Double r)           { this.averageRating = r; }
    public void setTheyTeachMe(long v)               { this.theyTeachMe = v; }
    public void setITeachThem(long v)                { this.iTeachThem = v; }
    public void setScore(double score)               { this.score = score; }
    public void setTeachSkills(List<String> skills)  { this.teachSkills = skills; }
    public void setLearnSkills(List<String> skills)  { this.learnSkills = skills; }
}
