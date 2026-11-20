package com.denserunestone;

import com.google.inject.Provides;
import java.awt.Color;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.MenuAction;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.AnimationID;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

/**
 * Watches the dense runestone the player is actively chiselling at the Dense essence mine
 * (Isle of Souls) and shows a notification above the chatbox as soon as they stop mining it
 * - whether because it depleted, their inventory filled up, or they just walked off - so they
 * know to switch to the paired rock instead of AFKing on a rock they're no longer working.
 *
 * Rocks are identified by object composition name/action rather than hardcoded object IDs,
 * since the dense runestone appears to use many different object ID variants in the cache,
 * and the base composition returned for the clicked ID is an impostor stub that must be
 * resolved via {@link ObjectComposition#getImpostor()} to see its real name/actions.
 *
 * The rock is a multiloc object: mineable-vs-depleted is the same GameObject instance
 * switching its resolved impostor via a varbit, not a despawn/respawn, so depletion is
 * detected by polling the tracked tile each tick rather than listening for a spawn event.
 */
@Slf4j
@PluginDescriptor(
	name = "Dense Runestone Notifier",
	description = "Notifies you when you stop mining dense essence (depleted, inventory full, etc), so you know to switch to the other rock",
	tags = {"mining", "crafting", "dense", "runestone", "essence", "notification", "zeah", "blood", "soul", "arceuus", "runecraft", "idle"}
)
public class DenseRunestoneNotifierPlugin extends Plugin
{
	private static final String TARGET_NAME = "Dense runestone";
	private static final String MINE_ACTION = "Chip";
	private static final Duration CLEAR_AFTER = Duration.ofMinutes(5);
	private static final int CLEAR_DISTANCE_TILES = 20;

	private static final Set<MenuAction> GAME_OBJECT_MENU_ACTIONS = Set.of(
		MenuAction.GAME_OBJECT_FIRST_OPTION,
		MenuAction.GAME_OBJECT_SECOND_OPTION,
		MenuAction.GAME_OBJECT_THIRD_OPTION,
		MenuAction.GAME_OBJECT_FOURTH_OPTION,
		MenuAction.GAME_OBJECT_FIFTH_OPTION
	);

	@Inject
	private Client client;

	@Inject
	private DenseRunestoneNotifierConfig config;

	@Inject
	private Notifier notifier;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private DenseRunestoneNotifierOverlay overlay;

