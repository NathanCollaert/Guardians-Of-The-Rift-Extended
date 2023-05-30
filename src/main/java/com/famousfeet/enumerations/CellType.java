package com.famousfeet.enumerations;

import com.famousfeet.GuardiansOfTheRiftExtendedConfig;

import java.awt.*;

public enum CellType {
    WEAK,
    MEDIUM,
    STRONG,
    OVERCHARGED;

    public Color getColor(GuardiansOfTheRiftExtendedConfig config) {
        switch (this) {
            case WEAK:
                return config.weakCellOutlineColor();
            case MEDIUM:
                return config.mediumCellOutlineColor();
            case STRONG:
                return config.strongCellOutlineColor();
            case OVERCHARGED:
                return config.overchargedCellOutlineColor();
            default:
                return Color.GREEN;
        }
    }
}
