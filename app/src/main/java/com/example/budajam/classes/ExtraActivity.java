package com.example.budajam.classes;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class ExtraActivity {
    String name;
    String group;
    List<Pair<String, Integer>> pointsList;

    public ExtraActivity(String name, String group, List<Pair<String, Integer>> pointsList) {
        this.name = name;
        this.group = group;
        this.pointsList = pointsList;
    }

    public ExtraActivity(String name, String rawDatabaseDataString){
        this.pointsList = new ArrayList<>();
        this.name = name;

        rawDatabaseDataString = rawDatabaseDataString.substring(1, rawDatabaseDataString.length()-1);
        String[] separatedString = rawDatabaseDataString.split(";");
        int counter = 0;
        for (String string : separatedString){
            counter++;
            if (counter == 1){
                String[] separatedGroup = string.split("=");
                this.group = separatedGroup[1];
                continue;
            }
            String[] separatedArray = string.split(":");
            Pair<String, Integer> qualitypair = new Pair<>(separatedArray[0], Integer.valueOf(separatedArray[1]));
            pointsList.add(qualitypair);
        }
    }

    public ExtraActivity() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public List<Pair<String, Integer>> getPointsList() {
        return pointsList;
    }

    public void setPointsList(List<Pair<String, Integer>> pointsList) {
        this.pointsList = pointsList;
    }
}
