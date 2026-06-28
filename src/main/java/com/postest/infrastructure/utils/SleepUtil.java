package com.postest.infrastructure.utils;

public class SleepUtil {

    private SleepUtil() {
        // Utility class
    }

    public static void sleepIfConfigured(long stepDelay) {
        if (stepDelay > 0) {
            try {
                Thread.sleep(stepDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

