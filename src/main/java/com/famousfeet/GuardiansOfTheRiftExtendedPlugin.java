package com.famousfeet;

import com.famousfeet.domain.GuardianPortalInfo;
import com.famousfeet.domain.GuardiansOfTheRiftExtendedOverlay;
import com.famousfeet.domain.GuardiansOfTheRiftExtendedPanel;
import com.famousfeet.enumerations.CellType;
import com.famousfeet.enumerations.RuneType;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import javax.inject.Inject;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
        name = "Guardians Of The Rift Extended"
)
public class GuardiansOfTheRiftExtendedPlugin extends Plugin {
    private final int chiselId = 1755;
    private final int overchargedCellId = 26886;
    private final int catalyticGuardianStoneId = 26880;
    private final int elementalGuardianStoneId = 26881;
    private final int polyElementalGuardianStoneId = 26941;
    private final int minigameMainRegion = 14484;
    private final int portalId = ObjectID.PORTAL_43729;
    private final int guardianActiveAnimation = 9363;
    private final int depositPoolId = 43696;
    private final int unchargedCellId = 26882;
    private final int elementalEssencePileId = 43722;
    private final int catalyticEssencePileId = 43723;
    private final int unchargedCellsTableId = 43732;
    private final int greatGuardianId = 11403;

    private final String checkpointRegex = "You have (\\d+) catalytic energy and (\\d+) elemental energy";
    private final Pattern checkpointPattern = Pattern.compile(checkpointRegex);
    private final String rewardPointRegex = "Total elemental energy:[^>]+>([\\d,]+).*Total catalytic energy:[^>]+>([\\d,]+).";
    private final Pattern rewardPointPattern = Pattern.compile(rewardPointRegex);

    private final Map<Integer, GuardianPortalInfo> guardianPortalInfo = new HashMap<>();
    private final Set<GameObject> guardians = new HashSet<>();
    private final List<GameObject> activeGuardianPortals = new ArrayList<>();
    private final List<Integer> inventoryTalismanIds = new ArrayList<>();
    private final Map<String, String> expandCardinal = new HashMap<>();
    private final List<Integer> altarPortalIds = new ArrayList<>();
    private List<Integer> talismanItemIds = new ArrayList<>();

    @Inject
    private Client client;
    @Inject
    private ItemManager itemManager;
    @Inject
    private GuardiansOfTheRiftExtendedConfig config;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private ModelOutlineRenderer modelOutlineRenderer;
    @Inject
    private Notifier notifier;


    private GuardiansOfTheRiftExtendedOverlay overlay;
    private GuardiansOfTheRiftExtendedPanel panel;

    private boolean isInMiniGame;
    private boolean isInMainRegion;
    private boolean areGuardiansNeeded = false;
    private boolean hasOverchargedCell = false;
    private boolean isFirstPortal = false;
    private boolean outlineDepositPool = false;
    private boolean outlineGreatGuardian = false;
    private boolean outlineUnchargedCellsTable = false;
    private int lastElementalRuneSprite;
    private int lastCatalyticRuneSprite;
    private int entryBarrierClickCooldown = 0;
    private GameObject minePortal;
    private GameObject depositPool;
    private GameObject unchargedCellTable;
    private GameObject catalyticEssencePile;
    private GameObject elementalEssencePile;
    private NPC greatGuardian;

    private int elementalRewardPoints;
    private int catalyticRewardPoints;

    private Optional<Instant> lastPortalDespawnTime = Optional.empty();
    private Optional<Instant> nextGameStart = Optional.empty();
    private Optional<Instant> minePortalSpawnTime = Optional.empty();

    @Override
    protected void startUp() throws Exception {
        this.overlay = new GuardiansOfTheRiftExtendedOverlay(this);
        this.panel = new GuardiansOfTheRiftExtendedPanel(this);
        overlayManager.add(overlay);
        overlayManager.add(panel);
        isInMiniGame = true;

        initializeGuardianPortalInfo();
        initializeTalismanItems();
        initializeExpandCardinal();
        initializeAltarPortalIds();
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(overlay);
        overlayManager.remove(panel);
        this.activeGuardianPortals.clear();
        resetPlugin();
    }