	// Set when the player clicks a dense runestone, confirmed into activeRock once they
	// actually start chiselling it (so a stray click without walking there doesn't count).
	private WorldPoint pendingRock;
	private WorldPoint activeRock;
	// Kept even after activeRock is cleared on a stop, so the overlay can still be cleared
	// once the player wanders far enough from wherever they were last mining.
	private WorldPoint lastRockLocation;
	private Instant lastEventTime;
	private String overlayMessage;
	private Color overlayColor;

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		pendingRock = null;
		activeRock = null;
		lastRockLocation = null;
		lastEventTime = null;
		overlayMessage = null;
	}

	String getOverlayMessage()
	{
		return overlayMessage;
	}

	Color getOverlayColor()
	{
		return overlayColor;
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		// The dense runestone is a multiloc object: the same GameObject instance stays in the
		// scene the whole time, and mineable-vs-depleted is just its impostor resolution
		// flipping via a varbit, so there's no despawn/spawn event to hook for depletion.
		// Poll the tracked tile instead and see if it still resolves as mineable.
		if (activeRock != null && !isTileStillMineable(activeRock))
		{
			log.info("[DRN-DEBUG] active rock at {} no longer mineable -> stopped", activeRock);
			activeRock = null;
			pendingRock = null;
			showStoppedNotification();
		}

		clearStaleOverlay();
	}

	private void clearStaleOverlay()
	{
		if (overlayMessage == null || lastEventTime == null)
		{
			return;
		}

		if (Duration.between(lastEventTime, Instant.now()).compareTo(CLEAR_AFTER) >= 0)
		{
			log.info("[DRN-DEBUG] clearing overlay after {} of inactivity", CLEAR_AFTER);
			overlayMessage = null;
			overlayColor = null;
			return;
		}

		if (lastRockLocation != null)
		{
			int distance = client.getLocalPlayer().getWorldLocation().distanceTo(lastRockLocation);
			if (distance > CLEAR_DISTANCE_TILES)
			{
				log.info("[DRN-DEBUG] clearing overlay - {} tiles from the mine", distance);
				overlayMessage = null;
				overlayColor = null;
			}
		}
	}

	private boolean isTileStillMineable(WorldPoint worldPoint)
	{
		LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);
		if (localPoint == null)
		{
			return true;
		}

		Tile tile = client.getTopLevelWorldView().getScene().getTiles()[worldPoint.getPlane()][localPoint.getSceneX()][localPoint.getSceneY()];
		if (tile == null)
		{
			return true;
		}

		for (GameObject gameObject : tile.getGameObjects())
		{
			if (gameObject != null && isMineableDenseRunestone(gameObject.getId()))
			{
				return true;
			}
		}

		return false;
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (!GAME_OBJECT_MENU_ACTIONS.contains(event.getMenuAction()))
		{
			return;
		}

		if (!isMineableDenseRunestone(event.getId()))
		{
			return;
		}

		LocalPoint localPoint = LocalPoint.fromScene(event.getParam0(), event.getParam1(), client.getTopLevelWorldView());
		WorldPoint worldPoint = WorldPoint.fromLocal(client, localPoint);

		pendingRock = worldPoint;
		log.info("[DRN-DEBUG] pendingRock set to {}", worldPoint);
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if (event.getActor() != client.getLocalPlayer())
		{
			return;
		}

		int animation = client.getLocalPlayer().getAnimation();

		if (pendingRock != null && animation == AnimationID.ARCEUUS_CHISEL_ESSENCE && !pendingRock.equals(activeRock))
		{
			activeRock = pendingRock;
			lastRockLocation = activeRock;
			log.info("[DRN-DEBUG] activeRock confirmed at {}", activeRock);
			showMiningNotification();
		}
		// Continuous chiselling alternates between a couple of non-idle animation IDs and
		// never dips to idle (-1) between swings, so an idle animation while we still think
		// we're active means the player stopped for a reason other than depletion - most
		// commonly a full inventory, but also walking off or manually cancelling.
		else if (activeRock != null && animation == -1)
		{
			log.info("[DRN-DEBUG] active rock at {} went idle -> stopped", activeRock);
			activeRock = null;
			pendingRock = null;
			showStoppedNotification();
		}
	}

	private boolean isMineableDenseRunestone(int id)
	{
		ObjectComposition comp = client.getObjectDefinition(id);
		if (comp == null)
		{
			return false;
		}

		if (comp.getImpostorIds() != null)
		{
			comp = comp.getImpostor();
		}

		if (comp == null || !TARGET_NAME.equalsIgnoreCase(comp.getName()))
		{
			return false;
		}

		String[] actions = comp.getActions();
		return actions != null && Arrays.stream(actions).anyMatch(a -> a != null && a.equalsIgnoreCase(MINE_ACTION));
	}

	private void showMiningNotification()
	{
		if (!config.enabled())
		{
			return;
		}

		overlayMessage = config.startMessage();
		overlayColor = config.startBoxColor();
		lastEventTime = Instant.now();
	}

	private void showStoppedNotification()
	{
		if (!config.enabled())
		{
			return;
		}

		overlayMessage = config.stoppedMessage();
		overlayColor = config.stoppedBoxColor();
		lastEventTime = Instant.now();

		if (config.notifyOnStop())
		{
			notifier.notify(config.stoppedMessage());
		}
	}

	@Provides
	DenseRunestoneNotifierConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DenseRunestoneNotifierConfig.class);
	}
}
