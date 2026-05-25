package com.runepal;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("runepal")
public interface RunePalConfig extends Config
{
	@ConfigItem(
		keyName = "syncEnabled",
		name = "Enable RunePal Sync",
		description = "Passively syncs your OSRS profile progression (skills, quests, music, collection log) to RunePal.com"
	)
	default boolean syncEnabled()
	{
		return true;
	}
}
