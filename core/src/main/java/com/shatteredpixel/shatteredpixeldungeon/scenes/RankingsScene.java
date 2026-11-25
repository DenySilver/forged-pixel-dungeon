/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * Reforged Pixel Dungeon
 * Copyright (C) 2026-2026 Denys Zamaruiev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.badlogic.gdx.utils.JsonValue;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Rankings;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.online.OnlineApiClient;
import com.shatteredpixel.shatteredpixeldungeon.online.OnlineRunSubmitter;
import com.shatteredpixel.shatteredpixeldungeon.windows.*;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.Button;
import com.shatteredpixel.shatteredpixeldungeon.ui.ExitButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.TitleBackground;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Music;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.GameMath;
import com.watabou.utils.RectF;

public class RankingsScene extends PixelScene {

	private static final float ROW_HEIGHT_MAX	= 20;
	private static final float ROW_HEIGHT_MIN	= 12;

	private static final float MAX_ROW_WIDTH    = 160;

	private static final float GAP	= 4;

	private Group localGroup;
	private Group onlineGroup;
	private Group onlineRowsGroup;

	private IconTitle title;
	private IconButton btnOnlineToggle;
	private RedButton btnOnlineMode;
	private RedButton btnChallenges;
	private RedButton btnAccount;
	private RenderedTextBlock onlineStatusLabel;
	private RenderedTextBlock onlineUserRankLabel;

	private boolean onlineActive = false;
	private boolean onlineDaily = false;
	private int selectedChallenges = 0;
	private int requestGeneration = 0;

	private float contentWidth;
	private float contentHeight;
	private RectF screenInsets;

	@Override
	public void create() {

		super.create();

		Music.INSTANCE.playTracks(
				new String[]{Assets.Music.THEME_1, Assets.Music.THEME_2},
				new float[]{1, 1},
				false);

		uiCamera.visible = false;

		int w = Camera.main.width;
		int h = Camera.main.height;
		screenInsets = getCommonInsets();

		TitleBackground BG = new TitleBackground(w, h);
		add( BG );

		w -= screenInsets.left + screenInsets.right;
		h -= screenInsets.top + screenInsets.bottom;
		contentWidth = w;
		contentHeight = h;

		Rankings.INSTANCE.load();

		title = new IconTitle( Icons.RANKINGS.get(), Messages.get(this, "title"));
		title.setSize(200, 0);
		title.setPos(
				screenInsets.left + (w - title.reqWidth()) / 2f,
				screenInsets.top + (20 - title.height()) / 2f
		);
		align(title);
		add(title);

		localGroup = new Group();
		add(localGroup);

		if (Rankings.INSTANCE.records.size() > 0) {

			float rowHeight = GameMath.gate(ROW_HEIGHT_MIN, (h - 26)/Rankings.INSTANCE.records.size(), ROW_HEIGHT_MAX);

			float left = (w - Math.min( MAX_ROW_WIDTH, w )) / 2 + GAP;
			float top = (h - rowHeight * Rankings.INSTANCE.records.size()) / 2;

			int pos = 0;

			for (Rankings.Record rec : Rankings.INSTANCE.records) {
				Record row = new Record( pos, pos == Rankings.INSTANCE.lastRecord, rec );
				float offset = 0;
				if (rowHeight <= 14){
					offset = (pos % 2 == 1) ? 5 : -5;
				}
				row.setRect( screenInsets.left + left+offset, screenInsets.top + top + pos * rowHeight, w - left * 2, rowHeight );
				localGroup.add(row);

				pos++;
			}

			if (Rankings.INSTANCE.totalNumber >= Rankings.TABLE_SIZE) {

				RenderedTextBlock label = PixelScene.renderTextBlock( 8 );
				label.hardlight( 0xCCCCCC );
				label.setHightlighting(true, Window.SHPX_COLOR);
				label.text( Messages.get(this, "total") + " _" + Rankings.INSTANCE.wonNumber + "_/" + Rankings.INSTANCE.totalNumber );
				localGroup.add( label );

				label.setPos(
						screenInsets.left + (w - label.width()) / 2,
						screenInsets.top + h - label.height() - 2*GAP
				);
				align(label);

			}

		} else {

			RenderedTextBlock noRec = PixelScene.renderTextBlock(Messages.get(this, "no_games"), 8);
			noRec.hardlight( 0xCCCCCC );
			noRec.setPos(
					screenInsets.left + (w - noRec.width()) / 2,
					screenInsets.top + (h - noRec.height()) / 2
			);
			align(noRec);
			localGroup.add(noRec);

		}

		buildOnlineGroup();

		ExitButton btnExit = new ExitButton();
		btnExit.setPos( Camera.main.width - btnExit.width() - screenInsets.right, screenInsets.top );
		add( btnExit );

		float left = screenInsets.left + (PixelScene.landscape() && !DeviceCompat.isDesktop() ? 10 : 0);

		if (Rankings.INSTANCE.latestDaily != null) {
			IconButton btnDailies = new IconButton(Icons.CALENDAR.get()) {
				@Override
				protected void onClick() {
					ShatteredPixelDungeon.scene().addToFront(new WndDailies());
				}

				@Override
				protected void onPointerUp() {
					icon.hardlight(0.5f, 1f, 2f);
				}
			};
			btnDailies.icon().hardlight(0.5f, 1f, 2f);
			btnDailies.setRect( left, screenInsets.top, 16, 20 );
			left += 16;
			add(btnDailies);
		}

		if (!OnlineApiClient.BASE_URL.isEmpty()) {
			btnOnlineToggle = new IconButton(Icons.RANKINGS.get()) {
				@Override
				protected void onClick() {
					setOnlineMode(!onlineActive);
				}

				@Override
				protected void onPointerUp() {
					updateToggleTint();
				}
			};
			btnOnlineToggle.setRect( left, screenInsets.top, 16, 20 );
			updateToggleTint();
			add(btnOnlineToggle);
		}

		setOnlineMode(false);

		if (Dungeon.daily){
			addToFront(new WndDailies());
		} else if (Badges.isUnlocked(Badges.Badge.VICTORY) && !SPDSettings.victoryNagged()) {
			SPDSettings.victoryNagged(true);
			add(new WndVictoryCongrats());
		}

		fadeIn();
	}

