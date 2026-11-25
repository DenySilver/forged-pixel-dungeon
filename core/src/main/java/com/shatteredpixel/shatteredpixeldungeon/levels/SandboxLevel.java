package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.watabou.utils.Bundle;

import java.util.Arrays;

public class SandboxLevel extends Level {

    {
        color1 = 0x48763c;
        color2 = 0x59994a;
    }

    @Override
    public String tilesTex() {
        return Assets.Environment.TILES_SEWERS;
    }

    @Override
    public String waterTex() {
        return Assets.Environment.WATER_SEWERS;
    }

    @Override
    protected boolean build() {
        setSize(32, 32);
        Arrays.fill(map, Terrain.EMPTY);

        for (int i = 0; i < length(); i++) {
            int x = i % width();
            int y = i / width();
            if (x == 0 || x == width() - 1 || y == 0 || y == height() - 1) {
                map[i] = Terrain.WALL;
            }
        }

        int center = width() * (height() / 2) + (width() / 2);
        map[center] = Terrain.ENTRANCE;
        transitions.add(new LevelTransition(this, center, LevelTransition.Type.REGULAR_ENTRANCE));

        return true;
    }

    @Override
    protected void createMobs() {}

    @Override
    protected void createItems() {}

    @Override
    public Mob createMob() {
        return null;
    }

    @Override
    public float respawnCooldown() {
        return Float.MAX_VALUE;
    }
}