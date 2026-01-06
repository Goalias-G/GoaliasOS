package com.goalias.common.rateLimiter.spring;


import com.goalias.common.rateLimiter.config.GoaliasProperty;

public class GoaliasConfigHolder {
    private static GoaliasProperty goaliasProperty;

    public static GoaliasProperty getGoaliasProperty() {
        return goaliasProperty;
    }

    public static void setGoaliasProperty(GoaliasProperty goaliasProperty) {
        GoaliasConfigHolder.goaliasProperty = goaliasProperty;
    }
}
