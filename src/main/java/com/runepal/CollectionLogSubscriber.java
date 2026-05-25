package com.runepal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@Singleton
class CollectionLogSubscriber
{
	// Fired by OSRS when a new collection log item is received (delayed transmit).
	// Verify against ScriptID.COLLECTION_LOG_ITEM_DELAY_TRANSMIT in the RuneLite client source.
	private static final int SCRIPT_COLLECTION_LOG_ITEM_DELAY_TRANSMIT = 2726;

	@Inject
	private Client client;

	private final Map<Integer, Integer> items = new HashMap<>();

	@Subscribe
	public void onScriptPreFired(ScriptPreFired event)
	{
		if (event.getScriptId() != SCRIPT_COLLECTION_LOG_ITEM_DELAY_TRANSMIT)
		{
			return;
		}

		// CS2 args sit at the top of the int stack before the script executes.
		// Stack layout: [..., itemId, quantity] (quantity at top)
		final int[] intStack = client.getIntStack();
		final int size = client.getIntStackSize();
		if (size < 2)
		{
			return;
		}

		int itemId = intStack[size - 2];
		int quantity = intStack[size - 1];

		if (itemId > 0 && quantity >= 0)
		{
			items.merge(itemId, quantity, Integer::max);
			log.debug("Collection log update — item {}: {}", itemId, quantity);
		}
	}

	Map<Integer, Integer> getItems()
	{
		return Collections.unmodifiableMap(items);
	}

	void clear()
	{
		items.clear();
	}
}
