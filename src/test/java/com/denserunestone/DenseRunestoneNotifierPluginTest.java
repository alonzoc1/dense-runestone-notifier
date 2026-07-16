package com.denserunestone;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class DenseRunestoneNotifierPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(DenseRunestoneNotifierPlugin.class);
		RuneLite.main(args);
	}
}
