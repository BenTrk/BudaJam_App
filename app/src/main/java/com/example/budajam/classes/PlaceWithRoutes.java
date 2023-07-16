package com.example.budajam.classes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class PlaceWithRoutes {
    String placeName;
    List<Route> routeList;

    public PlaceWithRoutes() {
    }

    public PlaceWithRoutes(String placeName, List<Route> routeList) {
        this.placeName = placeName;
        this.routeList = routeList;
    }

    public PlaceWithRoutes(String placeName, String routeListJSONArrayString) throws JsonProcessingException {
        this.placeName = placeName;
        this.routeList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonArray = objectMapper.readTree(routeListJSONArrayString);
        for (JsonNode element : jsonArray) {
            Route route = objectMapper.treeToValue(element, Route.class);
            routeList.add(route);
        }
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public List<Route> getRouteList() {
        return routeList;
    }

    public void setRouteList(List<Route> routeList) {
        this.routeList = routeList;
    }
}