	private void buildOnlineGroup() {
		onlineGroup = new Group();
		add(onlineGroup);

		float barWidth = Math.min(MAX_ROW_WIDTH, contentWidth - 4);
		float barLeft = screenInsets.left + (contentWidth - barWidth) / 2f;
		float barTop = screenInsets.top + 21;

		float modeW = 44;
		float chalW = 42;
		float accW = barWidth - modeW - chalW - 4;

		btnOnlineMode = new RedButton(Messages.get(this, "online_mode_standard")) {
			@Override
			protected void onClick() {
				onlineDaily = !onlineDaily;
				btnOnlineMode.text(Messages.get(RankingsScene.this, onlineDaily ? "online_mode_daily" : "online_mode_standard"));
				loadOnlineLeaderboard();
			}
		};
		btnOnlineMode.setRect(barLeft, barTop, modeW, 15);
		onlineGroup.add(btnOnlineMode);

		btnChallenges = new RedButton(Messages.get(this, "online_challenges", selectedChallenges)) {
			@Override
			protected void onClick() {
				selectedChallenges = (selectedChallenges + 1) % 10;
				btnChallenges.text(Messages.get(RankingsScene.this, "online_challenges", selectedChallenges));
				loadOnlineLeaderboard();
			}
		};
		btnChallenges.setRect(btnOnlineMode.right() + 2, barTop, chalW, 15);
		onlineGroup.add(btnChallenges);

		btnAccount = new RedButton("") {
			@Override
			protected void onClick() {
				if (!SPDSettings.isOnlineLoggedIn()) {
					ShatteredPixelDungeon.scene().addToFront(new WndAuth(() -> {
						updateAccountButton();
						loadOnlineLeaderboard();
					}));
				} else {
					ShatteredPixelDungeon.scene().addToFront(new WndOnlineProfile(
							SPDSettings.onlineUsername(),
							() -> {
								updateAccountButton();
								loadOnlineLeaderboard();
							}
					));
				}
			}
		};
		btnAccount.setRect(btnChallenges.right() + 2, barTop, accW, 15);
		onlineGroup.add(btnAccount);
		updateAccountButton();

		onlineRowsGroup = new Group();
		onlineGroup.add(onlineRowsGroup);

		onlineStatusLabel = PixelScene.renderTextBlock("", 8);
		onlineStatusLabel.maxWidth((int) barWidth);
		onlineGroup.add(onlineStatusLabel);

		onlineUserRankLabel = PixelScene.renderTextBlock("", 7);
		onlineUserRankLabel.hardlight(Window.SHPX_COLOR);
		onlineGroup.add(onlineUserRankLabel);
	}

