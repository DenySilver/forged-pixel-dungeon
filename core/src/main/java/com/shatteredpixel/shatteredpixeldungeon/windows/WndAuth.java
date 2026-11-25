/*
 * Author: DenySilver (Forged Pixel Dungeon)
 * License: GPLv3
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.badlogic.gdx.utils.JsonValue;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.online.OnlineApiClient;
import com.shatteredpixel.shatteredpixeldungeon.online.OnlineRunSubmitter;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

public class WndAuth extends Window {

    private static final int WIDTH = 120;

    private String username = "";
    private String password = "";
    private final Runnable onSuccessCallback;

    private RenderedTextBlock statusText;
    private RedButton btnUsername;
    private RedButton btnPassword;

    public WndAuth(Runnable onSuccessCallback) {
        super();
        this.onSuccessCallback = onSuccessCallback;

        RenderedTextBlock title = PixelScene.renderTextBlock(Messages.get(this, "title"), 9);
        title.hardlight(TITLE_COLOR);
        title.setPos((WIDTH - title.width()) / 2f, 4);
        add(title);

        float pos = title.bottom() + 6;

        btnUsername = new RedButton(Messages.get(this, "username_empty")) {
            @Override
            protected void onClick() {
                parent.add(new WndTextInput(
                        Messages.get(WndAuth.class, "username_title"),
                        Messages.get(WndAuth.class, "username_desc"),
                        username, 24, false,
                        Messages.get(WndAuth.class, "ok"),
                        Messages.get(WndAuth.class, "cancel")
                ) {
                    @Override
                    public void onSelect(boolean positive, String text) {
                        if (positive && text != null) {
                            username = text.trim();
                            btnUsername.text(username.isEmpty()
                                    ? Messages.get(WndAuth.class, "username_empty")
                                    : Messages.get(WndAuth.class, "username_value", username));
                        }
                    }
                });
            }
        };
        btnUsername.setRect(2, pos, WIDTH - 4, 18);
        add(btnUsername);
        pos = btnUsername.bottom() + 4;

        btnPassword = new RedButton(Messages.get(this, "password_empty")) {
            @Override
            protected void onClick() {
                parent.add(new WndTextInput(
                        Messages.get(WndAuth.class, "password_title"),
                        Messages.get(WndAuth.class, "password_desc"),
                        "", 64, false,
                        Messages.get(WndAuth.class, "ok"),
                        Messages.get(WndAuth.class, "cancel")
                ) {
                    @Override
                    public void onSelect(boolean positive, String text) {
                        if (positive && text != null) {
                            password = text;
                            btnPassword.text(password.isEmpty()
                                    ? Messages.get(WndAuth.class, "password_empty")
                                    : Messages.get(WndAuth.class, "password_filled"));
                        }
                    }
                });
            }
        };
        btnPassword.setRect(2, pos, WIDTH - 4, 18);
        add(btnPassword);
        pos = btnPassword.bottom() + 6;

        statusText = PixelScene.renderTextBlock("", 6);
        statusText.maxWidth(WIDTH - 4);
        statusText.setPos(2, pos);
        add(statusText);
        pos += 14;

        RedButton btnLogin = new RedButton(Messages.get(this, "login")) {
            @Override
            protected void onClick() {
                sendAuth("/auth/login");
            }
        };
        btnLogin.setRect(2, pos, (WIDTH - 6) / 2f, 18);
        add(btnLogin);

        RedButton btnRegister = new RedButton(Messages.get(this, "register")) {
            @Override
            protected void onClick() {
                sendAuth("/auth/register");
            }
        };
        btnRegister.setRect(btnLogin.right() + 2, pos, (WIDTH - 6) / 2f, 18);
        add(btnRegister);

        resize(WIDTH, (int) btnRegister.bottom() + 4);
    }

    private void sendAuth(String endpoint) {
        if (username.length() < 3 || password.length() < 6) {
            statusText.text(Messages.get(this, "validation_error"));
            statusText.hardlight(0xFF4444);
            return;
        }

        statusText.text(Messages.get(this, "connecting"));
        statusText.hardlight(0xCCCCCC);

        String body = "{\"username\":\"" + escapeJson(username) + "\",\"password\":\"" + escapeJson(password) + "\"}";
        OnlineApiClient.post(endpoint, body, new OnlineApiClient.ApiCallback() {
            @Override
            public void onSuccess(int statusCode, JsonValue json) {
                SPDSettings.onlineToken(json.getString("token"));
                SPDSettings.onlineUsername(json.getString("username"));
                OnlineRunSubmitter.syncLocalRecords();
                OnlineRunSubmitter.flushPendingRuns();
                hide();
                if (onSuccessCallback != null) {
                    onSuccessCallback.run();
                }
            }

            @Override
            public void onError(int statusCode, String errorMessage) {
                statusText.text(errorMessage);
                statusText.hardlight(0xFF4444);
            }
        });
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}