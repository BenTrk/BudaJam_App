package com.example.budajam.classes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataStorage {
    private Map<String, List<Route>> dataMap;

    public DataStorage() {
        dataMap = new LinkedHashMap<>();
    }

    public void addItem(String category, Route item) {
        List<Route> items = dataMap.get(category);
        boolean isThere = false;
        if (items == null) {
            items = new ArrayList<>();
            items.add(item);
            dataMap.put(category, items);
        }
        else {
            for (Route route : items) {
                if (route.key == item.key) {
                    isThere = true;
                    break;
                }
            }
            if (!isThere){
                items.add(item);
            }
        }
    }

    public List<Route> getItems(String category) {
        return dataMap.get(category);
    }

    public void removeCategory(String category) {
        dataMap.remove(category);
    }

    public Set<String> getCategory(){
        return dataMap.keySet();
    }

    public int size() {
        int size = 0;
        for (String key : dataMap.keySet()){
            for (Route route : dataMap.get(key)){
                size++;
            }
        }
    return size;
    }
}
