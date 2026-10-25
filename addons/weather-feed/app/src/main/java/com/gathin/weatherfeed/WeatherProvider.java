package com.gathin.weatherfeed;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The last temperature read from Open-Meteo for the place in options.xml, as one row: temperature (°C), code (WMO
 * weather code), is_day (1 or 0), time (when it was read, ms). Each query answers at once from the stored reading
 * and, when that is older than refresh_minutes, fetches a new one in the background for the next query. There is no
 * service and no timer: nothing runs unless the launcher asks, which it does once a minute while Home is on screen.
 */
public class WeatherProvider extends ContentProvider {
    static final String[] COLUMNS = {"temperature", "code", "is_day", "time"};
    private static final long RETRY_MS = 5 * 60_000L;

    private final AtomicBoolean fetching = new AtomicBoolean();
    private long lastAttempt;

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] args, String sort) {
        Context context = getContext();
        SharedPreferences prefs = context.getSharedPreferences("reading", Context.MODE_PRIVATE);
        long time = prefs.getLong("time", 0);
        long now = System.currentTimeMillis();
        long maxAge = context.getResources().getInteger(R.integer.refresh_minutes) * 60_000L;
        // A failed fetch waits a few minutes before the next try instead of one every minute.
        if (now - time > maxAge && now - lastAttempt > RETRY_MS && fetching.compareAndSet(false, true)) {
            lastAttempt = now;
            new Thread(() -> {
                try {
                    fetch(context, prefs);
                } finally {
                    fetching.set(false);
                }
            }).start();
        }
        MatrixCursor cursor = new MatrixCursor(COLUMNS, 1);
        if (time > 0) {
            cursor.addRow(new Object[]{(double) prefs.getFloat("temperature", 0f), prefs.getInt("code", 0),
                    prefs.getInt("is_day", 1), time});
        }
        return cursor;
    }

    private static void fetch(Context context, SharedPreferences prefs) {
        String url = "https://api.open-meteo.com/v1/forecast?latitude=" + context.getString(R.string.latitude)
                + "&longitude=" + context.getString(R.string.longitude)
                + "&current=temperature_2m,weather_code,is_day&timezone=auto";
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(10_000);
            if (connection.getResponseCode() != 200) return;
            double[] reading = Reading.parse(read(connection.getInputStream()));
            if (reading == null) return;
            prefs.edit().putFloat("temperature", (float) reading[0]).putInt("code", (int) reading[1])
                    .putInt("is_day", (int) reading[2]).putLong("time", System.currentTimeMillis()).apply();
        } catch (IOException | RuntimeException e) {
            // Offline or a bad answer: keep the last reading; the next query after RETRY_MS tries again.
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static String read(InputStream in) throws IOException {
        try (InputStream stream = in; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            for (int n; (n = stream.read(buffer)) > 0; ) out.write(buffer, 0, n);
            return out.toString(StandardCharsets.UTF_8.name());
        }
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("read-only");
    }

    @Override
    public int delete(Uri uri, String selection, String[] args) {
        throw new UnsupportedOperationException("read-only");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] args) {
        throw new UnsupportedOperationException("read-only");
    }
}
