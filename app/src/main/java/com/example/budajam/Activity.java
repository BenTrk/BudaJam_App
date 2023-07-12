package com.example.budajam;

import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Activity {
    String qualities;
    List<HashMap<String, Pair<String, Integer>>> qualityPairs = new ArrayList<>();

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
            HashMap<String, Pair<String, Integer>> qualityMap = new HashMap<>();
            qualityMap.put(group, qualitypair);
            qualityPairs.add(qualityMap);
        }
    }

    public String getQualities() {
        return qualities;
    }

    public void setQualities(String qualities) {
        this.qualities = qualities;
    }

    public List<HashMap<String, Pair<String, Integer>>> getQualityPairs() {
        return qualityPairs;
    }

    public void setQualityPairs(List<HashMap<String, Pair<String, Integer>>> qualityPairs) {
        this.qualityPairs = qualityPairs;
    }
}
