package com.famousfeet;

import com.famousfeet.enumerations.ColorActiveGuardianPortalsConfigOptions;
import com.famousfeet.enumerations.LimitOutlineConfigOptions;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.*;

@ConfigGroup("example")
public interface GuardiansOfTheRiftExtendedConfig extends Config {
    @ConfigSection(
            name = "Outlines",
            description = "All options relating to colored outlines",
            position = 0
    )
    String outlines = "outlines";

    @ConfigSection(
            name = "Overlays",
            description = "All options relating to the GOTR extended overlay",
            position = 1
    )
    String overlays = "overlays";

    @ConfigSection(
            name = "Panel",
            description = "All options relating to the GOTR extended panel",
            position = 2
    )
    String panel = "panel";

    @ConfigSection(
            name = "Color",
            description = "All options relating to the GOTR extended outline/overlay coloring",
            position = 3,
            closedByDefault = true
    )
    String colors = "colors";

    @ConfigItem(
            keyName = "notifyGameStart",
            name = "Notify on game start",
            description = "Notifies you when a GOTR game starts."
    )
    default boolean notifyGameStart() {
        return false;
    }

    @ConfigItem(
            keyName = "portalSpawn",
            name = "Notify on portal spawn",
            description = "Notifies you when a Huge Guardian Remains Portal spawns."
    )
    default boolean notifyPortalSpawn() {
        return false;
    }

    @ConfigItem(
            keyName = "quickPassCooldown",
            name = "Add Quick-Pass barrier cooldown",
            description = "Adds a 3 tick delay to the Quick-Pass menu option so you don't enter/leave by spam clicking the gate with Menu Entry Swapper's quick-pass option enabled."
    )
    default boolean quickPassCooldown() {
        return true;
    }

    @ConfigItem(
            keyName = "muteApprentices",
            name = "Mute Apprentices",
            description = "Mutes the over head messages of the Apprentices giving game advice."
    )
    default boolean muteApprentices() {
        return true;
    }

    @ConfigItem(
            keyName = "outlineDepositPool",
            name = "Outline Deposit Pool",
            description = "Outline the Deposit Pool when you have Runes in your inventory.",
            position = 1,
            section = outlines
    )
    default boolean outlineDepositPool() {
        return false;
    }

    @ConfigItem(
            keyName = "outlineGreatGuardian",
            name = "Outline Great Guardian",
            description = "Outline the Great Guardian when you have any Guardian Stones in your inventory.",
            position = 2,
            section = outlines
    )
    default boolean outlineGreatGuardian() {
        return true;
    }

    @ConfigItem(
            keyName = "outlineActiveGuardianPortals",
            name = "Outline Active Guardian Portals",
            description = "Outline the Active Guardian Portals in the GOTR minigame.",
            position = 3,
            section = outlines
    )
    default boolean outlineActiveGuardianPortals() {
        return true;
    }

    @ConfigItem(
            keyName = "outlineUnchargedCellsTable",
            name = "Outline Uncharged Cells Table",
            description = "Outline the Uncharged Cells Table when the amount of Uncharged Cells in your inventory is less than or equal to the given amount below.",
            position = 4,
            section = outlines
    )
    default boolean outlineUnchargedCellsTable() {
        return true;
    }

    @ConfigItem(
            keyName = "unchargedCellsOutlineLimit",
            name = "Uncharged Cells limit",
            description = "The limit of Uncharged Cells in your inventory before the Uncharged Cell Table will be outlined.",
            position = 5,
            section = outlines
    )
    default int unchargedCellsOutlineLimit() {
        return 3;
    }

    @ConfigItem(
            keyName = "levelOverride",
            name = "Outline only sufficient level",
            description = "Limited the active outlined guardian portals to the ones which you have the sufficient level for.",
            position = 6,
            section = outlines
    )
    default boolean levelOverride() {
        return true;
    }

    @ConfigItem(
            keyName = "limitOutlineTo",
            name = "Limit outline to",
            description = "Outline only the Active Guardian Portal with the highest selected option.",
            position = 7,
            section = outlines
    )
    default LimitOutlineConfigOptions limitOutlineTo() {
        return LimitOutlineConfigOptions.NO_LIMIT;
    }

    @ConfigItem(
            keyName = "onlyOutlineGuardianCustom",
            name = "Custom outline list",
            description = "Outline only the highest listed Active Guardian Portal.",
            position = 8,
            section = outlines
    )
    default String onlyOutlineGuardianByCustom() {
        return "blood rune,death rune,fire rune,nature rune,law rune,cosmic rune,earth rune,chaos rune,water rune,air rune,mind rune,body rune";
    }

    @ConfigItem(
            keyName = "showHugeGuardianRemainsPortalHintArrow",
            name = "Show Mine Portal Hint Arrow",
            description = "Show the Hint Arrow above the Huge Guardian Remains portal.",
            position = 9,
            section = overlays
    )
    default boolean showHugeGuardianRemainsPortalHintArrow() {
        return true;
    }

    @ConfigItem(
            keyName = "showHugeGuardianRemainsPortalTimer",
            name = "Show Mine Portal timer",
            description = "Show the timer above the Huge Guardian Remains portal.",
            position = 10,
            section = overlays
    )
    default boolean showHugeGuardianRemainsPortalTimer() {
        return true;
    }

    @ConfigItem(
            keyName = "showActiveGuardianPortalTimer",
            name = "Show Active Guardian timer",
            description = "Show the timer above the Active Guardian Portals.",
            position = 11,
            section = overlays
    )
    default boolean showActiveGuardianPortalTimer() {
        return true;
    }

