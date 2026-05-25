package com.runepal;

import java.util.Map;

class SyncPayload
{
	final String username;
	final Map<String, Integer> skills;
	final Map<String, Integer> quests;
	final Map<Integer, Integer> music;
	final Map<Integer, Integer> collectionLog;
	final long timestamp;

	SyncPayload(
		String username,
		Map<String, Integer> skills,
		Map<String, Integer> quests,
		Map<Integer, Integer> music,
		Map<Integer, Integer> collectionLog,
		long timestamp
	)
	{
		this.username = username;
		this.skills = skills;
		this.quests = quests;
		this.music = music;
		this.collectionLog = collectionLog;
		this.timestamp = timestamp;
	}
}
