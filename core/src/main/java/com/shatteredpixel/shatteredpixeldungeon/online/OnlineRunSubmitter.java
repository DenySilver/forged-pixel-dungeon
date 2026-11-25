/*
 * Author: DenySilver (Reforged Pixel Dungeon)
 * License: GPLv3
 */

package com.shatteredpixel.shatteredpixeldungeon.online;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.watabou.noosa.Game;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class OnlineRunSubmitter {

    public static void submitCurrentRun(boolean win, Object cause) {
        System.out.println("DEBUG submitCurrentRun: loggedIn=" + SPDSettings.isOnlineLoggedIn()
                + ", heroNull=" + (Dungeon.hero == null)
                + ", customSeed='" + Dungeon.customSeedText + "'");
        if (!SPDSettings.isOnlineLoggedIn() || Dungeon.hero == null) {
            return;
        }

        try {
            String payload = buildRunJson(win, cause, UUID.randomUUID().toString(), Game.version);
            com.badlogic.gdx.Gdx.app.log("OnlineRunSubmitter", "Enqueue run: " + payload);
            enqueuePayload(payload);
            flushPendingRuns();
        } catch (Exception e) {
            com.badlogic.gdx.Gdx.app.error("OnlineRunSubmitter", "Build/enqueue failed", e);
        }
    }

    private static String buildRunJson(boolean win, Object cause, String runUuid, String gameVersion) throws Exception {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        jw.setOutputType(JsonWriter.OutputType.json);

        boolean isDaily = Dungeon.daily;
        String dailyDate = null;
        if (isDaily) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            dailyDate = sdf.format(new Date());
        }

        String subclass = (Dungeon.hero.subClass != null)
                ? Dungeon.hero.subClass.name()
                : "NONE";

        Class<?> causeClass = null;
        if (cause != null) {
            causeClass = (cause instanceof Class) ? (Class<?>) cause : cause.getClass();
        }

        String causeStr = win
                ? (Statistics.ascended ? "ASCENDED" : "VICTORY")
                : (causeClass != null ? causeClass.getName() : "UNKNOWN");

        jw.object();
        jw.set("runUuid", runUuid);
        jw.set("gameVersion", gameVersion);
        jw.set("mode", isDaily ? "DAILY" : "STANDARD");
        if (dailyDate != null) {
            jw.set("dailyDate", dailyDate);
        }
        long validSeed = Dungeon.seed;
        if (validSeed >= DungeonSeed.TOTAL_SEEDS) {
            validSeed -= DungeonSeed.TOTAL_SEEDS;
        }
        if (validSeed < 0) {
            validSeed = 0;
        }
        jw.set("seedCode", DungeonSeed.convertToCode(validSeed));
        jw.set("challengesMask", Dungeon.challenges);
        jw.set("challengesCount", Challenges.activeChallenges());
        jw.set("heroClass", Dungeon.hero.heroClass.name());
        jw.set("heroSubclass", subclass);
        jw.set("heroLevel", Dungeon.hero.lvl);
        jw.set("deepestFloor", Statistics.deepestFloor);
        jw.set("turnsPassed", (int) Statistics.duration);
        jw.set("win", win);
        jw.set("ascended", Statistics.ascended);
        jw.set("causeOfDeath", causeStr);
        jw.set("score", Statistics.totalScore);

        Belongings b = Dungeon.hero.belongings;
        jw.object("buildSnapshot");
        jw.set("armorTier", Dungeon.hero.tier());
        jw.set("str", Dungeon.hero.STR);
        jw.set("enemiesSlain", Statistics.enemiesSlain);
        jw.set("goldCollected", Statistics.goldCollected);
        jw.set("foodEaten", Statistics.foodEaten);
        jw.set("itemsCrafted", Statistics.itemsCrafted);
        writeItem(jw, "weapon", b.weapon());
        writeItem(jw, "armor", b.armor());
        writeItem(jw, "artifact", b.artifact());
        writeItem(jw, "misc", b.misc());
        writeItem(jw, "ring", b.ring());
        writeItem(jw, "trinket", b.getItem(com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket.class));
        jw.pop();

        jw.pop();
        jw.flush();
        return sw.toString();
    }

    private static void writeItem(JsonWriter jw, String key, Item item) throws Exception {
        if (item == null) return;
        jw.object(key);
        jw.set("className", item.getClass().getName());
        jw.set("name", item.trueName());
        jw.set("level", item.level());
        jw.set("cursed", item.cursed);
        jw.pop();
    }

    private static synchronized void enqueuePayload(String payloadJson) {
        JsonValue arr = new JsonReader().parse(SPDSettings.pendingOnlineRuns());
        StringBuilder sb = new StringBuilder("[");
        if (arr != null && arr.isArray()) {
            for (int i = 0; i < arr.size; i++) {
                if (i > 0) sb.append(",");
                sb.append(arr.get(i).toJson(JsonWriter.OutputType.json));
            }
            if (arr.size > 0) sb.append(",");
        }
        sb.append(payloadJson).append("]");
        SPDSettings.pendingOnlineRuns(sb.toString());
    }

    public static synchronized void flushPendingRuns() {
        if (!SPDSettings.isOnlineLoggedIn()) return;

        JsonValue arr = new JsonReader().parse(SPDSettings.pendingOnlineRuns());
        if (arr == null || !arr.isArray() || arr.size == 0) return;

        String firstPayload = arr.get(0).toJson(JsonWriter.OutputType.json);
        OnlineApiClient.post("/runs", firstPayload, new OnlineApiClient.ApiCallback() {
            @Override
            public void onSuccess(int statusCode, JsonValue body) {
                removeFirstFromQueue();
                flushPendingRuns();
            }

            @Override
            public void onError(int statusCode, String errorMessage) {
                com.badlogic.gdx.Gdx.app.error("OnlineRunSubmitter", "POST /runs error " + statusCode + ": " + errorMessage);
                if (statusCode == 400 || statusCode == 409) {
                    removeFirstFromQueue();
                    flushPendingRuns();
                }
            }
        });
    }

    private static synchronized void removeFirstFromQueue() {
        JsonValue arr = new JsonReader().parse(SPDSettings.pendingOnlineRuns());
        if (arr == null || !arr.isArray() || arr.size <= 1) {
            SPDSettings.pendingOnlineRuns("[]");
            return;
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 1; i < arr.size; i++) {
            if (i > 1) sb.append(",");
            sb.append(arr.get(i).toJson(JsonWriter.OutputType.json));
        }
        sb.append("]");
        SPDSettings.pendingOnlineRuns(sb.toString());
    }

    public static void syncLocalRecords() {
        if (!SPDSettings.isOnlineLoggedIn()) return;

        try {
            Rankings.INSTANCE.load();
            for (Rankings.Record rec : Rankings.INSTANCE.records) {
                if (rec.daily || (rec.customSeed != null && !rec.customSeed.isEmpty())) {
                    continue;
                }
                if (rec.gameID == null || rec.gameID.isEmpty()) {
                    continue;
                }

                try {
                    Rankings.INSTANCE.loadGameData(rec);
                    if (Dungeon.hero == null) continue;

                    String runUuid = UUID.nameUUIDFromBytes(
                            rec.gameID.getBytes(java.nio.charset.StandardCharsets.UTF_8)
                    ).toString();

                    String version = (rec.version != null && !rec.version.isEmpty())
                            ? rec.version
                            : Game.version;

                    String payload = buildRunJson(rec.win, rec.cause, runUuid, version);
                    enqueuePayload(payload);
                } catch (Exception ignored) {
                }
            }
            Dungeon.hero = null;
            flushPendingRuns();
        } catch (Exception ignored) {
        }
    }
}