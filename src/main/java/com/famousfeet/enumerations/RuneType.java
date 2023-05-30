package com.famousfeet.enumerations;

import com.famousfeet.GuardiansOfTheRiftExtendedConfig;

import java.awt.*;

public enum RuneType {
    ELEMENTAL,
    CATALYTIC;

    public Color getColor(GuardiansOfTheRiftExtendedConfig config) {
        switch (this) {
            case ELEMENTAL:
                return config.elementalRuneOutlineColor();
            case CATALYTIC:
                return config.catalyticRuneOutlineColor();
            default:
                return Color.GREEN;
        }
    }
}
