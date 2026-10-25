package com.gathin.tvhome;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

/**
 * weather_temp: the outside temperature the weather-feed helper app read last. The launcher has
 * no network permission; the helper is the one that goes online, and its provider answers here with its last reading.
 */
final class Weather {
    static final String AUTHORITY = "com.gathin.weatherfeed";

    private Weather() {
    }

    /** "☀️ 17°", or null while the helper is missing or disabled or has no reading yet. */
    static String read(ContentResolver resolver) {
        try (Cursor c = resolver.query(Uri.parse("content://" + AUTHORITY + "/current"), null, null, null, null)) {
            if (c == null || !c.moveToFirst()) return null;
            return text(c.getDouble(c.getColumnIndexOrThrow("temperature")), c.getInt(c.getColumnIndexOrThrow("code")),
                    c.getInt(c.getColumnIndexOrThrow("is_day")) != 0);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** The WMO weather code as one symbol, then the temperature in whole degrees. */
    static String text(double celsius, int code, boolean day) {
        return symbol(code, day) + " " + Math.round(celsius) + "°";
    }

    static String symbol(int code, boolean day) {
        if (code <= 1) return day ? "☀️" : "🌙";   // clear, mainly clear
        if (code == 2) return day ? "⛅" : "☁️";   // partly cloudy
        if (code == 3) return "☁️";
        if (code == 45 || code == 48) return "🌫️";
        if (code >= 71 && code <= 77 || code == 85 || code == 86) return "❄️";
        if (code >= 95) return "⛈️";
        return "🌧️";   // drizzle, rain and showers
    }
}
