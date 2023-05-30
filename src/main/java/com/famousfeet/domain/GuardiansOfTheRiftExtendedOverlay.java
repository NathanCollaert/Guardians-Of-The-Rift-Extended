package com.famousfeet.domain;

import com.famousfeet.GuardiansOfTheRiftExtendedPlugin;
import com.famousfeet.enumerations.LimitOutlineConfigOptions;
import com.famousfeet.enumerations.RuneType;
import net.runelite.api.Point;
import net.runelite.api.*;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class GuardiansOfTheRiftExtendedOverlay extends Overlay {

    private final GuardiansOfTheRiftExtendedPlugin plugin;

    public GuardiansOfTheRiftExtendedOverlay(GuardiansOfTheRiftExtendedPlugin plugin) {
        this.plugin = plugin;
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPosition(OverlayPosition.DYNAMIC);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (plugin.isInMainRegion()) {
            renderActivePortals(graphics);
            renderMinePortal(graphics);
            highlightDepositPool(graphics);
            highlightUnchargedCellTable(graphics);
            highlightGreatGuardian(graphics);
            highlightEssencePiles(graphics);
        }
        return null;
    }

    private void renderActivePortals(Graphics2D graphics) {
        if (!plugin.getConfig().outlineActiveGuardianPortals()) {
            return;
        }

        plugin.getActiveGuardianPortals().stream()
                .filter(portal -> !plugin.getConfig().levelOverride() || plugin.getGuardianPortalInfo().get(portal.getId()).getRequiredLevel() <= plugin.getClient().getBoostedSkillLevel(Skill.RUNECRAFT))
                .sorted(getLimitOutlineComparator())
                .limit(plugin.getConfig().limitOutlineTo() == LimitOutlineConfigOptions.NO_LIMIT ? 12 : 1)
                .forEach(portal -> {
                    GuardianPortalInfo portalInfo = plugin.getGuardianPortalInfo().get(portal.getId());
                    renderActivePortal(graphics, portal, portalInfo);
                });
    }

    private void highlightDepositPool(Graphics2D graphics) {
        if (!plugin.getConfig().outlineDepositPool()) {
            return;
        }

        GameObject depositPool = plugin.getDepositPool();
        if (plugin.isOutlineDepositPool() && depositPool != null) {
            graphics.setColor(plugin.getConfig().depositPoolColor());
            graphics.setStroke(new BasicStroke(1));
            graphics.draw(depositPool.getConvexHull());
        }
    }

    private void highlightGreatGuardian(Graphics2D graphics) {
        if (!plugin.getConfig().outlineGreatGuardian()) {
            return;
        }

        NPC greatGuardian = plugin.getGreatGuardian();
        if (plugin.isOutlineGreatGuardian() && greatGuardian != null) {
            graphics.setColor(plugin.getConfig().greatGuardianColor());
            graphics.setStroke(new BasicStroke(1));
            graphics.draw(greatGuardian.getConvexHull());
        }
    }

    private void highlightUnchargedCellTable(Graphics2D graphics) {
        if (!plugin.getConfig().outlineUnchargedCellsTable()) {
            return;
        }

        GameObject table = plugin.getUnchargedCellTable();
        if (plugin.isOutlineUnchargedCellsTable() && table != null) {
            graphics.setColor(plugin.getConfig().unchargedCellsTableColor());
            graphics.setStroke(new BasicStroke(1));
            graphics.draw(table.getConvexHull());
        }
    }

    private void highlightEssencePiles(Graphics2D graphics) {
        if (plugin.areGuardiansNeeded() && plugin.hasOverchargedCell()) {
            GameObject elementalEssencePile = plugin.getElementalEssencePile();
            GameObject catalyticEssencePile = plugin.getCatalyticEssencePile();
            if (elementalEssencePile != null) {
                graphics.setColor(plugin.getConfig().essencePileColor());
                graphics.setStroke(new BasicStroke(1));
                graphics.draw(elementalEssencePile.getConvexHull());
            }
            if (catalyticEssencePile != null) {
                graphics.setColor(plugin.getConfig().essencePileColor());
                graphics.setStroke(new BasicStroke(1));
                graphics.draw(catalyticEssencePile.getConvexHull());
            }
        }
    }

    private void renderMinePortal(Graphics2D graphics) {
        if (plugin.getConfig().showHugeGuardianRemainsPortalTimer() && plugin.getMinePortalSpawnTime().isPresent() && plugin.getMinePortal() != null) {
            Instant spawnTime = plugin.getMinePortalSpawnTime().get();
            GameObject portal = plugin.getMinePortal();
            long millis = ChronoUnit.MILLIS.between(Instant.now(), spawnTime.plusMillis((long) Math.floor(plugin.getPortalTickCount() * 600)));
            if (millis > 0) {
                String timeRemainingText = "" + (Math.round(millis / 100D) / 10D);
                Point textLocation = Perspective.getCanvasTextLocation(plugin.getClient(), graphics, portal.getLocalLocation(), timeRemainingText, 100);
                OverlayUtil.renderTextLocation(graphics, textLocation, timeRemainingText, Color.WHITE);
            }
        }
    }

    private void renderActivePortal(Graphics2D graphics, GameObject portal, GuardianPortalInfo portalInfo) {
        graphics.setColor(portalInfo.getColor(plugin.getConfig()));
        graphics.setStroke(new BasicStroke(2));
        graphics.draw(portal.getConvexHull());

        int portalImageOffset = 505;
        if (portalInfo.getSpawnTime().isPresent()) {
            BufferedImage image = portalInfo.getRuneImage(plugin.getItemManager());
            if (plugin.getConfig().showActiveGuardianRuneImage()) {
                OverlayUtil.renderImageLocation(plugin.getClient(), graphics, portal.getLocalLocation(), image, portalImageOffset);
            }
            Point imgLocation = Perspective.getCanvasImageLocation(plugin.getClient(), portal.getLocalLocation(), image, portalImageOffset);
            int guardianPortalTickCount = 33;
            long millis = ChronoUnit.MILLIS.between(Instant.now(), portalInfo.getSpawnTime().get().plusMillis((long) Math.floor(guardianPortalTickCount * 600)));
            if (plugin.getConfig().showActiveGuardianPortalTimer() && millis > 0) {
                String timeRemainingText = "" + (Math.round(millis / 100D) / 10D);
                Rectangle2D strBounds = graphics.getFontMetrics().getStringBounds(timeRemainingText, graphics);
                Point textLocation = Perspective.getCanvasTextLocation(plugin.getClient(), graphics, portal.getLocalLocation(), timeRemainingText, portalImageOffset + 60);
                textLocation = new Point((int) (imgLocation.getX() + image.getWidth() / 2D - strBounds.getWidth() / 2D), textLocation.getY());
                OverlayUtil.renderTextLocation(graphics, textLocation, timeRemainingText, Color.WHITE);
            }
        } else if (plugin.getConfig().showActiveGuardianTalismanImage() && isPortalOpenWithTalisman(portalInfo)) {
            BufferedImage image = portalInfo.getTalismanImage(plugin.getItemManager());
            OverlayUtil.renderImageLocation(plugin.getClient(), graphics, portal.getLocalLocation(), image, portalImageOffset);
        }
    }

    private boolean isPortalOpenWithTalisman(GuardianPortalInfo portalInfo) {
        return plugin.getInventoryTalismanIds().contains(portalInfo.getTalismanId());
    }

    private Comparator<GameObject> getLimitOutlineComparator() {
        Comparator<GameObject> comparator;
        switch (this.plugin.getConfig().limitOutlineTo()) {
            case CATALYTIC:
                comparator = Comparator.comparing((GameObject guardian) -> plugin.getGuardianPortalInfo().get(guardian.getId()).getRuneType() == RuneType.CATALYTIC).reversed();
                break;
            case ELEMENTAL:
                comparator = Comparator.comparing((GameObject guardian) -> plugin.getGuardianPortalInfo().get(guardian.getId()).getRuneType() == RuneType.ELEMENTAL).reversed();
                break;
            case HIGHEST_LEVEL:
                comparator = Comparator.comparing((GameObject guardian) -> plugin.getGuardianPortalInfo().get(guardian.getId()).getRequiredLevel()).reversed();
                break;
            case HIGHEST_TIER:
                comparator = Comparator.comparing((GameObject guardian) -> plugin.getGuardianPortalInfo().get(guardian.getId()).getCellType()).thenComparing((GameObject guardian) -> plugin.getGuardianPortalInfo().get(guardian.getId()).getProfitPerFragment(plugin.getClient().getBoostedSkillLevel(Skill.RUNECRAFT), plugin.getClient().getItemContainer(InventoryID.EQUIPMENT), plugin.getItemManager())).reversed();
                break;
            case HIGHEST_PROFIT:
                comparator = Comparator.comparing((GameObject guardian) -> plugin.getGuardianPortalInfo().get(guardian.getId()).getProfitPerFragment(plugin.getClient().getBoostedSkillLevel(Skill.RUNECRAFT), plugin.getClient().getItemContainer(InventoryID.EQUIPMENT), plugin.getItemManager())).reversed();
                break;
            case CUSTOM:
                List<String> customList = Arrays.asList(plugin.getConfig().onlyOutlineGuardianByCustom().toUpperCase().split(","));
                comparator = Comparator.comparing(guardian -> customList.indexOf(plugin.getItemManager().getItemComposition(plugin.getGuardianPortalInfo().get(guardian.getId()).getRuneId()).getMembersName().toUpperCase()));
                break;
            default:
                comparator = Comparator.comparing(GameObject::getId).reversed();
                break;
        }
        return comparator;
    }
}
