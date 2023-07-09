package com.example.budajam;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataStorage {
    private Map<String, List<Routes>> dataMap;

    public DataStorage() {
        dataMap = new LinkedHashMap<>();
    }

    public void addItem(String category, Routes item) {
        List<Routes> items = dataMap.get(category);
        boolean isThere = false;
        if (items == null) {
            items = new ArrayList<>();
            items.add(item);
            dataMap.put(category, items);
        }
        else {
            for (Routes route : items) {
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

    public List<Routes> getItems(String category) {
        return dataMap.get(category);
    }

    public void removeCategory(String category) {
        dataMap.remove(category);
    }

    public Set<String> getCategory(){
        return dataMap.keySet();
    }
}