    private void resetPlugin() {
        guardians.clear();
        activeGuardianPortals.clear();
        unchargedCellTable = null;
        greatGuardian = null;
        catalyticEssencePile = null;
        elementalEssencePile = null;
        depositPool = null;
        minePortal = null;
        client.clearHintArrow();
    }

    private void initializeGuardianPortalInfo() {
        guardianPortalInfo.put(ObjectID.GUARDIAN_OF_AIR, new GuardianPortalInfo("AIR", 1, ItemID.AIR_RUNE, 26887, 4353, RuneType.ELEMENTAL, CellType.WEAK));
        guardianPortalInfo.put(ObjectID.GUARDIAN_OF_MIND, new GuardianPortalInfo("MIND", 2, ItemID.MIND_RUNE, 26891, 4354, RuneType.CATALYTIC, CellType.WEAK));
        guardianPortalInfo.put(ObjectID.GUARDIAN_OF_WATER, new GuardianPortalInfo("WATER", 5, ItemID.WATER_RUNE, 26888, 4355, RuneType.ELEMENTAL, CellType.MEDIUM));
        guardianPortalInfo.put(ObjectID.GUARDIAN_OF_EARTH, new GuardianPortalInfo("EARTH", 9, ItemID.EARTH_RUNE, 26889, 4356, RuneType.ELEMENTAL, CellType.STRONG));
        guardianPortalInfo.put(ObjectID.GUARDIAN_OF_FIRE, new GuardianPortalInfo("FIRE", 14, ItemID.FIRE_RUNE, 26890, 4357, RuneType.ELEMENTAL, CellType.OVERCHARGED));
        guardianPortalInfo.put(ObjectID.GUARDIAN_OF_BODY, new GuardianPortalInfo("BODY", 20, ItemID.BODY_RUNE, 26895, 4358, RuneType.CATALYTIC, CellType.WEAK));
        guardianPortalInfo.put(ObjectID.GUARDIAN_OF_COSMIC, new GuardianPortalInfo("COSMIC", 27, ItemID.COSMIC_RUNE, 26896, 4359, RuneType.CATALYTIC, CellType.MEDIUM));
        guardianPortalInfo.put(ObjectID.GUARDIAN_OF_CHAOS, new GuardianPortalInfo("CHAOS", 35, ItemID.CHAOS_RUNE, 26892, 4360, RuneType.CATALYTIC, CellType.MEDIUM));
        guardianPortalInfo.put(ObjectID.GUARDIAN_OF_NATURE, new GuardianPortalInfo("NATURE", 44, ItemID.NATURE_RUNE, 26897, 4361, RuneType.CATALYTIC, CellType.STRONG));
        guardianPortalInfo.put(ObjectID.GUARDIAN_OF_LAW, new GuardianPortalInfo("LAW", 54, ItemID.LAW_RUNE, 26898, 4362, RuneType.CATALYTIC, CellType.STRONG));
        guardianPortalInfo.put(ObjectID.GUARDIAN_OF_DEATH, new GuardianPortalInfo("DEATH", 65, ItemID.DEATH_RUNE, 26893, 4363, RuneType.CATALYTIC, CellType.OVERCHARGED));
        guardianPortalInfo.put(ObjectID.GUARDIAN_OF_BLOOD, new GuardianPortalInfo("BLOOD", 77, ItemID.BLOOD_RUNE, 26894, 4364, RuneType.CATALYTIC, CellType.OVERCHARGED));
    }

    private void initializeTalismanItems() {
        talismanItemIds = guardianPortalInfo.values().stream().map(GuardianPortalInfo::getTalismanId).collect(Collectors.toList());
    }

