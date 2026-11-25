/*
Author: DenySilver (Reforged Pixel Dungeon)
License: GPLv3
*/

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonValue;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.online.OnlineApiClient;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;

import java.net.URLEncoder;
import java.text.NumberFormat;

public class WndOnlineProfile extends Window {

    private static final int WIDTH = 120;

    private final String username;
    private final boolean isOwnProfile;
    private final Runnable onLogoutCallback;

    private Group content;
    private IconTitle title;
    private RenderedTextBlock statusText;

    public WndOnlineProfile(String username, Runnable onLogoutCallback) {
        super();
        this.username = username;
        this.onLogoutCallback = onLogoutCallback;
        this.isOwnProfile = SPDSettings.isOnlineLoggedIn()
                && username.equalsIgnoreCase(SPDSettings.onlineUsername());

        content = new Group();
        add(content);

        title = new IconTitle(Icons.RANKINGS.get(), username);
        title.color(isOwnProfile ? 0x44DDFF : TITLE_COLOR);
        title.setRect(0, 0, WIDTH, 0);
        content.add(title);

        statusText = PixelScene.renderTextBlock(Messages.get(this, "loading"), 7);
        statusText.hardlight(0xCCCCCC);
        statusText.setPos((WIDTH - statusText.width()) / 2f, title.bottom() + 14);
        content.add(statusText);

        float bottom = statusText.bottom() + 12;
        if (isOwnProfile) {
            bottom = addLogoutButton(bottom);
        }
        resize(WIDTH, (int) bottom + 4);

        loadProfile();
    }

    private void loadProfile() {
        String encoded;
        try {
            encoded = URLEncoder.encode(username, "UTF-8");
        } catch (Exception e) {
            encoded = username;
        }

        OnlineApiClient.get("/users/" + encoded + "/profile", new OnlineApiClient.ApiCallback() {
            @Override
            public void onSuccess(int statusCode, JsonValue body) {
                Gdx.app.postRunnable(() -> {
                    if (parent == null) return;
                    renderStats(body);
                });
            }

            @Override
            public void onError(int statusCode, String errorMessage) {
                Gdx.app.postRunnable(() -> {
                    if (parent == null) return;
                    renderError(errorMessage);
                });
            }
        });
    }

    private void renderError(String errorMessage) {
        content.clear();

        title = new IconTitle(Icons.RANKINGS.get(), username);
        title.color(isOwnProfile ? 0x44DDFF : TITLE_COLOR);
        title.setRect(0, 0, WIDTH, 0);
        content.add(title);

        statusText = PixelScene.renderTextBlock(7);
        statusText.maxWidth(WIDTH - 4);
        statusText.text(Messages.get(this, "load_error", errorMessage));
        statusText.hardlight(0xFF5555);
        statusText.setPos((WIDTH - statusText.width()) / 2f, title.bottom() + 10);
        content.add(statusText);

        float bottom = statusText.bottom() + 12;
        if (isOwnProfile) {
            bottom = addLogoutButton(bottom);
        }
        resize(WIDTH, (int) bottom + 4);
    }

