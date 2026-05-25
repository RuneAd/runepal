# RunePal

A lightweight passive sync plugin for [RunePal.com](https://runepal.com).

## What it does

Periodically syncs your OSRS profile progression to RunePal.com:

- **Skills** — XP for all skills
- **Quests** — completion state for all quests
- **Music** — track unlock progress
- **Collection Log** — item counts as you receive them

## What it does NOT do

- No automation or bot-like behaviour
- No login credentials or session data collected
- No inventory, bank, or trade data
- No world or clan tracking
- No overlays or UI panels
- No hidden telemetry

## Configuration

One toggle: **Enable RunePal Sync** (off by default).

## Sync triggers

- On login
- On level up (debounced 15 s to avoid spam)
- On quest completion
- On collection log item received
- Every 5 minutes passively

## Privacy

Only publicly visible player progression data is synced. See [RunePal.com/privacy](https://runepal.com/privacy) for the full policy.