	private void updateAccountButton() {
		if (SPDSettings.isOnlineLoggedIn()) {
			String name = SPDSettings.onlineUsername();
			if (name.length() > 10) {
				name = name.substring(0, 9) + "…";
			}
			btnAccount.text(name);
		} else {
			btnAccount.text(Messages.get(this, "online_login"));
		}
	}

	private void updateToggleTint() {
		if (btnOnlineToggle == null) return;
		if (onlineActive) {
			btnOnlineToggle.icon().hardlight(0.3f, 1.4f, 2f);
		} else {
			btnOnlineToggle.icon().resetColor();
		}
	}

	private void setOnlineMode(boolean online) {
		this.onlineActive = online;
		localGroup.visible = localGroup.active = !online;
		onlineGroup.visible = onlineGroup.active = online;
		updateToggleTint();

		title.label(online ? Messages.get(this, "online_title") : Messages.get(this, "title"));
		title.setPos(
				screenInsets.left + (contentWidth - title.reqWidth()) / 2f,
				screenInsets.top + (20 - title.height()) / 2f
		);
		align(title);

		if (online) {
			updateAccountButton();
			OnlineRunSubmitter.flushPendingRuns();
			loadOnlineLeaderboard();
		}
	}

	private void showOnlineStatus(String text, int color) {
		onlineRowsGroup.clear();
		onlineUserRankLabel.visible = false;
		onlineStatusLabel.visible = true;
		onlineStatusLabel.text(text);
		onlineStatusLabel.hardlight(color);
		onlineStatusLabel.setPos(
				screenInsets.left + (contentWidth - onlineStatusLabel.width()) / 2f,
				screenInsets.top + (contentHeight - onlineStatusLabel.height()) / 2f + 8
		);
		align(onlineStatusLabel);
	}

	private void loadOnlineLeaderboard() {
		final int gen = ++requestGeneration;
		showOnlineStatus(Messages.get(this, "online_loading"), 0xCCCCCC);

		String path = onlineDaily
				? "/leaderboard/daily?challenges=" + selectedChallenges + "&limit=10"
				: "/leaderboard/standard?challenges=" + selectedChallenges + "&limit=10";

		OnlineApiClient.get(path, new OnlineApiClient.ApiCallback() {
			@Override
			public void onSuccess(int statusCode, JsonValue body) {
				if (!onlineActive || gen != requestGeneration) return;
				renderOnlineLeaderboard(body);
			}

			@Override
			public void onError(int statusCode, String errorMessage) {
				if (!onlineActive || gen != requestGeneration) return;
				showOnlineStatus(Messages.get(RankingsScene.this, "online_net_error", errorMessage), 0xFF5555);
			}
		});
	}