    @ConfigItem(
            keyName = "showActiveGuardianRuneImage",
            name = "Show Active Guardian Rune",
            description = "Show the rune image above the Active Guardian Portals.",
            position = 12,
            section = overlays
    )
    default boolean showActiveGuardianRuneImage() {
        return true;
    }

    @ConfigItem(
            keyName = "showActiveGuardianTalismanImage",
            name = "Show Active Guardian Talisman",
            description = "Show the talisman image above the Active Guardian Portals if in inventory.",
            position = 13,
            section = overlays
    )
    default boolean showActiveGuardianTalismanImage() {
        return true;
    }

    @ConfigItem(
            keyName = "showGOTREPanel",
            name = "Show GOTR Extended Panel",
            description = "Show the GOTR Extended Panel with various bits of information on it.",
            position = 14,
            section = panel
    )
    default boolean showGOTREPanel() {
        return true;
    }

    @ConfigItem(
            keyName = "showGameStart",
            name = "Show Game Start Timer",
            description = "Show the Game Start Timer on the GOTR Extended Panel.",
            position = 15,
            section = panel
    )
    default boolean showGameStart() {
        return true;
    }

    @ConfigItem(
            keyName = "showLastPortalSpawn",
            name = "Show last Portal spawn Timer",
            description = "Show the last Portal spawn Timer on the GOTR Extended Panel.",
            position = 16,
            section = panel
    )
    default boolean showLastPortalSpawn() {
        return true;
    }

    @ConfigItem(
            keyName = "showCurrentPoints",
            name = "Show currently saved points",
            description = "Show your currently saved points on the GOTR Extended Panel.",
            position = 17,
            section = panel
    )
    default boolean showCurrentPoints() {
        return true;
    }

    @ConfigItem(
            keyName = "colorActiveGuardianPortalsBasedBy",
            name = "Color Active Guardians by",
            description = "Outline Active Guardian Portals with colors based on the selected option.",
            position = 18,
            section = colors
    )
    default ColorActiveGuardianPortalsConfigOptions colorActiveGuardianPortalsBasedBy() {
        return ColorActiveGuardianPortalsConfigOptions.RUNE_TYPE;
    }

    @ConfigItem(
            keyName = "elementalRuneOutlineColor",
            name = "Elemental Rune outline color",
            description = "Color for the Elemental Rune outline.",
            position = 19,
            section = colors
    )
    default Color elementalRuneOutlineColor() {
        return new Color(0, 255, 0, 150);
    }

    @ConfigItem(
            keyName = "catalyticRuneOutlineColor",
            name = "Catalytic Rune outline color",
            description = "Color for the Catalytic Rune outline.",
            position = 20,
            section = colors
    )
    default Color catalyticRuneOutlineColor() {
        return new Color(255, 0, 0, 150);
    }

    @ConfigItem(
            keyName = "weakCellOutlineColor",
            name = "Weak Cell outline color",
            description = "Color for the Weak Cell outline.",
            position = 21,
            section = colors
    )
    default Color weakCellOutlineColor() {
        return new Color(255, 255, 255, 150);
    }

    @ConfigItem(
            keyName = "mediumCellOutlineColor",
            name = "Medium Cell outline color",
            description = "Color for the Medium Cell outline.",
            position = 22,
            section = colors
    )
    default Color mediumCellOutlineColor() {
        return new Color(0, 0, 255, 150);
    }

    @ConfigItem(
            keyName = "strongCellOutlineColor",
            name = "Strong Cell outline color",
            description = "Color for the Strong Cell outline.",
            position = 23,
            section = colors
    )
    default Color strongCellOutlineColor() {
        return new Color(0, 255, 0, 150);
    }

    @ConfigItem(
            keyName = "overchargedCellOutlineColor",
            name = "Overcharged Cell outline color",
            description = "Color for the Overcharged Cell outline.",
            position = 24,
            section = colors
    )
    default Color overchargedCellOutlineColor() {
        return new Color(255, 0, 0, 150);
    }

    @ConfigItem(
            keyName = "customOutlineColor",
            name = "Custom outline color",
            description = "Color for custom outlining.",
            position = 25,
            section = colors
    )
    default Color customOutlineColor() {
        return new Color(0, 255, 255, 150);
    }

    @ConfigItem(
            keyName = "depositPoolColor",
            name = "Deposit Pool color",
            description = "Color for outlining the Deposit Pool.",
            position = 26,
            section = colors
    )
    default Color depositPoolColor() {
        return new Color(255, 255, 0, 255);
    }

    @ConfigItem(
            keyName = "greatGuardianColor",
            name = "Great Guardian color",
            description = "Color for the Great Guardian outline.",
            position = 27,
            section = colors
    )
    default Color greatGuardianColor() {
        return new Color(255, 255, 0, 255);
    }

    @ConfigItem(
            keyName = "unchargedCellsTableColor",
            name = "Uncharged Cells Table color",
            description = "Color for the Uncharged Cells Table outline.",
            position = 28,
            section = colors
    )
    default Color unchargedCellsTableColor() {
        return new Color(255, 255, 0, 255);
    }

    @ConfigItem(
            keyName = "essencePileColor",
            name = "Essence Pile color",
            description = "Color for the Essence Pile outline.",
            position = 29,
            section = colors
    )
    default Color essencePileColor() {
        return new Color(255, 255, 0, 255);
    }
}
