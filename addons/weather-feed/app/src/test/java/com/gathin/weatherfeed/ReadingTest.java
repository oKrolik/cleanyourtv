package com.gathin.weatherfeed;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ReadingTest {
    @Test
    public void readsTheCurrentBlock() {
        String json = "{\"latitude\":41.12,\"current_units\":{\"temperature_2m\":\"°C\"},"
                + "\"current\":{\"time\":\"2026-10-25T20:30\",\"interval\":900,\"temperature_2m\":16.4,"
                + "\"weather_code\":3,\"is_day\":0}}";
        assertArrayEquals(new double[]{16.4, 3, 0}, Reading.parse(json), 0.0);
    }

    @Test
    public void anAnswerWithoutAReadingIsNull() {
        assertNull(Reading.parse("{\"error\":true,\"reason\":\"Latitude must be in range\"}"));
        assertNull(Reading.parse("{\"current\":{}}"));
        assertNull(Reading.parse("not json"));
    }
}