    private void renderStats(JsonValue data) {
        content.clear();

        String favClassStr = data.getString("favoriteHeroClass", "NONE");
        HeroClass favClass = null;
        if (favClassStr != null && !"NONE".equals(favClassStr)) {
            try {
                favClass = HeroClass.valueOf(favClassStr);
            } catch (Exception ignored) {
            }
        }

        Image avatar = favClass != null
                ? HeroSprite.avatar(favClass, 1)
                : Icons.RANKINGS.get();

        title = new IconTitle(avatar, data.getString("username", username));
        title.color(isOwnProfile ? 0x44DDFF : TITLE_COLOR);
        title.setRect(0, 0, WIDTH, 0);
        content.add(title);

        float pos = title.bottom() + 3;

        String joinedAt = data.getString("joinedAt", "");
        if (joinedAt.length() >= 10) {
            RenderedTextBlock regDate = PixelScene.renderTextBlock(Messages.get(this, "joined_at", joinedAt.substring(0, 10)), 6);
            regDate.hardlight(0xAAAAAA);
            regDate.setPos(0, pos);
            content.add(regDate);
            pos = regDate.bottom() + 5;
        }

        NumberFormat num = NumberFormat.getInstance(Messages.locale());

        long totalRuns = data.getLong("totalRuns", 0);
        long wins = data.getLong("wins", 0);
        long ascensions = data.getLong("ascensions", 0);
        int bestScore = data.getInt("bestScore", 0);

        pos = statSlot(Messages.get(this, "total_runs"), num.format(totalRuns), pos);
        pos = statSlot(Messages.get(this, "wins"), num.format(wins), pos);
        pos = statSlot(Messages.get(this, "ascensions"), num.format(ascensions), pos);
        pos = statSlot(Messages.get(this, "best_score"), num.format(bestScore), pos);

        if (favClass != null) {
            pos = statSlot(Messages.get(this, "fav_class"), Messages.titleCase(favClass.title()), pos);
        }

        JsonValue recentRuns = data.get("recentRuns");
        if (recentRuns != null && recentRuns.isArray() && recentRuns.size > 0) {
            pos += 2;
            ColorBlock sep = new ColorBlock(WIDTH, 1, 0xFF333333);
            sep.x = 0;
            sep.y = pos;
            content.add(sep);
            pos += 4;

            RenderedTextBlock recentHeader = PixelScene.renderTextBlock(Messages.get(this, "recent_runs"), 6);
            recentHeader.hardlight(TITLE_COLOR);
            recentHeader.setPos(0, pos);
            content.add(recentHeader);
            pos = recentHeader.bottom() + 3;

            int shown = 0;
            for (JsonValue r : recentRuns) {
                if (shown >= 4) break;
                pos = addRecentRunRow(r, num, pos);
                shown++;
            }
        }

        pos += 4;
        if (isOwnProfile) {
            pos = addLogoutButton(pos);
        }

        resize(WIDTH, (int) pos + 2);
    }

    private float addRecentRunRow(JsonValue r, NumberFormat num, float pos) {
        String clsStr = r.getString("heroClass", "WARRIOR");
        HeroClass cls;
        try {
            cls = HeroClass.valueOf(clsStr);
        } catch (Exception e) {
            cls = HeroClass.WARRIOR;
        }

        int floor = r.getInt("deepestFloor", 1);
        int score = r.getInt("score", 0);
        boolean win = r.getBoolean("win", false);
        boolean ascended = r.getBoolean("ascended", false);

        String status = ascended
                ? Messages.get(this, "status_ascended")
                : (win ? Messages.get(this, "status_victory") : Messages.get(this, "status_floor", floor));
        String leftText = Messages.titleCase(cls.title()) + " • " + status;

        RenderedTextBlock left = PixelScene.renderTextBlock(leftText, 6);
        left.hardlight(win ? 0xFFFF44 : 0xCCCCCC);
        left.setPos(0, pos);
        content.add(left);

        RenderedTextBlock right = PixelScene.renderTextBlock(num.format(score), 6);
        right.hardlight(0xFFFFFF);
        right.setPos(WIDTH - right.width(), pos);
        content.add(right);

        return pos + Math.max(left.height(), right.height()) + 2;
    }

    private float addLogoutButton(float top) {
        RedButton btnLogout = new RedButton(Messages.get(this, "logout")) {
            @Override
            protected void onClick() {
                SPDSettings.onlineLogout();
                hide();
                if (onLogoutCallback != null) {
                    onLogoutCallback.run();
                }
            }
        };
        btnLogout.setRect(0, top, WIDTH, 16);
        content.add(btnLogout);
        return btnLogout.bottom() + 2;
    }

    private float statSlot(String label, String value, float pos) {
        int size = 7;
        RenderedTextBlock lbl;
        do {
            lbl = PixelScene.renderTextBlock(label, size);
            size--;
        } while (lbl.width() >= WIDTH * 0.62f && size > 4);
        lbl.setPos(0, pos);
        PixelScene.align(lbl);
        content.add(lbl);

        size = 7;
        RenderedTextBlock val;
        do {
            val = PixelScene.renderTextBlock(value, size);
            size--;
        } while (val.width() >= WIDTH * 0.38f && size > 4);
        val.setPos(WIDTH - val.width(), pos);
        PixelScene.align(val);
        content.add(val);

        return pos + Math.max(lbl.height(), val.height()) + 3;
    }
}