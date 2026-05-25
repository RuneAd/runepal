package com.runepal;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
class SyncScheduler
{
	private static final long PERIODIC_MINUTES = 5;
	private static final long DEBOUNCE_SECONDS = 15;
	private static final long IMMEDIATE_SECONDS = 3;

	private ScheduledExecutorService executor;
	private ScheduledFuture<?> periodicTask;
	private ScheduledFuture<?> debounceTask;
	private Runnable syncCallback;

	private final AtomicBoolean immediateQueued = new AtomicBoolean(false);

	void start(Runnable callback)
	{
		this.syncCallback = callback;
		executor = Executors.newSingleThreadScheduledExecutor(r ->
		{
			Thread t = new Thread(r, "runepal-sync");
			t.setDaemon(true);
			return t;
		});
		periodicTask = executor.scheduleAtFixedRate(
			this::run, PERIODIC_MINUTES, PERIODIC_MINUTES, TimeUnit.MINUTES
		);
	}

	void stop()
	{
		if (periodicTask != null)
		{
			periodicTask.cancel(false);
		}
		if (debounceTask != null)
		{
			debounceTask.cancel(false);
		}
		if (executor != null)
		{
			executor.shutdown();
		}
	}

	void scheduleImmediate()
	{
		if (executor == null || executor.isShutdown())
		{
			return;
		}
		if (immediateQueued.getAndSet(true))
		{
			return;
		}
		executor.schedule(() ->
		{
			immediateQueued.set(false);
			run();
		}, IMMEDIATE_SECONDS, TimeUnit.SECONDS);
	}

	void debounce()
	{
		if (executor == null || executor.isShutdown())
		{
			return;
		}
		if (debounceTask != null)
		{
			debounceTask.cancel(false);
		}
		debounceTask = executor.schedule(this::run, DEBOUNCE_SECONDS, TimeUnit.SECONDS);
	}

	private void run()
	{
		try
		{
			if (syncCallback != null)
			{
				syncCallback.run();
			}
		}
		catch (Exception e)
		{
			log.error("RunePal sync error", e);
		}
	}
}
