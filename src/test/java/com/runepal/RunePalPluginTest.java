package com.runepal;

import net.runelite.client.plugins.Plugin;
import org.junit.Assert;
import org.junit.Test;

public class RunePalPluginTest
{
	@Test
	public void testPluginExists()
	{
		Assert.assertTrue(Plugin.class.isAssignableFrom(RunePalPlugin.class));
	}
}
