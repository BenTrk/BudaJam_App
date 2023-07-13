package com.example.budajam.classes;

public class Routes {
    public String name;
    public long difficulty;
    public long length;
    public String diffchanger;
    public int key;
    public double points;
    public String climbStyle;
    public String best;

    public Routes(){}

    public Routes(String name, long difficulty, long length, String diffchanger, int key, double points, String climbStyle, String best) {
        this.name = name;
        this.difficulty = difficulty;
        this.length = length;
        this.diffchanger = diffchanger;
        this.key = key;
        this.points = points;
        this.climbStyle = climbStyle;
        this.best = best;
    }
}
