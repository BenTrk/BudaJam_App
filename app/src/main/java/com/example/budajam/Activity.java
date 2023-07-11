package com.example.budajam;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class Activity {
    String qualities;
    List<Pair<String, Integer>> qualityPairs = new ArrayList<>();

    public Activity(String qualities) {
        this.qualities = qualities;
        String[] separatedString = qualities.split(";");
        for (String string : separatedString){
            String[] separatedArray = string.split(":");
            Pair<String, Integer> qualitypair = new Pair<>(separatedArray[0], Integer.valueOf(separatedArray[1]));
            qualityPairs.add(qualitypair);
        }
    }

    public String getQualities() {
        return qualities;
    }

    public void setQualities(String qualities) {
        this.qualities = qualities;
    }

    public List<Pair<String, Integer>> getQualityPairs() {
        return qualityPairs;
    }

    public void setQualityPairs(List<Pair<String, Integer>> qualityPairs) {
        this.qualityPairs = qualityPairs;
    }
}
