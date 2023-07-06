package com.example.budajam;

public class ClimberNames {
    public String TeamName;
    public String ClimberOne;
    public String ClimberTwo;
    public double teamPoints;
    public boolean Paid;

    public ClimberNames(){}

    public ClimberNames(String TeamName, String ClimberOne, String ClimberTwo, double teamPoints, boolean paid){
        this.TeamName = TeamName;
        this.ClimberOne = ClimberOne;
        this.ClimberTwo = ClimberTwo;
        this.teamPoints = teamPoints;
        this.Paid = paid;
    }

}
