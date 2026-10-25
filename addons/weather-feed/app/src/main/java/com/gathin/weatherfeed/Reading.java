package com.gathin.weatherfeed;

import org.json.JSONException;
import org.json.JSONObject;

/** Open-Meteo's "current" block. */
final class Reading {
    private Reading() {
    }

    /** {temperature °C, WMO code, is_day}, or null when the answer has no current reading. */
    static double[] parse(String json) {
        try {
            JSONObject current = new JSONObject(json).optJSONObject("current");
            if (current == null || !current.has("temperature_2m")) return null;
            return new double[]{current.getDouble("temperature_2m"), current.optInt("weather_code", 0),
                    current.optInt("is_day", 1)};
        } catch (JSONException e) {
            return null;
        }
    }
}