	private void renderOnlineLeaderboard(JsonValue body) {
		onlineRowsGroup.clear();
		updateAccountButton();

		JsonValue entries = (body != null) ? body.get("entries") : null;
		if (entries == null || !entries.isArray() || entries.size == 0) {
			showOnlineStatus(Messages.get(this, "online_empty"), 0xCCCCCC);
			return;
		}

		onlineStatusLabel.visible = false;

		int count = entries.size;
		float availableH = contentHeight - 54;
		float rowHeight = GameMath.gate(ROW_HEIGHT_MIN, availableH / count, ROW_HEIGHT_MAX);
		float left = (contentWidth - Math.min(MAX_ROW_WIDTH, contentWidth)) / 2f + GAP;
		float top = 40 + Math.max(0, (availableH - rowHeight * count) / 2f);

		for (int i = 0; i < count; i++) {
			JsonValue entry = entries.get(i);
			OnlineRecord row = new OnlineRecord(i, entry);
			float offset = 0;
			if (rowHeight <= 14) {
				offset = (i % 2 == 1) ? 5 : -5;
			}
			row.setRect(
					screenInsets.left + left + offset,
					screenInsets.top + top + i * rowHeight,
					contentWidth - left * 2,
					rowHeight
			);
			onlineRowsGroup.add(row);
		}

		JsonValue currentUser = body.get("currentUserEntry");
		if (currentUser != null && !currentUser.isNull()) {
			int myRank = currentUser.getInt("rank", 0);
			int myScore = currentUser.getInt("score", 0);
			onlineUserRankLabel.visible = true;
			onlineUserRankLabel.text(Messages.get(this, "online_your_rank", myRank, myScore));
			onlineUserRankLabel.setPos(
					screenInsets.left + (contentWidth - onlineUserRankLabel.width()) / 2f,
					screenInsets.top + contentHeight - onlineUserRankLabel.height() - GAP
			);
			align(onlineUserRankLabel);
		} else if (!SPDSettings.isOnlineLoggedIn()) {
			onlineUserRankLabel.visible = true;
			onlineUserRankLabel.text(Messages.get(this, "online_login_hint"));
			onlineUserRankLabel.setPos(
					screenInsets.left + (contentWidth - onlineUserRankLabel.width()) / 2f,
					screenInsets.top + contentHeight - onlineUserRankLabel.height() - GAP
			);
			align(onlineUserRankLabel);
		} else {
			onlineUserRankLabel.visible = false;
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		//so that opening daily records does not trigger WndDailies opening on future visits
		Dungeon.daily = Dungeon.dailyReplay = false;
	}

	@Override
	protected void onBackPressed() {
		if (onlineActive) {
			setOnlineMode(false);
		} else {
			ShatteredPixelDungeon.switchNoFade(TitleScene.class);
		}
	}

	public static class OnlineRecord extends Button {

		private static final float GAP = 4;

		private static final int[] TEXT_WIN  = {0xFFFF88, 0xB2B25F};
		private static final int[] TEXT_LOSE = {0xDDDDDD, 0x888888};
		private static final int TEXT_SELF   = 0x44DDFF;

		private final String runUuid;

		protected Image shield;
		private Flare flare;
		private BitmapText position;
		private RenderedTextBlock desc;
		private Image steps;
		private BitmapText depth;
		private Image classIcon;
		private BitmapText level;

		public OnlineRecord(int pos, JsonValue entry) {
			super();

			this.runUuid = entry.getString("runUuid", "");
			int rank = entry.getInt("rank", pos + 1);
			String username = entry.getString("username", Messages.get(RankingsScene.class, "online_default_player"));
			int score = entry.getInt("score", 0);
			int heroLevel = entry.getInt("heroLevel", 1);
			int deepestFloor = entry.getInt("deepestFloor", 1);
			boolean win = entry.getBoolean("win", false);
			boolean ascended = entry.getBoolean("ascended", false);
			String heroClassStr = entry.getString("heroClass", "WARRIOR");

			boolean isSelf = SPDSettings.isOnlineLoggedIn()
					&& username.equalsIgnoreCase(SPDSettings.onlineUsername());

			if (isSelf) {
				flare = new Flare(6, 24);
				flare.angularSpeed = 90;
				flare.color(0x226688);
				addToBack(flare);
			}

			position.text(Integer.toString(rank));
			position.measure();

			desc.text(username + " • " + score);

			int odd = pos % 2;
			int textColor = isSelf ? TEXT_SELF : (win ? TEXT_WIN[odd] : TEXT_LOSE[odd]);

			position.hardlight(textColor);
			desc.hardlight(textColor);
			depth.hardlight(textColor);
			level.hardlight(textColor);

			if (ascended) {
				shield.copy(new ItemSprite(ItemSpriteSheet.AMULET, null));
				shield.hardlight(0.4f, 0.4f, 0.7f);
			} else if (win) {
				shield.copy(new ItemSprite(ItemSpriteSheet.AMULET, null));
			} else if (deepestFloor > 0) {
				depth.text(Integer.toString(deepestFloor));
				depth.measure();
				steps.copy(Icons.STAIRS.get());
				add(steps);
				add(depth);
			}

			if (heroLevel > 0) {
				level.text(Integer.toString(heroLevel));
				level.measure();
				add(level);
			}

			HeroClass heroClass;
			try {
				heroClass = HeroClass.valueOf(heroClassStr);
			} catch (Exception e) {
				heroClass = HeroClass.WARRIOR;
			}

			classIcon.copy(Icons.get(heroClass));
			if (heroClass == HeroClass.ROGUE) {
				classIcon.brightness(2f);
			}
		}

		@Override
		protected void createChildren() {
			super.createChildren();

			shield = new Image(new ItemSprite(ItemSpriteSheet.TOMB, null));
			add(shield);

			position = new BitmapText(PixelScene.pixelFont);
			add(position);

			desc = renderTextBlock(7);
			add(desc);

			depth = new BitmapText(PixelScene.pixelFont);
			steps = new Image();

			classIcon = new Image();
			add(classIcon);

			level = new BitmapText(PixelScene.pixelFont);
		}

		@Override
		protected void layout() {
			super.layout();

			shield.x = x + (16 - shield.width) / 2f;
			shield.y = y + (height - shield.height) / 2f;
			align(shield);

			position.x = shield.x + (shield.width - position.width()) / 2f;
			position.y = shield.y + (shield.height - position.height()) / 2f + 1;
			align(position);

			if (flare != null) {
				flare.point(shield.center());
			}

			classIcon.x = x + width - 16 + (16 - classIcon.width()) / 2f;
			classIcon.y = shield.y + (16 - classIcon.height()) / 2f;
			align(classIcon);

			level.x = classIcon.x + (classIcon.width - level.width()) / 2f;
			level.y = classIcon.y + (classIcon.height - level.height()) / 2f + 1;
			align(level);

			steps.x = x + width - 32 + (16 - steps.width()) / 2f;
			steps.y = shield.y + (16 - steps.height()) / 2f;
			align(steps);

			depth.x = steps.x + (steps.width - depth.width()) / 2f;
			depth.y = steps.y + (steps.height - depth.height()) / 2f + 1;
			align(depth);

			desc.maxWidth((int) (steps.x - (x + 16 + GAP)));
			desc.setPos(x + 16 + GAP, shield.y + (shield.height - desc.height()) / 2f + 1);
			align(desc);
		}

		@Override
		protected void onClick() {
			if (runUuid.isEmpty()) return;
			OnlineApiClient.get("/runs/" + runUuid, new OnlineApiClient.ApiCallback() {
				@Override
				public void onSuccess(int statusCode, JsonValue body) {
					if (body != null && ShatteredPixelDungeon.scene() != null) {
						ShatteredPixelDungeon.scene().addToFront(new WndOnlineRunDetail(body));
					}
				}

				@Override
				public void onError(int statusCode, String errorMessage) {
				}
			});
		}
	}

	public static class Record extends Button {

		private static final float GAP	= 4;

		private static final int[] TEXT_WIN	= {0xFFFF88, 0xB2B25F};
		private static final int[] TEXT_LOSE= {0xDDDDDD, 0x888888};
		private static final int FLARE_WIN	= 0x888866;
		private static final int FLARE_LOSE	= 0x666666;

		private Rankings.Record rec;

		protected Image shield;
		private Flare flare;
		private BitmapText position;
		private RenderedTextBlock desc;
		private Image steps;
		private BitmapText depth;
		private Image classIcon;
		private BitmapText level;

		public Record( int pos, boolean latest, Rankings.Record rec ) {
			super();

			this.rec = rec;

			if (latest) {
				flare = new Flare( 6, 24 );
				flare.angularSpeed = 90;
				flare.color( rec.win ? FLARE_WIN : FLARE_LOSE );
				addToBack( flare );
			}

			if (pos != Rankings.TABLE_SIZE-1) {
				position.text(Integer.toString(pos + 1));
			} else
				position.text(" ");
			position.measure();

			desc.text( Messages.titleCase(rec.desc()) );

			int odd = pos % 2;

			if (rec.win) {
				shield.copy( new ItemSprite(ItemSpriteSheet.AMULET, null) );
				position.hardlight( TEXT_WIN[odd] );
				desc.hardlight( TEXT_WIN[odd] );
				depth.hardlight( TEXT_WIN[odd] );
				level.hardlight( TEXT_WIN[odd] );
			} else {
				position.hardlight( TEXT_LOSE[odd] );
				desc.hardlight( TEXT_LOSE[odd] );
				depth.hardlight( TEXT_LOSE[odd] );
				level.hardlight( TEXT_LOSE[odd] );

				if (rec.depth != 0){
					depth.text( Integer.toString(rec.depth) );
					depth.measure();
					steps.copy(Icons.STAIRS.get());

					add(steps);
					add(depth);
				}

				if (rec.ascending){
					shield.copy( new ItemSprite(ItemSpriteSheet.AMULET, null) );
					shield.hardlight(0.4f, 0.4f, 0.7f);
				}

			}

			if (rec.daily){
				shield.copy( Icons.get(Icons.CALENDAR) );
				shield.hardlight(0.5f, 1f, 2f);
			} else if (!rec.customSeed.isEmpty()){
				shield.copy( Icons.get(Icons.SEED) );
				shield.hardlight(1f, 1.5f, 0.67f);
			}

			if (rec.herolevel != 0){
				level.text( Integer.toString(rec.herolevel) );
				level.measure();
				add(level);
			}

			classIcon.copy( Icons.get( rec.heroClass ) );
			if (rec.heroClass == HeroClass.ROGUE){
				//cloak of shadows needs to be brightened a bit
				classIcon.brightness(2f);
			}
		}

		@Override
		protected void createChildren() {

			super.createChildren();

			shield = new Image(new ItemSprite( ItemSpriteSheet.TOMB, null ));
			add( shield );

			position = new BitmapText( PixelScene.pixelFont);
			add( position );

			desc = renderTextBlock( 7 );
			add( desc );

			depth = new BitmapText( PixelScene.pixelFont);

			steps = new Image();

			classIcon = new Image();
			add( classIcon );

			level = new BitmapText( PixelScene.pixelFont);
		}

		@Override
		protected void layout() {

			super.layout();

			shield.x = x + (16 - shield.width) / 2f;
			shield.y = y + (height - shield.height) / 2f;
			align(shield);

			position.x = shield.x + (shield.width - position.width()) / 2f;
			position.y = shield.y + (shield.height - position.height()) / 2f + 1;
			align(position);

			if (flare != null) {
				flare.point( shield.center() );
			}

			classIcon.x = x + width - 16 + (16 - classIcon.width())/2f;
			classIcon.y = shield.y + (16 - classIcon.height())/2f;
			align(classIcon);

			level.x = classIcon.x + (classIcon.width - level.width()) / 2f;
			level.y = classIcon.y + (classIcon.height - level.height()) / 2f + 1;
			align(level);

			steps.x = x + width - 32 + (16 - steps.width())/2f;
			steps.y = shield.y + (16 - steps.height())/2f;
			align(steps);

			depth.x = steps.x + (steps.width - depth.width()) / 2f;
			depth.y = steps.y + (steps.height - depth.height()) / 2f + 1;
			align(depth);

			desc.maxWidth((int)(steps.x - (x + 16 + GAP)));
			desc.setPos(x + 16 + GAP, shield.y + (shield.height - desc.height()) / 2f + 1);
			align(desc);
		}

		@Override
		protected void onClick() {
			parent.add( new WndRanking( rec ) );
		}
	}
}