package com.runepal;

import com.google.gson.Gson;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
@Singleton
class RunePalApiClient
{
	private static final String SYNC_URL = "https://runepal.com/api/plugin/sync";
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	private static final String VERSION = "1.0";

	@Inject
	private OkHttpClient httpClient;

	@Inject
	private Gson gson;

	void postAsync(SyncPayload payload)
	{
		String json = gson.toJson(payload);
		Request request = new Request.Builder()
			.url(SYNC_URL)
			.header("User-Agent", "RunePal-Plugin/" + VERSION)
			.post(RequestBody.create(JSON, json))
			.build();

		httpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				log.warn("RunePal sync failed: {}", e.getMessage());
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				try (response)
				{
					if (response.isSuccessful())
					{
						log.debug("RunePal sync OK for {}", payload.username);
					}
					else
					{
						log.warn("RunePal sync HTTP {}", response.code());
					}
				}
			}
		});
	}
}
