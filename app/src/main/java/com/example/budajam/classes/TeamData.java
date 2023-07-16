package com.example.budajam.classes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TeamData {
    public String TeamName;
    public String ClimberOne;
    public String ClimberTwo;
    public double teamPoints;
    public boolean Paid;
    public String Category;
    HashMap<String, List<PlaceWithRoutes>> climbersClimbsMap;

    public TeamData() {
    }

    public TeamData(String teamName, String climberOne, String climberTwo, double teamPoints, boolean paid, String category,
                    HashMap<String, List<PlaceWithRoutes>> climbersClimbsMap) {
        this.TeamName = teamName;
        this.ClimberOne = climberOne;
        this.ClimberTwo = climberTwo;
        this.teamPoints = teamPoints;
        this.Paid = paid;
        this.Category = category;
        this.climbersClimbsMap = climbersClimbsMap;
    }

    public TeamData(String teamName, String climberOne, String climberTwo, double teamPoints, boolean paid, String category) {
        this.TeamName = teamName;
        this.ClimberOne = climberOne;
        this.ClimberTwo = climberTwo;
        this.teamPoints = teamPoints;
        this.Paid = paid;
        this.Category = category;
        this.climbersClimbsMap = new HashMap<>();
    }

    public String getTeamName() {
        return TeamName;
    }

    public void setTeamName(String teamName) {
        TeamName = teamName;
    }

    public String getClimberOne() {
        return ClimberOne;
    }

    public void setClimberOne(String climberOne) {
        ClimberOne = climberOne;
    }

    public String getClimberTwo() {
        return ClimberTwo;
    }

    public void setClimberTwo(String climberTwo) {
        ClimberTwo = climberTwo;
    }

    public double getTeamPoints() {
        return teamPoints;
    }

    public void setTeamPoints(double teamPoints) {
        this.teamPoints = teamPoints;
    }

    public boolean isPaid() {
        return Paid;
    }

    public void setPaid(boolean paid) {
        Paid = paid;
    }

    public String getCategory() {
        return Category;
    }

    public void setCategory(String category) {
        Category = category;
    }

    public HashMap<String, List<PlaceWithRoutes>> getClimbersClimbsMap() {
        return climbersClimbsMap;
    }

    public void setClimbersClimbsMap(HashMap<String, List<PlaceWithRoutes>> climbersClimbsMap) {
        this.climbersClimbsMap = climbersClimbsMap;
    }
}
