package com.example.mtheodor.sunshine.app;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherDataParser {
    public static double getMaxTemperatureForDay(String weatherJsonStr, int dayIndex) throws JSONException {
        JSONObject json = new JSONObject(weatherJsonStr);
        JSONArray days = json.getJSONArray("list");

        return days.getJSONObject(dayIndex).getJSONObject("temp").getDouble("max");
    }
}
