package com.famousfeet.domain;

import com.famousfeet.GuardiansOfTheRiftExtendedPlugin;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;

import javax.inject.Inject;
import java.awt.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

public class GuardiansOfTheRiftExtendedPanel extends OverlayPanel {
    private GuardiansOfTheRiftExtendedPlugin plugin;

    @Inject
    public GuardiansOfTheRiftExtendedPanel(GuardiansOfTheRiftExtendedPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
        setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
        getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Guardians of the Rift Extended Overlay"));
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!plugin.getConfig().showGOTREPanel() || (!plugin.isInMainRegion() && !plugin.isInMiniGame())) {
            return null;
        }

        Optional<Instant> gameStart = plugin.getNextGameStart();
        if (plugin.getConfig().showGameStart() && gameStart.isPresent()) {
            int timeToStart = ((int) ChronoUnit.SECONDS.between(Instant.now(), gameStart.get()));
            if (timeToStart >= 0) {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Time to start:")
                        .right("" + timeToStart)
                        .rightColor(getGameStartColor(timeToStart))
                        .build());
            }
        } else if (plugin.getConfig().showLastPortalSpawn()) {
            Optional<Instant> despawn = plugin.getLastPortalDespawnTime();
            int timeSincePortal = despawn.map(instant -> ((int) (ChronoUnit.SECONDS.between(instant, Instant.now())))).orElse(-1);
            if (timeSincePortal != -1) {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Last portal:")
                        .right("" + timeSincePortal)
                        .rightColor(getTimeSincePortalColor(timeSincePortal))
                        .build());
            }
        }

        if (plugin.getConfig().showCurrentPoints()) {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Points:")
                    .right(plugin.getElementalRewardPoints() + "/" + plugin.getCatalyticRewardPoints())
                    .build());
        }

        return super.render(graphics);
    }

    private Color getTimeSincePortalColor(int timeSincePortal) {
        if (plugin.isFirstPortal()) {
            // first portal takes about 40 more seconds to spawn
            timeSincePortal -= 40;
        }
        if (timeSincePortal >= 108) {
            return Color.RED;
        } else if (timeSincePortal >= 85) {
            return Color.YELLOW;
        }
        return Color.GREEN;
    }

    private Color getGameStartColor(int timeTillGameStart) {
        if (timeTillGameStart > 10) {
            return Color.GREEN;
        } else if (timeTillGameStart > 5) {
            return Color.YELLOW;
        } else {
            return Color.RED;
        }
    }
}
