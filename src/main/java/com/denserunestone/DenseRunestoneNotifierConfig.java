package com.denserunestone;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("denserunestonenotifier")
public interface DenseRunestoneNotifierConfig extends Config
{
	@ConfigItem(
		keyName = "enabled",
		name = "Enabled",
		description = "Show notifications when you start/stop mining a dense runestone",
		position = 0
	)
	default boolean enabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "textColor",
		name = "Text color",
		description = "Color of the notification text",
		position = 1
	)
	default Color textColor()
	{
		return Color.WHITE;
	}

	@ConfigItem(
		keyName = "startBoxColor",
		name = "Start box color",
		description = "Background color of the notification shown when you start mining a dense runestone",
		position = 2
	)
	default Color startBoxColor()
	{
		return new Color(0x00AA00);
	}

	@ConfigItem(
		keyName = "startMessage",
		name = "Start message",
		description = "Text shown when you start mining a dense runestone",
		position = 3
	)
	default String startMessage()
	{
		return "Mining dense essence";
	}

	@ConfigItem(
		keyName = "stoppedBoxColor",
		name = "Stopped box color",
		description = "Background color of the notification shown when you're no longer mining (depleted, inventory full, etc.)",
		position = 4
	)
	default Color stoppedBoxColor()
	{
		return new Color(0xAD0000);
	}

	@ConfigItem(
		keyName = "stoppedMessage",
		name = "Stopped message",
		description = "Text shown when you're no longer mining (depleted, inventory full, etc.)",
		position = 5
	)
	default String stoppedMessage()
	{
		return "Not mining dense essence";
	}

	@ConfigItem(
		keyName = "notifyOnStop",
		name = "Notify when you stop mining",
		description = "Send a RuneLite notification (desktop notification, per your RuneLite notification settings) when you stop mining a dense runestone",
		position = 6
	)
	default boolean notifyOnStop()
	{
		return true;
	}
}
