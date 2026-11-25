/*
 * Author: DenySilver (Reforged Pixel Dungeon)
 * License: GPLv3
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.SpiritHawk;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalSpire;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Pylon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.MirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.PrismaticImage;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Sheep;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfRegrowth;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfWarding;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.TerrainFeaturesTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollingGridPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.RectF;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.List;

public class WndSandbox extends WndTabbed {

    public static final int WIDTH_P  = 126;
    public static final int HEIGHT_P = 180;

    public static final int WIDTH_L  = 216;
    public static final int HEIGHT_L = 130;

    private static final int ITEM_HEIGHT = 18;

    private ClassTab classTab;
    private ItemsTab itemsTab;
    private StatsTab statsTab;
    private TrapsTab trapsTab;
    private MobsTab mobsTab;

    public static int last_index = 0;
    private static WndSandbox INSTANCE = null;

    public WndSandbox() {
        if (INSTANCE != null) {
            INSTANCE.hide();
        }
        INSTANCE = this;
        int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;
        int height = PixelScene.landscape() ? HEIGHT_L : HEIGHT_P;

        resize(width, height);

        classTab = new ClassTab();
        add(classTab);
        classTab.setRect(0, 0, width, height);
        classTab.updateList();

        itemsTab = new ItemsTab();
        add(itemsTab);
        itemsTab.setRect(0, 0, width, height);
        itemsTab.updateList();

        statsTab = new StatsTab();
        add(statsTab);
        statsTab.setRect(0, 0, width, height);
        statsTab.updateList();

        trapsTab = new TrapsTab();
        add(trapsTab);
        trapsTab.setRect(0, 0, width, height);
        trapsTab.updateList();

        mobsTab = new MobsTab();
        add(mobsTab);
        mobsTab.setRect(0, 0, width, height);
        mobsTab.updateList();

        Tab[] tabs = {
                new IconTab(Icons.get(Dungeon.hero.heroClass)) {
                    @Override
                    protected void select(boolean value) {
                        super.select(value);
                        classTab.active = classTab.visible = value;
                        if (value) last_index = 0;
                    }

                    @Override
                    protected String hoverText() {
                        return Messages.get(WndSandbox.class, "tab_class");
                    }
                },
                new IconTab(new ItemSprite(ItemSpriteSheet.WEAPON_HOLDER)) {
                    @Override
                    protected void select(boolean value) {
                        super.select(value);
                        itemsTab.active = itemsTab.visible = value;
                        if (value) last_index = 1;
                    }

                    @Override
                    protected String hoverText() {
                        return Messages.get(WndSandbox.class, "tab_items");
                    }
                },
                new IconTab(new ItemSprite(ItemSpriteSheet.MASTERY)) {
                    @Override
                    protected void select(boolean value) {
                        super.select(value);
                        statsTab.active = statsTab.visible = value;
                        if (value) last_index = 2;
                    }

                    @Override
                    protected String hoverText() {
                        return Messages.get(WndSandbox.class, "tab_stats");
                    }
                },
                new IconTab(new ItemSprite(ItemSpriteSheet.BOMB_HOLDER)) {
                    @Override
                    protected void select(boolean value) {
                        super.select(value);
                        trapsTab.active = trapsTab.visible = value;
                        if (value) last_index = 3;
                    }

                    @Override
                    protected String hoverText() {
                        return Messages.get(WndSandbox.class, "tab_traps");
                    }
                },
                new IconTab(new ItemSprite(ItemSpriteSheet.MOB_HOLDER)) {
                    @Override
                    protected void select(boolean value) {
                        super.select(value);
                        mobsTab.active = mobsTab.visible = value;
                        if (value) last_index = 4;
                    }

                    @Override
                    protected String hoverText() {
                        return Messages.get(WndSandbox.class, "tab_mobs");
                    }
                }
        };

        for (Tab tab : tabs) {
            add(tab);
        }

        layoutTabs();
        select(last_index);
    }

    // =========================================================
    // CLASSES
    // =========================================================
    private class ClassTab extends Component {

        private ScrollPane list;
        private final ArrayList<RedButton> classButtons = new ArrayList<>();
        private final ArrayList<HeroClass> classList = new ArrayList<>();

        private final ArrayList<RedButton> subButtons = new ArrayList<>();
        private final ArrayList<HeroSubClass> subList = new ArrayList<>();
        private final ArrayList<HeroClass> subOwners = new ArrayList<>();

        @Override
        protected void createChildren() {
            list = new ScrollPane(new Component());
            add(list);
        }

        @Override
        protected void layout() {
            super.layout();
            list.setRect(x, y, width, height);
        }

        public void updateList() {
            Component content = list.content();

            if (classButtons.isEmpty()) {
                for (HeroClass hc : HeroClass.values()) {
                    RedButton btn = new RedButton(Messages.titleCase(hc.title())) {
                        @Override
                        protected void onClick() {
                            Dungeon.hero.heroClass = hc;
                            Dungeon.hero.subClass = HeroSubClass.NONE;
                            Dungeon.hero.armorAbility = null;

                            Dungeon.hero.lvl = 1;
                            Dungeon.hero.exp = 0;
                            Dungeon.hero.STR = 10;
                            Dungeon.hero.talents.clear();
                            Dungeon.hero.metamorphedTalents.clear();
                            Talent.initClassTalents(Dungeon.hero);

                            Dungeon.hero.updateHT(true);
                            Dungeon.hero.HP = Dungeon.hero.HT;

                            if (Dungeon.hero.sprite instanceof HeroSprite) {
                                Dungeon.hero.sprite.texture(hc.spritesheet());
                                ((HeroSprite) Dungeon.hero.sprite).updateArmor();
                                Dungeon.hero.sprite.idle();
                            }
                            GameScene.updateAvatar();
                            GLog.p(Messages.get(WndSandbox.class, "class_selected", Messages.titleCase(hc.title())));
                            refreshState();
                        }
                    };
                    btn.icon(Icons.get(hc));
                    content.add(btn);
                    classButtons.add(btn);
                    classList.add(hc);

                    for (HeroSubClass sub : hc.subClasses()) {
                        RedButton btnSub = new RedButton(Messages.titleCase(sub.title())) {
                            @Override
                            protected void onClick() {
                                Dungeon.hero.subClass = sub;

                                Dungeon.hero.lvl = 12;
                                Dungeon.hero.exp = 0;
                                Dungeon.hero.talents.clear();
                                Dungeon.hero.metamorphedTalents.clear();
                                Talent.initClassTalents(Dungeon.hero);
                                Talent.initSubclassTalents(Dungeon.hero);

                                Dungeon.hero.updateHT(true);
                                Dungeon.hero.HP = Dungeon.hero.HT;

                                GLog.p(Messages.get(WndSandbox.class, "subclass_selected", Messages.titleCase(sub.title())));
                                refreshState();
                            }
                        };
                        btnSub.icon(new ItemSprite(ItemSpriteSheet.MASTERY));
                        content.add(btnSub);
                        subButtons.add(btnSub);
                        subList.add(sub);
                        subOwners.add(hc);
                    }
                }
            }

            refreshState();
        }

        private void refreshState() {
            float top = 2;

            for (int i = 0; i < classButtons.size(); i++) {
                RedButton btn = classButtons.get(i);
                HeroClass hc = classList.get(i);
                btn.textColor(Dungeon.hero.heroClass == hc ? Window.TITLE_COLOR : Window.WHITE);
                btn.setRect(2, top, width - 4, ITEM_HEIGHT);
                top += ITEM_HEIGHT + 2;
            }

            top += 4;

            for (int i = 0; i < subButtons.size(); i++) {
                RedButton btnSub = subButtons.get(i);
                boolean belongsToCurrent = (subOwners.get(i) == Dungeon.hero.heroClass);
                btnSub.visible = btnSub.active = belongsToCurrent;
                if (belongsToCurrent) {
                    btnSub.textColor(Dungeon.hero.subClass == subList.get(i) ? Window.TITLE_COLOR : Window.WHITE);
                    btnSub.setRect(2, top, width - 4, ITEM_HEIGHT);
                    top += ITEM_HEIGHT + 2;
                }
            }

            list.content().setSize(width, top);
            list.setSize(list.width(), list.height());
        }
    }

    // =========================================================
    // ITEMS
    // =========================================================
    private class ItemsTab extends Component {

        private RedButton[] catButtons;
        private static final int NUM_BUTTONS = 3;
        private int currentIdx = 0;
        private ScrollingGridPane grid;

        @Override
        protected void createChildren() {
            catButtons = new RedButton[NUM_BUTTONS];
            String[] labels = {
                    Messages.get(WndSandbox.class, "cat_gear"),
                    Messages.get(WndSandbox.class, "cat_consumables"),
                    Messages.get(WndSandbox.class, "cat_enchants")
            };
            for (int i = 0; i < NUM_BUTTONS; i++) {
                final int idx = i;
                catButtons[i] = new RedButton(labels[i], 6) {
                    @Override
                    protected void onClick() {
                        currentIdx = idx;
                        updateList();
                    }
                };
                add(catButtons[i]);
            }
            catButtons[0].icon(new ItemSprite(ItemSpriteSheet.WEAPON_HOLDER));
            catButtons[1].icon(new ItemSprite(ItemSpriteSheet.POTION_HOLDER));
            catButtons[2].icon(new ItemSprite(ItemSpriteSheet.SCROLL_HOLDER));

            grid = new ScrollingGridPane();
            add(grid);
        }

        @Override
        protected void layout() {
            super.layout();
            float btnW = width() / NUM_BUTTONS;
            for (int i = 0; i < NUM_BUTTONS; i++) {
                catButtons[i].setRect(x + i * btnW, y, btnW, ITEM_HEIGHT);
                PixelScene.align(catButtons[i]);
            }
            grid.setRect(x, catButtons[0].bottom() + 1, width, height - ITEM_HEIGHT - 1);
        }

        public void updateList() {
            grid.clear();
            for (int i = 0; i < NUM_BUTTONS; i++) {
                catButtons[i].textColor(i == currentIdx ? Window.TITLE_COLOR : Window.WHITE);
            }

            if (currentIdx == 2) {
                populateEnchantsAndGlyphs();
                grid.setRect(x, catButtons[0].bottom() + 1, width, height - ITEM_HEIGHT - 1);
                return;
            }

            ArrayList<Catalog> catalogs = (currentIdx == 0) ? Catalog.equipmentCatalogs : Catalog.consumableCatalogs;

            for (Catalog catalog : catalogs) {
                grid.addHeader("_" + Messages.titleCase(catalog.title()) + "_");
                for (Class<?> itemClass : catalog.items()) {
                    if (!Item.class.isAssignableFrom(itemClass)) continue;

                    Item item = (Item) Reflection.newInstance(itemClass);
                    ItemSprite sprite = new ItemSprite(item.image, item.glowing());

                    if (item instanceof MagesStaff) {
                        RectF frame = sprite.frame();
                        frame.top += frame.height() / 8f;
                        sprite.frame(frame);
                    }

                    ScrollingGridPane.GridItem gridItem = new ScrollingGridPane.GridItem(sprite) {
                        @Override
                        public boolean onClick(float x, float y) {
                            if (inside(x, y)) {
                                Item fresh = (Item) Reflection.newInstance(itemClass);
                                fresh.identify();
                                GameScene.show(new WndSandboxItem(fresh));
                                return true;
                            }
                            return false;
                        }
                    };

                    if (item.icon != -1) {
                        Image secondIcon = new Image(Assets.Sprites.ITEM_ICONS);
                        secondIcon.frame(ItemSpriteSheet.Icons.film.get(item.icon));
                        gridItem.addSecondIcon(secondIcon);
                    }

                    grid.addItem(gridItem);
                }
            }

            grid.setRect(x, catButtons[0].bottom() + 1, width, height - ITEM_HEIGHT - 1);
        }

        private void populateEnchantsAndGlyphs() {
            grid.addHeader("_" + Messages.get(WndSandbox.class, "header_weapon_enchants") + "_");
            addEnchantGroup(Weapon.Enchantment.common);
            addEnchantGroup(Weapon.Enchantment.uncommon);
            addEnchantGroup(Weapon.Enchantment.rare);

            grid.addHeader("_" + Messages.get(WndSandbox.class, "header_weapon_curses") + "_");
            addEnchantGroup(Weapon.Enchantment.curses);

            grid.addHeader("_" + Messages.get(WndSandbox.class, "header_armor_glyphs") + "_");
            addGlyphGroup(Armor.Glyph.common);
            addGlyphGroup(Armor.Glyph.uncommon);
            addGlyphGroup(Armor.Glyph.rare);

            grid.addHeader("_" + Messages.get(WndSandbox.class, "header_armor_curses") + "_");
            addGlyphGroup(Armor.Glyph.curses);
        }

        private void addEnchantGroup(Class<?>[] classes) {
            for (Class<?> cls : classes) {
                Weapon.Enchantment ench = (Weapon.Enchantment) Reflection.newInstance(cls);
                ItemSprite sprite = new ItemSprite(ItemSpriteSheet.WORN_SHORTSWORD, ench.glowing());

                grid.addItem(new ScrollingGridPane.GridItem(sprite) {
                    @Override
                    public boolean onClick(float x, float y) {
                        if (inside(x, y)) {
                            Weapon.Enchantment fresh = (Weapon.Enchantment) Reflection.newInstance(cls);
                            GameScene.show(new WndApplyEnchant(fresh, null));
                            return true;
                        }
                        return false;
                    }
                });
            }
        }

        private void addGlyphGroup(Class<?>[] classes) {
            for (Class<?> cls : classes) {
                Armor.Glyph glyph = (Armor.Glyph) Reflection.newInstance(cls);
                ItemSprite sprite = new ItemSprite(ItemSpriteSheet.ARMOR_CLOTH, glyph.glowing());

                grid.addItem(new ScrollingGridPane.GridItem(sprite) {
                    @Override
                    public boolean onClick(float x, float y) {
                        if (inside(x, y)) {
                            Armor.Glyph fresh = (Armor.Glyph) Reflection.newInstance(cls);
                            GameScene.show(new WndApplyEnchant(null, fresh));
                            return true;
                        }
                        return false;
                    }
                });
            }
        }
    }

    private static class WndApplyEnchant extends WndTitledMessage {

        public WndApplyEnchant(Weapon.Enchantment ench, Armor.Glyph glyph) {
            super(
                    ench != null
                            ? new ItemSprite(ItemSpriteSheet.WORN_SHORTSWORD, ench.glowing())
                            : new ItemSprite(ItemSpriteSheet.ARMOR_CLOTH, glyph.glowing()),
                    Messages.titleCase(ench != null ? ench.name() : glyph.name()),
                    ench != null ? ench.desc() : glyph.desc()
            );

            RedButton btnApply = new RedButton(Messages.get(WndSandbox.class, "apply_gear")) {
                @Override
                protected void onClick() {
                    if (ench != null) {
                        Weapon target = null;
                        if (Dungeon.hero.belongings.weapon instanceof Weapon) {
                            target = (Weapon) Dungeon.hero.belongings.weapon;
                        } else {
                            target = Dungeon.hero.belongings.getItem(Weapon.class);
                        }

                        if (target != null) {
                            target.enchant(ench);
                            target.cursed = ench.curse();
                            target.identify();
                            GLog.p(Messages.get(WndSandbox.class, "enchant_applied", ench.name(), target.name()));
                        } else {
                            GLog.n(Messages.get(WndSandbox.class, "no_weapon"));
                        }
                    } else if (glyph != null) {
                        Armor target = Dungeon.hero.belongings.armor;
                        if (target == null) {
                            target = Dungeon.hero.belongings.getItem(Armor.class);
                        }

                        if (target != null) {
                            target.inscribe(glyph);
                            target.cursed = glyph.curse();
                            target.identify();
                            GLog.p(Messages.get(WndSandbox.class, "glyph_applied", glyph.name(), target.name()));
                        } else {
                            GLog.n(Messages.get(WndSandbox.class, "no_armor"));
                        }
                    }
                    hide();
                }
            };
            btnApply.setRect(0, height + 4, width, 18);
            add(btnApply);

            resize(width, (int) btnApply.bottom());
        }
    }

    private static class WndSandboxItem extends WndInfoItem {

        private static int enchantIdx = 0;

        public WndSandboxItem(Item item) {
            super(item);

            float top = height + 2;

            if (item.isUpgradable()) {
                RedButton btnPlus1 = new RedButton(Messages.get(WndSandbox.class, "plus_1")) {
                    @Override
                    protected void onClick() {
                        item.upgrade(1);
                        hide();
                        GameScene.show(new WndSandboxItem(item));
                    }
                };
                btnPlus1.setRect(0, top, width / 2f - 1, 16);
                add(btnPlus1);

                RedButton btnPlus5 = new RedButton(Messages.get(WndSandbox.class, "plus_5")) {
                    @Override
                    protected void onClick() {
                        item.upgrade(5);
                        hide();
                        GameScene.show(new WndSandboxItem(item));
                    }
                };
                btnPlus5.setRect(btnPlus1.right() + 2, top, width / 2f - 1, 16);
                add(btnPlus5);

                top = btnPlus1.bottom() + 2;
            }

            RedButton btnGive = new RedButton(Messages.get(WndSandbox.class, "give_item")) {
                @Override
                protected void onClick() {
                    item.identify();
                    if (!item.collect()) {
                        Dungeon.level.drop(item, Dungeon.hero.pos).sprite.drop();
                    }
                    GLog.p(Messages.get(WndSandbox.class, "item_given", item.title()));
                    hide();
                }
            };
            btnGive.icon(new ItemSprite(item.image, item.glowing()));
            btnGive.setRect(0, top, width, 18);
            add(btnGive);

            resize(width, (int) btnGive.bottom());
        }

        private static List<Class<?>> getAllEnchants() {
            List<Class<?>> list = new ArrayList<>();
            for (Class<?> c : Weapon.Enchantment.common) list.add(c);
            for (Class<?> c : Weapon.Enchantment.uncommon) list.add(c);
            for (Class<?> c : Weapon.Enchantment.rare) list.add(c);
            for (Class<?> c : Weapon.Enchantment.curses) list.add(c);
            return list;
        }

        private static List<Class<?>> getAllGlyphs() {
            List<Class<?>> list = new ArrayList<>();
            for (Class<?> c : Armor.Glyph.common) list.add(c);
            for (Class<?> c : Armor.Glyph.uncommon) list.add(c);
            for (Class<?> c : Armor.Glyph.rare) list.add(c);
            for (Class<?> c : Armor.Glyph.curses) list.add(c);
            return list;
        }
    }

    // =========================================================
    // STATS
    // =========================================================
    private static class StatsTab extends Component {

        private ScrollPane list;

        @Override
        protected void createChildren() {
            list = new ScrollPane(new Component());
            add(list);
        }

        @Override
        protected void layout() {
            super.layout();
            list.setRect(x, y, width, height);
        }

        public void updateList() {
            Component content = list.content();
            content.clear();

            float top = 2;

            top = addActionBtn(content, top, Messages.get(WndSandbox.class, "stat_lvl_1"), new ItemSprite(ItemSpriteSheet.MASTERY), () -> {
                Dungeon.hero.lvl++;
                Dungeon.hero.updateHT(true);
                Dungeon.hero.HP = Dungeon.hero.HT;
                GLog.p(Messages.get(WndSandbox.class, "log_lvl", Dungeon.hero.lvl));
            });

            top = addActionBtn(content, top, Messages.get(WndSandbox.class, "stat_lvl_5"), new ItemSprite(ItemSpriteSheet.MASTERY), () -> {
                Dungeon.hero.lvl += 5;
                Dungeon.hero.updateHT(true);
                Dungeon.hero.HP = Dungeon.hero.HT;
                GLog.p(Messages.get(WndSandbox.class, "log_lvl", Dungeon.hero.lvl));
            });

            top = addActionBtn(content, top, Messages.get(WndSandbox.class, "stat_heal"), new ItemSprite(ItemSpriteSheet.POTION_HOLDER), () -> {
                Dungeon.hero.HP = Dungeon.hero.HT;
                Buff.affect(Dungeon.hero, Hunger.class).satisfy(Hunger.STARVING);
                GLog.p(Messages.get(WndSandbox.class, "log_heal"));
            });

            top = addActionBtn(content, top, Messages.get(WndSandbox.class, "stat_godmode"), new ItemSprite(ItemSpriteSheet.ANKH), () -> {
                Dungeon.hero.HT = 9999;
                Dungeon.hero.HP = 9999;
                GLog.p(Messages.get(WndSandbox.class, "log_godmode"));
            });

            top = addActionBtn(content, top, Messages.get(WndSandbox.class, "stat_str"), new ItemSprite(ItemSpriteSheet.ELIXIR_HOLDER), () -> {
                Dungeon.hero.STR++;
                GLog.p(Messages.get(WndSandbox.class, "log_str", Dungeon.hero.STR));
            });

            top = addActionBtn(content, top, Messages.get(WndSandbox.class, "stat_gold"), new ItemSprite(ItemSpriteSheet.GOLD), () -> {
                Dungeon.gold += 1000;
                GLog.p(Messages.get(WndSandbox.class, "log_gold", Dungeon.gold));
            });

            top = addActionBtn(content, top, Messages.get(WndSandbox.class, "stat_kill_all"), new ItemSprite(ItemSpriteSheet.SCROLL_HOLDER), () -> {
                for (Mob m : Dungeon.level.mobs.toArray(new Mob[0])) {
                    m.die(null);
                }
                GLog.w(Messages.get(WndSandbox.class, "log_kill_all"));
            });

            content.setSize(width, top);
            list.setSize(list.width(), list.height());
        }

        private float addActionBtn(Component content, float top, String label, Image icon, Runnable action) {
            RedButton btn = new RedButton(label) {
                @Override
                protected void onClick() {
                    action.run();
                }
            };
            btn.icon(icon);
            btn.setRect(2, top, width - 4, ITEM_HEIGHT);
            content.add(btn);
            return top + ITEM_HEIGHT + 2;
        }
    }

    // =========================================================
    // TRAPS
    // =========================================================
    private class TrapsTab extends Component {

        private ScrollingGridPane grid;

        @Override
        protected void createChildren() {
            grid = new ScrollingGridPane();
            add(grid);
        }

        @Override
        protected void layout() {
            super.layout();
            grid.setRect(x, y, width, height);
        }

        public void updateList() {
            grid.clear();
            grid.addHeader("_" + Messages.get(WndSandbox.class, "header_traps") + "_");

            for (Bestiary bestiary : Bestiary.values()) {
                for (Class<?> entityCls : bestiary.entities()) {
                    if (!Trap.class.isAssignableFrom(entityCls)) continue;

                    Trap trap = (Trap) Reflection.newInstance(entityCls);
                    Image icon = TerrainFeaturesTilemap.getTrapVisual(trap);

                    ScrollingGridPane.GridItem gridItem = new ScrollingGridPane.GridItem(icon) {
                        @Override
                        public boolean onClick(float x, float y) {
                            if (inside(x, y)) {
                                WndSandbox.this.hide();
                                selectTrapCell(entityCls, Messages.titleCase(trap.name()));
                                return true;
                            }
                            return false;
                        }
                    };
                    grid.addItem(gridItem);
                }
            }

            grid.setRect(x, y, width, height);
        }

        private void selectTrapCell(Class<?> trapCls, String trapName) {
            GameScene.selectCell(new CellSelector.Listener() {
                @Override
                public void onSelect(Integer cell) {
                    if (cell == null) return;
                    if (cell >= 0 && cell < Dungeon.level.length() && Dungeon.level.passable[cell]) {
                        Trap t = (Trap) Reflection.newInstance(trapCls);
                        t.pos = cell;
                        Dungeon.level.setTrap(t, cell);
                        Level.set(cell, Terrain.TRAP);
                        t.reveal();
                        GameScene.updateMap(cell);
                        GLog.i(Messages.get(WndSandbox.class, "trap_placed", trapName));
                        selectTrapCell(trapCls, trapName);
                    }
                }

                @Override
                public String prompt() {
                    return Messages.get(WndSandbox.class, "trap_prompt", trapName);
                }
            });
        }
    }

    // =========================================================
    // MOBS
    // =========================================================
    private class MobsTab extends Component {

        private ScrollingGridPane grid;

        @Override
        protected void createChildren() {
            grid = new ScrollingGridPane();
            add(grid);
        }

        @Override
        protected void layout() {
            super.layout();
            grid.setRect(x, y, width, height);
        }

        public void updateList() {
            grid.clear();

            for (Bestiary bestiary : Bestiary.values()) {
                if (bestiary == Bestiary.BOSSES) {
                    continue;
                }

                boolean hasMobs = false;
                for (Class<?> cls : bestiary.entities()) {
                    if (Mob.class.isAssignableFrom(cls)) {
                        hasMobs = true;
                        break;
                    }
                }
                if (!hasMobs) continue;

                grid.addHeader("_" + Messages.titleCase(bestiary.title()) + "_");

                for (Class<?> entityCls : bestiary.entities()) {
                    if (!Mob.class.isAssignableFrom(entityCls)) continue;

                    Mob mob = (Mob) Reflection.newInstance(entityCls);
                    if (mob.properties().contains(Char.Property.BOSS)) {
                        continue;
                    }
                    if (mob instanceof Mimic || mob instanceof Pylon || mob instanceof CrystalSpire) {
                        mob.alignment = Char.Alignment.ENEMY;
                    }
                    if (mob instanceof WandOfWarding.Ward) {
                        ((WandOfWarding.Ward) mob).upgrade(0);
                    }

                    CharSprite sprite = mob.sprite();
                    sprite.idle();
                    Image icon = new Image(sprite);

                    if (icon.width() >= 17 || icon.height() >= 17) {
                        RectF frame = icon.frame();
                        float wShrink = frame.width() * (1f - 17f / icon.width());
                        if (wShrink > 0) {
                            frame.left += wShrink / 2f;
                            frame.right -= wShrink / 2f;
                        }
                        float hShrink = frame.height() * (1f - 17f / icon.height());
                        if (hShrink > 0) {
                            frame.top += hShrink / 2f;
                            frame.bottom -= hShrink / 2f;
                        }
                        icon.frame(frame);
                    }

                    ScrollingGridPane.GridItem gridItem = new ScrollingGridPane.GridItem(icon) {
                        @Override
                        public boolean onClick(float x, float y) {
                            if (inside(x, y)) {
                                WndSandbox.this.hide();
                                selectMobCell(entityCls, Messages.titleCase(mob.name()));
                                return true;
                            }
                            return false;
                        }
                    };
                    grid.addItem(gridItem);
                }
            }

            grid.setRect(x, y, width, height);
        }

        private void selectMobCell(Class<?> mobCls, String mobName) {
            GameScene.selectCell(new CellSelector.Listener() {
                @Override
                public void onSelect(Integer cell) {
                    if (cell == null) return;
                    if (cell >= 0 && cell < Dungeon.level.length()
                            && Dungeon.level.passable[cell]
                            && Actor.findChar(cell) == null) {

                        Mob m = (Mob) Reflection.newInstance(mobCls);
                        if (m instanceof Mimic || m instanceof Pylon || m instanceof CrystalSpire) {
                            m.alignment = Char.Alignment.ENEMY;
                        }

                        if (m instanceof MirrorImage) {
                            ((MirrorImage) m).duplicate(Dungeon.hero);
                        } else if (m instanceof PrismaticImage) {
                            ((PrismaticImage) m).duplicate(Dungeon.hero, Dungeon.hero.lvl);
                        } else if (m instanceof WandOfWarding.Ward) {
                            if (m instanceof WandOfWarding.Ward.WardSentry) {
                                ((WandOfWarding.Ward) m).upgrade(3);
                                ((WandOfWarding.Ward) m).upgrade(3);
                                ((WandOfWarding.Ward) m).upgrade(3);
                                ((WandOfWarding.Ward) m).upgrade(3);
                            } else {
                                ((WandOfWarding.Ward) m).upgrade(0);
                            }
                        } else if (m instanceof WandOfRegrowth.Lotus) {
                            try {
                                java.lang.reflect.Method setLvl = WandOfRegrowth.Lotus.class.getDeclaredMethod("setLevel", int.class);
                                setLvl.setAccessible(true);
                                setLvl.invoke(m, 3);
                            } catch (Exception ignored) {
                                m.HP = m.HT = 100;
                            }
                        } else if (m instanceof SpiritHawk.HawkAlly) {
                            m.HP = m.HT = 30;
                        } else if (m instanceof Sheep) {
                            ((Sheep) m).initialize(20);
                        }

                        m.pos = cell;
                        if (m.alignment == Char.Alignment.ENEMY) {
                            m.state = m.HUNTING;
                        }
                        GameScene.add(m);
                        Dungeon.level.occupyCell(m);

                        GLog.i(Messages.get(WndSandbox.class, "mob_spawned", mobName));
                        selectMobCell(mobCls, mobName);
                    }
                }

                @Override
                public String prompt() {
                    return Messages.get(WndSandbox.class, "mob_prompt", mobName);
                }
            });
        }
    }
}