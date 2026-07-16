package com.denserunestone;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;

/**
 * A single line of text rendered above the chatbox, similar to plugins like GP/hr trackers.
 * Shows whichever notification (mining started / stopped) is currently active, with the box
 * background solid-filled in the state's color and the text in a separately configurable color.
 */
class DenseRunestoneNotifierOverlay extends OverlayPanel
{
	private final DenseRunestoneNotifierPlugin plugin;
	private final DenseRunestoneNotifierConfig config;

	@Inject
	private DenseRunestoneNotifierOverlay(DenseRunestoneNotifierPlugin plugin, DenseRunestoneNotifierConfig config)
	{
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		String message = plugin.getOverlayMessage();
		if (message == null)
		{
			return null;
		}

		panelComponent.setBackgroundColor(plugin.getOverlayColor());
		panelComponent.getChildren().add(LineComponent.builder()
			.left(message)
			.leftColor(config.textColor())
			.build());

		return super.render(graphics);
	}
}