    private void initializeExpandCardinal() {
        expandCardinal.put("S", "south");
        expandCardinal.put("SW", "south west");
        expandCardinal.put("W", "west");
        expandCardinal.put("NW", "north west");
        expandCardinal.put("N", "north");
        expandCardinal.put("NE", "north east");
        expandCardinal.put("E", "east");
        expandCardinal.put("SE", "south east");
    }

    private void initializeAltarPortalIds() {
        altarPortalIds.add(ObjectID.PORTAL_34748);
        altarPortalIds.add(ObjectID.PORTAL_34749);
        altarPortalIds.add(ObjectID.PORTAL_34750);
        altarPortalIds.add(ObjectID.PORTAL_34751);
        altarPortalIds.add(ObjectID.PORTAL_34752);
        altarPortalIds.add(ObjectID.PORTAL_34753);
        altarPortalIds.add(ObjectID.PORTAL_34754);
        altarPortalIds.add(ObjectID.PORTAL_34755);
        altarPortalIds.add(ObjectID.PORTAL_34756);
        altarPortalIds.add(ObjectID.PORTAL_34757);
        altarPortalIds.add(ObjectID.PORTAL_34758);
        altarPortalIds.add(ObjectID.PORTAL_43478);
    }

    @Provides
    GuardiansOfTheRiftExtendedConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(GuardiansOfTheRiftExtendedConfig.class);
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOADING) {
            resetPlugin();
        } else if (event.getGameState() == GameState.LOGIN_SCREEN) {
            isInMiniGame = false;
        }
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event) {
        GameObject gameObject = event.getGameObject();
        if (isGuardianPortal(gameObject)) {
            guardians.add(gameObject);
        }

        if (gameObject.getId() == portalId) {
            minePortal = gameObject;
            if (config.showHugeGuardianRemainsPortalHintArrow()) {
                client.setHintArrow(minePortal.getWorldLocation());
            }
        }

        if (gameObject.getId() == depositPoolId) {
            depositPool = gameObject;
        }

        if (gameObject.getId() == elementalEssencePileId) {
            elementalEssencePile = gameObject;
        }

        if (gameObject.getId() == catalyticEssencePileId) {
            catalyticEssencePile = gameObject;
        }

        if (gameObject.getId() == unchargedCellsTableId) {
            unchargedCellTable = gameObject;
        }
    }

    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned event) {
        GameObject gameObject = event.getGameObject();

        guardians.remove(gameObject);
        activeGuardianPortals.remove(gameObject);

        if (gameObject.getId() == portalId) {
            client.clearHintArrow();
            minePortal = null;
        }

        if (gameObject.getId() == depositPoolId) {
            depositPool = null;
        }

        if (gameObject.getId() == elementalEssencePileId) {
            elementalEssencePile = null;
        }

        if (gameObject.getId() == catalyticEssencePileId) {
            catalyticEssencePile = null;
        }

        if (gameObject.getId() == unchargedCellsTableId) {
            unchargedCellTable = null;
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (entryBarrierClickCooldown > 0) {
            entryBarrierClickCooldown--;
        }

        updateIsInMinigame();
        updateInMainRegion();
        updateActivePortals();
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {
        if (!isInMainRegion || event.getItemContainer() != client.getItemContainer(InventoryID.INVENTORY)) {
            return;
        }

        Item[] items = event.getItemContainer().getItems();

        outlineDepositPool = Arrays.stream(items).anyMatch(item -> guardianPortalInfo.values().stream().map(GuardianPortalInfo::getRuneId).collect(Collectors.toList()).contains(item.getId()));
        hasOverchargedCell = Arrays.stream(items).anyMatch(x -> x.getId() == chiselId) && Arrays.stream(items).anyMatch(x -> x.getId() == overchargedCellId);
        outlineGreatGuardian = Arrays.stream(items).anyMatch(x -> x.getId() == elementalGuardianStoneId || x.getId() == catalyticGuardianStoneId || x.getId() == polyElementalGuardianStoneId);

        outlineUnchargedCellsTable = Arrays.stream(items).filter(item -> item != null && item.getId() == unchargedCellId).mapToInt(Item::getQuantity).sum() <= config.unchargedCellsOutlineLimit();

        inventoryTalismanIds.clear();
        inventoryTalismanIds.addAll(Arrays.stream(items).mapToInt(Item::getId).filter(talismanItemIds::contains).boxed().collect(Collectors.toList()));
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned npcSpawned) {
        NPC npc = npcSpawned.getNpc();
        if (npc.getId() == greatGuardianId) {
            greatGuardian = npc;
        }
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned npcDespawned) {
        NPC npc = npcDespawned.getNpc();
        if (npc.getId() == greatGuardianId) {
            greatGuardian = null;
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        if (!isInMainRegion) {
            return;
        }

        if (chatMessage.getType() != ChatMessageType.SPAM && chatMessage.getType() != ChatMessageType.GAMEMESSAGE) {
            return;
        }

        String msg = chatMessage.getMessage();

        if (msg.contains("You step through the portal")) {
            client.clearHintArrow();
            nextGameStart = Optional.empty();
        }

        if (msg.contains("The rift becomes active!")) {
            lastPortalDespawnTime = Optional.of(Instant.now());
            nextGameStart = Optional.empty();
            isFirstPortal = true;
            if (config.notifyGameStart() && notifier != null) {
                notifier.notify("The Guardians Of The Rift game has started.");
            }
        } else if (msg.contains("The rift will become active in 30 seconds.")) {
            nextGameStart = Optional.of(Instant.now().plusSeconds(30));
        } else if (msg.contains("The rift will become active in 10 seconds.")) {
            nextGameStart = Optional.of(Instant.now().plusSeconds(10));
        } else if (msg.contains("The rift will become active in 5 seconds.")) {
            nextGameStart = Optional.of(Instant.now().plusSeconds(5));
        } else if (msg.contains("The Portal Guardians will keep their rifts open for another 30 seconds.")) {
            nextGameStart = Optional.of(Instant.now().plusSeconds(60));
        } else if (msg.contains("You found some loot:")) {
            elementalRewardPoints--;
            catalyticRewardPoints--;
        }

        Matcher rewardPointMatcher = rewardPointPattern.matcher(msg);
        if (rewardPointMatcher.find()) {
            elementalRewardPoints = Integer.parseInt(rewardPointMatcher.group(1).replaceAll(",", ""));
            catalyticRewardPoints = Integer.parseInt(rewardPointMatcher.group(2).replaceAll(",", ""));
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (config.quickPassCooldown() && event.getId() == ObjectID.BARRIER_43700 && event.getMenuAction().getId() == 5) {
            if (entryBarrierClickCooldown > 0) {
                event.consume();
            } else {
                entryBarrierClickCooldown = 3;
            }

            ItemContainer itemContainer = client.getItemContainer(InventoryID.INVENTORY);
            if (itemContainer != null) {
                Item[] items = itemContainer.getItems();
                outlineUnchargedCellsTable = Arrays.stream(items).filter(item -> item != null && item.getId() == unchargedCellId).mapToInt(Item::getQuantity).sum() <= config.unchargedCellsOutlineLimit();
            }
        }

        if (event.getMenuAction().getId() == 3 && altarPortalIds.contains(event.getId())) {
            ItemContainer itemContainer = client.getItemContainer(InventoryID.INVENTORY);
            if (itemContainer != null) {
                Item[] items = itemContainer.getItems();
                outlineDepositPool = Arrays.stream(items).anyMatch(item -> guardianPortalInfo.values().stream().map(GuardianPortalInfo::getRuneId).collect(Collectors.toList()).contains(item.getId()));
                hasOverchargedCell = Arrays.stream(items).anyMatch(x -> x.getId() == chiselId) && Arrays.stream(items).anyMatch(x -> x.getId() == overchargedCellId);
                outlineGreatGuardian = Arrays.stream(items).anyMatch(x -> x.getId() == elementalGuardianStoneId || x.getId() == catalyticGuardianStoneId || x.getId() == polyElementalGuardianStoneId);

                inventoryTalismanIds.clear();
                inventoryTalismanIds.addAll(Arrays.stream(items).mapToInt(Item::getId).filter(talismanItemIds::contains).boxed().collect(Collectors.toList()));
            }
        }
    }

    @Subscribe
    public void onOverheadTextChanged(OverheadTextChanged event) {
        if (!("Apprentice Tamara".equals(event.getActor().getName()) || "Apprentice Cordelia".equals(event.getActor().getName()))) {
            return;
        }

        if (config.muteApprentices()) {
            event.getActor().setOverheadText(" ");
        }
    }

    private void updateActivePortals() {
        activeGuardianPortals.removeIf(ag -> {
            Animation anim = ((DynamicObject) ag.getRenderable()).getAnimation();
            return (anim == null || anim.getId() != guardianActiveAnimation) && !inventoryTalismanIds.contains(guardianPortalInfo.get(ag.getId()).getTalismanId());
        });

        activeGuardianPortals.addAll(guardians.stream()
                .filter(guardian -> {
                    Animation animation = ((DynamicObject) guardian.getRenderable()).getAnimation();
                    return !activeGuardianPortals.contains(guardian) && ((animation != null && animation.getId() == guardianActiveAnimation) || inventoryTalismanIds.contains(guardianPortalInfo.get(guardian.getId()).getTalismanId()));
                })
                .collect(Collectors.toList()));

        int elementalRuneWidgetId = 48889879;
        Widget elementalRuneWidget = client.getWidget(elementalRuneWidgetId);
        int catalyticRuneWidgetId = 48889876;
        Widget catalyticRuneWidget = client.getWidget(catalyticRuneWidgetId);
        int guardianCountWidgetId = 48889886;
        Widget guardianCountWidget = client.getWidget(guardianCountWidgetId);
        int portalWidgetId = 48889884;
        Widget portalWidget = client.getWidget(portalWidgetId);

        lastElementalRuneSprite = parseRuneWidget(elementalRuneWidget, lastElementalRuneSprite);
        lastCatalyticRuneSprite = parseRuneWidget(catalyticRuneWidget, lastCatalyticRuneSprite);

        if (guardianCountWidget != null) {
            String text = guardianCountWidget.getText();
            areGuardiansNeeded = text != null && !text.contains("10/10");
        }

        if (portalWidget != null && !portalWidget.isHidden()) {
            if (!minePortalSpawnTime.isPresent() && lastPortalDespawnTime.isPresent()) {
                lastPortalDespawnTime = Optional.empty();
                if (isFirstPortal) {
                    isFirstPortal = false;
                }
                if (config.notifyPortalSpawn() && notifier != null) {
                    String compass = portalWidget.getText().split(" ")[0];
                    String full = expandCardinal.getOrDefault(compass, "unknown");
                    notifier.notify(String.format("A portal has spawned in the %s.", full));
                }
            }
            minePortalSpawnTime = minePortalSpawnTime.isPresent() ? minePortalSpawnTime : Optional.of(Instant.now());
        } else if (elementalRuneWidget != null && !elementalRuneWidget.isHidden()) {
            if (minePortalSpawnTime.isPresent()) {
                lastPortalDespawnTime = Optional.of(Instant.now());
            }
            minePortalSpawnTime = Optional.empty();
        }

        int dialogWidgetGroup = 229;
        int dialogWidgetMessage = 1;
        Widget dialog = client.getWidget(dialogWidgetGroup, dialogWidgetMessage);
        if (dialog != null) {
            String dialogText = dialog.getText();
            String barrierDialogFinishingUp = "It looks like the adventurers within are just finishing up. You must<br>wait until they are done to join.";
            if (dialogText.equals(barrierDialogFinishingUp)) {
                entryBarrierClickCooldown = 0;
            } else {
                final Matcher checkMatcher = checkpointPattern.matcher(dialogText);
                if (checkMatcher.find(0)) {
                    catalyticRewardPoints = Integer.parseInt(checkMatcher.group(1));
                    elementalRewardPoints = Integer.parseInt(checkMatcher.group(2));
                }
            }
        }
    }

    int parseRuneWidget(Widget runeWidget, int lastSpriteId) {
        if (runeWidget != null) {
            int spriteId = runeWidget.getSpriteId();
            if (spriteId != lastSpriteId) {
                if (lastSpriteId > 0) {
                    Optional<GuardianPortalInfo> lastGuardian = guardianPortalInfo.values().stream().filter(g -> g.getSpriteId() == lastSpriteId).findFirst();
                    lastGuardian.ifPresent(GuardianPortalInfo::despawn);
                }
                Optional<GuardianPortalInfo> currentGuardian = guardianPortalInfo.values().stream().filter(g -> g.getSpriteId() == spriteId).findFirst();
                currentGuardian.ifPresent(GuardianPortalInfo::spawn);
            }
            return spriteId;
        }
        return lastSpriteId;
    }

    private boolean isGuardianPortal(GameObject gameObject) {
        return guardianPortalInfo.containsKey(gameObject.getId());
    }

    private void updateIsInMinigame() {
        GameState gameState = client.getGameState();
        if (gameState != GameState.LOGGED_IN && gameState != GameState.LOADING) {
            isInMiniGame = false;
        }

        int parentWidgetId = 48889857;
        Widget elementalRuneWidget = client.getWidget(parentWidgetId);
        isInMiniGame = elementalRuneWidget != null;
    }

    private void updateInMainRegion() {
        int[] currentMapRegions = client.getMapRegions();
        isInMainRegion = Arrays.stream(currentMapRegions).anyMatch(x -> x == minigameMainRegion);
    }

    public List<Integer> getInventoryTalismanIds() {
        return inventoryTalismanIds;
    }

    public Map<Integer, GuardianPortalInfo> getGuardianPortalInfo() {
        return guardianPortalInfo;
    }

    public Client getClient() {
        return client;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public GuardiansOfTheRiftExtendedConfig getConfig() {
        return config;
    }

    public List<GameObject> getActiveGuardianPortals() {
        return activeGuardianPortals;
    }

    public boolean isInMiniGame() {
        return isInMiniGame;
    }

    public boolean isInMainRegion() {
        return isInMainRegion;
    }

    public GameObject getMinePortal() {
        return minePortal;
    }

    public Optional<Instant> getMinePortalSpawnTime() {
        return minePortalSpawnTime;
    }

    public int getPortalTickCount() {
        return 43;
    }

    public Optional<Instant> getNextGameStart() {
        return nextGameStart;
    }

    public int getElementalRewardPoints() {
        return elementalRewardPoints;
    }

    public int getCatalyticRewardPoints() {
        return catalyticRewardPoints;
    }

    public Optional<Instant> getLastPortalDespawnTime() {
        return lastPortalDespawnTime;
    }

    public boolean isFirstPortal() {
        return isFirstPortal;
    }

    public GameObject getDepositPool() {
        return depositPool;
    }

    public boolean isOutlineDepositPool() {
        return outlineDepositPool;
    }

    public boolean isOutlineGreatGuardian() {
        return outlineGreatGuardian;
    }

    public boolean isOutlineUnchargedCellsTable() {
        return outlineUnchargedCellsTable;
    }

    public NPC getGreatGuardian() {
        return greatGuardian;
    }

    public GameObject getUnchargedCellTable() {
        return unchargedCellTable;
    }

    public GameObject getCatalyticEssencePile() {
        return catalyticEssencePile;
    }

    public GameObject getElementalEssencePile() {
        return elementalEssencePile;
    }

    public boolean areGuardiansNeeded() {
        return areGuardiansNeeded;
    }

    public boolean hasOverchargedCell() {
        return hasOverchargedCell;
    }
}
