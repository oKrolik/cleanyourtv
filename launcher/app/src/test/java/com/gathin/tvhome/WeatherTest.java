package com.gathin.tvhome;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class WeatherTest {
    @Test
    public void temperatureIsRoundedToWholeDegrees() {
        assertEquals("☀️ 17°", Weather.text(16.6, 0, true));
        assertEquals("☀️ 0°", Weather.text(-0.4, 0, true));
        assertEquals("☀️ -3°", Weather.text(-2.6, 0, true));
    }

    @Test
    public void clearNightsShowTheMoon() {
        assertEquals("🌙", Weather.symbol(0, false));
        assertEquals("🌙", Weather.symbol(1, false));
    }

    @Test
    public void codesMapToTheirKind() {
        assertEquals("☁️", Weather.symbol(3, true));
        assertEquals("🌫️", Weather.symbol(45, true));
        assertEquals("🌧️", Weather.symbol(61, true));
        assertEquals("🌧️", Weather.symbol(81, true));
        assertEquals("❄️", Weather.symbol(73, true));
        assertEquals("❄️", Weather.symbol(86, true));
        assertEquals("⛈️", Weather.symbol(95, true));
    }
}
