package com.runepal;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "RunePal",
	description = "Passively syncs your OSRS profile progression to RunePal.com",
	tags = {"runepal", "sync", "profile", "tracker"},
	enabledByDefault = false
)
public class RunePalPlugin extends Plugin
{
	@Inject private Client client;
	@Inject private ClientThread clientThread;
	@Inject private EventBus eventBus;
	@Inject private RunePalConfig config;
	@Inject private PlayerDataService playerDataService;
	@Inject private CollectionLogSubscriber collectionLogSubscriber;
	@Inject private RunePalApiClient apiClient;
	@Inject private SyncScheduler syncScheduler;

	@Provides
	RunePalConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RunePalConfig.class);
	}

	@Override
	protected void startUp()
	{
		eventBus.register(collectionLogSubscriber);
		syncScheduler.start(this::triggerSync);
		log.info("RunePal started");
	}

	@Override
	protected void shutDown()
	{
		eventBus.unregister(collectionLogSubscriber);
		syncScheduler.stop();
		collectionLogSubscriber.clear();
		log.info("RunePal stopped");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (!config.syncEnabled())
		{
			return;
		}
		switch (event.getGameState())
		{
			case LOGGED_IN:
				syncScheduler.scheduleImmediate();
				break;
			case LOGIN_SCREEN:
			case HOPPING:
				collectionLogSubscriber.clear();
				break;
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		if (config.syncEnabled())
		{
			syncScheduler.debounce();
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		// Catches quest completions and other progression state changes
		if (config.syncEnabled())
		{
			syncScheduler.debounce();
		}
	}

	// Called from SyncScheduler's background thread — bounces to client thread for safe Client reads.
	private void triggerSync()
	{
		clientThread.invoke(() ->
		{
			if (!config.syncEnabled())
			{
				return;
			}
			if (client.getGameState() != GameState.LOGGED_IN)
			{
				return;
			}
			if (client.getLocalPlayer() == null)
			{
				return;
			}

			String username = client.getLocalPlayer().getName();
			if (username == null || username.isEmpty())
			{
				return;
			}

			SyncPayload payload = playerDataService.buildPayload(
				username,
				collectionLogSubscriber.getItems()
			);
			apiClient.postAsync(payload);
		});
	}
}
