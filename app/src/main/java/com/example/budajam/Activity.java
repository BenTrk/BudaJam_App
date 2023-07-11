package com.example.budajam;

import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Activity {
    String qualities;
    HashMap<String, Pair<String, Integer>> qualityPairs = new HashMap<>();

    public Activity(String qualities) {
        this.qualities = qualities;
        String[] separatedString = qualities.split(";");
        int counter = 0;
        String group = "";
        for (String string : separatedString){
            counter++;
            if (counter == 1){
                group = string;
                continue;
            }
            String[] separatedArray = string.split(":");
            Pair<String, Integer> qualitypair = new Pair<>(separatedArray[0], Integer.valueOf(separatedArray[1]));
            qualityPairs.put(group, qualitypair);
        }
    }

    public String getQualities() {
        return qualities;
    }

    public void setQualities(String qualities) {
        this.qualities = qualities;
    }

    public HashMap<String, Pair<String, Integer>> getQualityPairs() {
        return qualityPairs;
    }

    public void setQualityPairs(HashMap<String, Pair<String, Integer>> qualityPairs) {
        this.qualityPairs = qualityPairs;
    }
}
