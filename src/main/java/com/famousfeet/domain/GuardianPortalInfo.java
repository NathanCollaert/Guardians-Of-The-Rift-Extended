package com.famousfeet.domain;

import com.famousfeet.GuardiansOfTheRiftExtendedConfig;
import com.famousfeet.enumerations.CellType;
import com.famousfeet.enumerations.RuneType;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.client.game.ItemManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class GuardianPortalInfo {
    private final String name;
    private final int requiredLevel;
    private final int runeId;
    private final int talismanId;
    private final int spriteId;
    private final RuneType runeType;
    private final CellType cellType;

    private final List<Integer> raimentsOfTheEyeSsetIds = new ArrayList<Integer>() {{
        add(ItemID.HAT_OF_THE_EYE);
        add(ItemID.HAT_OF_THE_EYE_BLUE);
        add(ItemID.HAT_OF_THE_EYE_GREEN);
        add(ItemID.HAT_OF_THE_EYE_RED);
        add(ItemID.ROBE_BOTTOMS_OF_THE_EYE);
        add(ItemID.ROBE_BOTTOMS_OF_THE_EYE_BLUE);
        add(ItemID.ROBE_BOTTOMS_OF_THE_EYE_GREEN);
        add(ItemID.ROBE_BOTTOMS_OF_THE_EYE_RED);
        add(ItemID.ROBE_TOP_OF_THE_EYE);
        add(ItemID.ROBE_TOP_OF_THE_EYE_BLUE);
        add(ItemID.ROBE_TOP_OF_THE_EYE_GREEN);
        add(ItemID.ROBE_TOP_OF_THE_EYE_RED);
        add(ItemID.BOOTS_OF_THE_EYE);
    }};

    private Optional<Instant> spawnTime = Optional.empty();

    public GuardianPortalInfo(String name, int requiredLevel, int runeId, int talismanId, int spriteId, RuneType runeType, CellType cellType) {
        this.name = name;
        this.requiredLevel = requiredLevel;
        this.runeId = runeId;
        this.talismanId = talismanId;
        this.spriteId = spriteId;
        this.runeType = runeType;
        this.cellType = cellType;
    }

    public String getName() {
        return name;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public int getRuneId() {
        return runeId;
    }

    public int getTalismanId() {
        return talismanId;
    }

    public int getSpriteId() {
        return spriteId;
    }

    public RuneType getRuneType() {
        return runeType;
    }

    public CellType getCellType() {
        return cellType;
    }

    public BufferedImage getRuneImage(ItemManager itemManager) {
        return itemManager.getImage(runeId);
    }

    public BufferedImage getTalismanImage(ItemManager itemManager) {
        return itemManager.getImage(talismanId);
    }

    public Color getColor(GuardiansOfTheRiftExtendedConfig config) {
        switch (config.colorActiveGuardianPortalsBasedBy()) {
            case RUNE_TYPE:
                return this.runeType.getColor(config);
            case CELL_TYPE:
                return this.cellType.getColor(config);
            case CUSTOM:
                return config.customOutlineColor();
            default:
                return Color.GREEN;
        }
    }

    public void spawn() {
        spawnTime = Optional.of(Instant.now());
    }

    public void despawn() {
        spawnTime = Optional.empty();
    }

    public Optional<Instant> getSpawnTime() {
        return spawnTime;
    }

    public double getProfitPerFragment(int playerLevel, ItemContainer itemContainer, ItemManager itemManager) {
        double amountOfSetWearing = itemContainer == null ? 0 : Arrays.stream(itemContainer.getItems()).map(Item::getId).filter(id -> raimentsOfTheEyeSsetIds.stream().anyMatch(bonusId -> bonusId.equals(id))).count();
        double setBonus = 1 + (amountOfSetWearing * .1 + (amountOfSetWearing == 4 ? .2 : 0));

        return itemManager.getItemPrice(this.runeId) * (new RuneLookupTable().getHighestMultiplier(this.runeId, playerLevel) * setBonus);
    }
}
