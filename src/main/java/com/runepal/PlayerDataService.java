package com.runepal;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;

@Singleton
class PlayerDataService
{
	// Music track unlocks are encoded as bitfields across these VarPlayers.
	// Verify range against VarPlayer.java in the current RuneLite client source.
	private static final int MUSIC_VARP_START = 1353;
	private static final int MUSIC_VARP_END = 1465;

	@Inject
	private Client client;

	SyncPayload buildPayload(String username, Map<Integer, Integer> collectionLog)
	{
		return new SyncPayload(
			username,
			getSkills(),
			getQuests(),
			getMusicVarps(),
			new HashMap<>(collectionLog),
			Instant.now().getEpochSecond()
		);
	}

	private Map<String, Integer> getSkills()
	{
		Map<String, Integer> skills = new HashMap<>();
		for (Skill skill : Skill.values())
		{
			if (skill == Skill.OVERALL)
			{
				continue;
			}
			skills.put(skill.getName(), client.getSkillExperience(skill));
		}
		return skills;
	}

	private Map<String, Integer> getQuests()
	{
		Map<String, Integer> quests = new HashMap<>();
		for (Quest quest : Quest.values())
		{
			QuestState state = quest.getState(client);
			// 0 = NOT_STARTED, 1 = IN_PROGRESS, 2 = FINISHED
			quests.put(String.valueOf(quest.getId()), state.ordinal());
		}
		return quests;
	}

	// Sends raw varp bitfields; the RunePal server decodes which tracks are unlocked.
	// Only non-zero varps are sent to keep payloads small.
	private Map<Integer, Integer> getMusicVarps()
	{
		Map<Integer, Integer> music = new HashMap<>();
		for (int varp = MUSIC_VARP_START; varp <= MUSIC_VARP_END; varp++)
		{
			int value = client.getVarpValue(varp);
			if (value != 0)
			{
				music.put(varp, value);
			}
		}
		return music;
	}
}
