/*
 * Author: DenySilver (Reforged Pixel Dungeon)
 * License: GPLv3
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.badlogic.gdx.utils.JsonValue;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.Button;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.ItemSlot;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.DeviceCompat;

import java.text.NumberFormat;
import java.util.Locale;

public class WndOnlineRunDetail extends WndTabbed {

    private static final int WIDTH  = 115;
    private static final int HEIGHT = 144;

    private final JsonValue run;
    private final JsonValue snap;

    private Hero prevHero;
    private boolean usedDummyHero = false;

    public WndOnlineRunDetail(JsonValue run) {
        super();
        resize(WIDTH, HEIGHT);

        this.run = run;
        this.snap = run.get("buildSnapshot");

        if (Dungeon.hero == null) {
            prevHero = null;
            usedDummyHero = true;

            Hero dummy = new Hero();
            try {
                dummy.heroClass = HeroClass.valueOf(run.getString("heroClass", "WARRIOR"));
            } catch (Exception e) {
                dummy.heroClass = HeroClass.WARRIOR;
            }
            dummy.lvl = run.getInt("heroLevel", 1);
            if (snap != null && !snap.isNull()) {
                dummy.STR = snap.getInt("str", 10);
            }
            Dungeon.hero = dummy;
        }

        int challengesMask = run.getInt("challengesMask", 0);

        Icons[] icons = {Icons.RANKINGS, Icons.BACKPACK_LRG, Icons.CHALLENGE_COLOR};
        Group[] pages = {
                new StatsTab(),
                new ItemsTab(),
                challengesMask != 0 ? new ChallengesTab(challengesMask) : null
        };

        for (int i = 0; i < pages.length; i++) {
            if (pages[i] == null) continue;
            add(pages[i]);
            add(new RankingTab(icons[i], pages[i]));
        }

        layoutTabs();
        select(0);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (usedDummyHero) {
            Dungeon.hero = prevHero;
            usedDummyHero = false;
        }
    }

    private class RankingTab extends IconTab {

        private final Group page;

        public RankingTab(Icons icon, Group page) {
            super(Icons.get(icon));
            this.page = page;
        }

        @Override
        protected void select(boolean value) {
            super.select(value);
            if (page != null) {
                page.visible = page.active = selected;
            }
        }
    }

    private class StatsTab extends Group {

        private int GAP = 4;

        public StatsTab() {
            super();

            String username = run.getString("username", "Player");
            String heroClassStr = run.getString("heroClass", "WARRIOR");
            String heroSubclass = run.getString("heroSubclass", "NONE");
            int heroLevel = run.getInt("heroLevel", 1);
            int deepestFloor = run.getInt("deepestFloor", 1);
            int score = run.getInt("score", 0);
            int turns = run.getInt("turnsPassed", 0);
            boolean win = run.getBoolean("win", false);
            boolean ascended = run.getBoolean("ascended", false);
            String cause = run.getString("causeOfDeath", "");
            String seedCode = run.getString("seedCode", "");
            String mode = run.getString("mode", "STANDARD");
            String version = run.getString("gameVersion", "");

            HeroClass heroClass;
            try {
                heroClass = HeroClass.valueOf(heroClassStr);
            } catch (Exception e) {
                heroClass = HeroClass.WARRIOR;
            }

            int armorTier = (snap != null && !snap.isNull()) ? snap.getInt("armorTier", 1) : 1;

            IconTitle title = new IconTitle();
            title.icon(HeroSprite.avatar(heroClass, armorTier));
            String titleText = Messages.get(WndOnlineRunDetail.class, "title", heroLevel, heroClass.title().toUpperCase(Locale.ENGLISH));
            title.label(titleText);
            title.color(Window.TITLE_COLOR);
            title.setRect(0, 0, WIDTH, 0);
            add(title);

            if (!seedCode.isEmpty()) {
                GAP--;
            }

            float pos = title.bottom() + 1;

            RenderedTextBlock playerLabel = PixelScene.renderTextBlock(username + " >", 7);
            playerLabel.hardlight(0x44DDFF);
            playerLabel.setPos(0, pos);
            add(playerLabel);

            Button btnProfile = new Button() {
                @Override
                protected void onPointerDown() {
                    playerLabel.hardlight(0xFFFFFF);
                    Sample.INSTANCE.play(Assets.Sounds.CLICK, 0.7f, 0.7f, 1.2f);
                }

                @Override
                protected void onPointerUp() {
                    playerLabel.hardlight(0x44DDFF);
                }

                @Override
                protected void onClick() {
                    ShatteredPixelDungeon.scene().addToFront(new WndOnlineProfile(username, null));
                }
            };

            btnProfile.setRect(0, pos - 2, playerLabel.width() + 4, playerLabel.height() + 4);
            add(btnProfile);

            RenderedTextBlock verLabel = PixelScene.renderTextBlock(version, 7);
            verLabel.hardlight(0xCCCCCC);
            verLabel.setPos(WIDTH - verLabel.width(), pos);
            add(verLabel);

            pos = playerLabel.bottom() + 4;

            NumberFormat num = NumberFormat.getInstance(Messages.locale());

            pos = statSlot(this, Messages.get(WndOnlineRunDetail.class, "score"), num.format(score), pos);
            if (!"NONE".equals(heroSubclass)) {
                pos = statSlot(this, Messages.get(WndOnlineRunDetail.class, "subclass"), heroSubclass, pos);
            }
            if (snap != null && !snap.isNull() && snap.has("str")) {
                pos = statSlot(this, Messages.get(WndOnlineRunDetail.class, "str"), Integer.toString(snap.getInt("str", 10)), pos);
            }
            pos = statSlot(this, Messages.get(WndOnlineRunDetail.class, "turns"), num.format(turns), pos);

            String depthLabel = ascended
                    ? Messages.get(WndOnlineRunDetail.class, "ascension")
                    : (win ? Messages.get(WndOnlineRunDetail.class, "victory") : Messages.get(WndOnlineRunDetail.class, "depth"));
            pos = statSlot(this, depthLabel, num.format(deepestFloor), pos);

            if (!seedCode.isEmpty()) {
                String seedTitle = "DAILY".equals(mode)
                        ? Messages.get(WndOnlineRunDetail.class, "daily_seed")
                        : Messages.get(WndOnlineRunDetail.class, "seed");
                pos = statSlot(this, seedTitle, seedCode, pos);
            }

            if (snap != null && !snap.isNull() && snap.has("enemiesSlain")) {
                pos += GAP;
                pos = statSlot(this, Messages.get(WndOnlineRunDetail.class, "enemies"), num.format(snap.getInt("enemiesSlain", 0)), pos);
                pos = statSlot(this, Messages.get(WndOnlineRunDetail.class, "gold"), num.format(snap.getInt("goldCollected", 0)), pos);
                pos = statSlot(this, Messages.get(WndOnlineRunDetail.class, "food"), num.format(snap.getInt("foodEaten", 0)), pos);
                pos = statSlot(this, Messages.get(WndOnlineRunDetail.class, "alchemy"), num.format(snap.getInt("itemsCrafted", 0)), pos);
            }

            int buttontop = HEIGHT - 16;

            if (!seedCode.isEmpty() && !"DAILY".equals(mode)
                    && (DeviceCompat.isDebug() || Badges.isUnlocked(Badges.Badge.VICTORY))) {
                final Image icon = Icons.get(Icons.SEED);
                RedButton btnSeed = new RedButton(Messages.get(WndOnlineRunDetail.class, "copy_seed")) {
                    @Override
                    protected void onClick() {
                        super.onClick();
                        ShatteredPixelDungeon.scene().addToFront(new WndOptions(
                                new Image(icon),
                                Messages.get(WndOnlineRunDetail.class, "copy_seed"),
                                Messages.get(WndOnlineRunDetail.class, "copy_seed_desc", seedCode),
                                Messages.get(WndOnlineRunDetail.class, "copy_seed_yes"),
                                Messages.get(WndOnlineRunDetail.class, "copy_seed_no")
                        ) {
                            @Override
                            protected void onSelect(int index) {
                                super.onSelect(index);
                                if (index == 0) {
                                    SPDSettings.customSeed(seedCode);
                                    icon.hardlight(1f, 1.5f, 0.67f);
                                }
                            }
                        });
                    }
                };
                if (SPDSettings.customSeed().equals(seedCode)) {
                    icon.hardlight(1f, 1.5f, 0.67f);
                }
                btnSeed.icon(icon);
                btnSeed.setRect(0, buttontop, WIDTH, 16);
                add(btnSeed);
            }
        }

        private float statSlot(Group parent, String label, String value, float pos) {
            int size = 7;
            RenderedTextBlock txt;
            do {
                txt = PixelScene.renderTextBlock(label, size);
                size--;
            } while (txt.width() >= WIDTH * 0.55f && size > 4);
            txt.setPos(0, pos + (6 - txt.height()) / 2f);
            PixelScene.align(txt);
            parent.add(txt);

            size = 7;
            do {
                txt = PixelScene.renderTextBlock(value, size);
                size--;
            } while (txt.width() >= WIDTH * 0.45f && size > 4);
            txt.maxWidth((int) (WIDTH * 0.45f));
            txt.setPos(WIDTH * 0.55f, pos + (6 - txt.height()) / 2f);
            PixelScene.align(txt);
            parent.add(txt);

            return pos + GAP + txt.height();
        }

        private String formatCause(String causeOfDeath, boolean win, boolean ascended) {
            if (ascended || "ASCENDED".equals(causeOfDeath)) {
                return Messages.titleCase(Messages.get(Rankings.class, "ascended"));
            }
            if (win || "VICTORY".equals(causeOfDeath)) {
                return Messages.titleCase(Messages.get(
                        com.shatteredpixel.shatteredpixeldungeon.items.Amulet.class, "rankings_desc"
                ));
            }
            if (causeOfDeath != null && causeOfDeath.contains(".")) {
                try {
                    Class<?> causeClass = Class.forName(causeOfDeath);
                    String desc = Messages.get(causeClass, "rankings_desc");
                    if (!desc.startsWith(Messages.NO_TEXT_FOUND)) {
                        return Messages.titleCase(desc);
                    }
                } catch (ClassNotFoundException ignored) {
                }
            }
            return Messages.titleCase(Messages.get(Rankings.Record.class, "something"));
        }
    }

    private class ItemsTab extends Group {

        private float pos = 0;

        public ItemsTab() {
            super();

            if (snap == null || snap.isNull()) {
                RenderedTextBlock empty = PixelScene.renderTextBlock(Messages.get(WndOnlineRunDetail.class, "no_items"), 7);
                empty.maxWidth(WIDTH - 8);
                empty.setPos((WIDTH - empty.width()) / 2f, (HEIGHT - empty.height()) / 2f);
                add(empty);
                return;
            }

            addSnapshotSlot(snap.get("weapon"), ItemSpriteSheet.WEAPON_HOLDER);
            addSnapshotSlot(snap.get("armor"), ItemSpriteSheet.ARMOR_HOLDER);
            addSnapshotSlot(snap.get("artifact"), ItemSpriteSheet.ARTIFACT_HOLDER);
            addSnapshotSlot(snap.get("misc"), ItemSpriteSheet.SOMETHING);
            addSnapshotSlot(snap.get("ring"), ItemSpriteSheet.RING_HOLDER);

            JsonValue trinket = snap.get("trinket");
            if (trinket != null && !trinket.isNull()) {
                addSnapshotSlot(trinket, ItemSpriteSheet.TRINKET_HOLDER);
            }
        }

        private void addSnapshotSlot(JsonValue itemJson, int fallbackSprite) {
            if (itemJson == null || itemJson.isNull()) return;
            SnapshotItemButton slot = new SnapshotItemButton(itemJson, fallbackSprite);
            slot.setRect(0, pos, WIDTH, SnapshotItemButton.HEIGHT);
            add(slot);
            pos += slot.height() + 1;
        }
    }

    private class ChallengesTab extends Group {

        public ChallengesTab(int challengesMask) {
            super();
            camera = WndOnlineRunDetail.this.camera;

            float pos = 0;
            for (int i = 0; i < Challenges.NAME_IDS.length; i++) {
                final String challenge = Challenges.NAME_IDS[i];

                CheckBox cb = new CheckBox(Messages.titleCase(Messages.get(Challenges.class, challenge)));
                cb.checked((challengesMask & Challenges.MASKS[i]) != 0);
                cb.active = false;

                if (i > 0) {
                    pos += 1;
                }
                cb.setRect(0, pos, WIDTH - 16, 15);
                add(cb);

                IconButton info = new IconButton(Icons.get(Icons.INFO)) {
                    @Override
                    protected void onClick() {
                        super.onClick();
                        ShatteredPixelDungeon.scene().add(
                                new WndMessage(Messages.get(Challenges.class, challenge + "_desc"))
                        );
                    }
                };
                info.setRect(cb.right(), pos, 16, 15);
                add(info);

                pos = cb.bottom();
            }
        }
    }

    private static class SnapshotItemButton extends Button {

        public static final int HEIGHT = 22;

        private final Item resolvedItem;
        private final String displayName;
        private final int fallbackSprite;
        private final boolean cursed;

        private ItemSlot slot;
        private ColorBlock bg;
        private RenderedTextBlock name;

        public SnapshotItemButton(JsonValue itemJson, int fallbackSprite) {
            super();

            this.fallbackSprite = fallbackSprite;
            String rawName = itemJson.getString("name", Messages.get(WndOnlineRunDetail.class, "default_item"));
            int lvl = itemJson.getInt("level", 0);
            this.cursed = itemJson.getBoolean("cursed", false);
            String className = itemJson.getString("className", "");

            this.resolvedItem = instantiateItem(className, lvl, cursed);
            if (this.resolvedItem != null) {
                this.displayName = Messages.titleCase(resolvedItem.name());
                slot.item(resolvedItem);
            } else {
                String suffix = lvl > 0 ? " +" + lvl : (lvl < 0 ? " " + lvl : "");
                this.displayName = Messages.titleCase(rawName) + suffix;
                slot.item(new Item() {
                    @Override
                    public int image() {
                        return fallbackSprite;
                    }
                });
            }

            if (cursed) {
                bg.ra = +0.3f;
                bg.ga = -0.15f;
                bg.ba = -0.15f;
            } else if (resolvedItem != null && !(resolvedItem instanceof EquipableItem || resolvedItem instanceof Wand)) {
                bg.ra = +0.35f;
                bg.ba = +0.35f;
            }
        }

        private static Item instantiateItem(String className, int level, boolean cursed) {
            if (className == null || className.isEmpty()) return null;
            try {
                Class<?> cls = Class.forName(className);
                Object obj = cls.getDeclaredConstructor().newInstance();
                if (obj instanceof Item) {
                    Item item = (Item) obj;
                    item.level(level);
                    item.cursed = cursed;
                    item.cursedKnown = true;
                    item.levelKnown = true;
                    return item;
                }
            } catch (Exception ignored) {
            }
            return null;
        }

        @Override
        protected void createChildren() {
            bg = new ColorBlock(28, HEIGHT, 0x9953564D);
            add(bg);

            slot = new ItemSlot();
            add(slot);

            name = PixelScene.renderTextBlock(7);
            add(name);

            super.createChildren();
        }

        @Override
        protected void layout() {
            bg.x = x;
            bg.y = y;

            slot.setRect(x, y, 28, HEIGHT);
            PixelScene.align(slot);

            name.maxWidth((int) (width - slot.width() - 2));
            name.text(displayName);
            name.setPos(
                    slot.right() + 2,
                    y + (height - name.height()) / 2f
            );
            PixelScene.align(name);

            super.layout();
        }

        @Override
        protected void onPointerDown() {
            bg.brightness(1.5f);
            Sample.INSTANCE.play(Assets.Sounds.CLICK, 0.7f, 0.7f, 1.2f);
        }

        @Override
        protected void onPointerUp() {
            bg.brightness(1.0f);
        }

        @Override
        protected void onClick() {
            if (resolvedItem != null) {
                Game.scene().add(new WndInfoItem(resolvedItem));
            }
        }
    }
}