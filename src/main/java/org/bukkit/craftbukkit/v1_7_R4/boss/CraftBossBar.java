package org.bukkit.craftbukkit.v1_7_R4.boss;

import io.github.crucible.bootstrap.CrucibleMetadata;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.List;

public class CraftBossBar implements BossBar {


    public CraftBossBar(String title, BarColor color, BarStyle style, BarFlag... flags) {}

    @Override
    public String getTitle() {
        throw new UnsupportedOperationException(CrucibleMetadata.NECRO_TEMPUS_REQUIRED);
    }

    @Override
    public void setTitle(String title) {
        throw new UnsupportedOperationException(CrucibleMetadata.NECRO_TEMPUS_REQUIRED);
    }

    @Override
    public BarColor getColor() {
        throw new UnsupportedOperationException(CrucibleMetadata.NECRO_TEMPUS_REQUIRED);
    }

    @Override
    public void setColor(BarColor color) {
        throw new UnsupportedOperationException(CrucibleMetadata.NECRO_TEMPUS_REQUIRED);
    }

    @Override
    public BarStyle getStyle() {
        throw new UnsupportedOperationException(CrucibleMetadata.NECRO_TEMPUS_REQUIRED);

    }

    @Override
    public void setStyle(BarStyle style) {
        throw new UnsupportedOperationException(CrucibleMetadata.NECRO_TEMPUS_REQUIRED);
    }

    @Override
    public void addFlag(BarFlag flag) {
        throw new UnsupportedOperationException(CrucibleMetadata.NECRO_TEMPUS_REQUIRED);
    }

    @Override
    public void removeFlag(BarFlag flag) {
        throw new UnsupportedOperationException(CrucibleMetadata.NECRO_TEMPUS_REQUIRED);
    }

    @Override
    public boolean hasFlag(BarFlag flag) {
        throw new UnsupportedOperationException(CrucibleMetadata.NECRO_TEMPUS_REQUIRED);
    }

    @Override
    public void setProgress(double progress) {
        throw new UnsupportedOperationException(CrucibleMetadata.NECRO_TEMPUS_REQUIRED);
    }

    @Override
    public double getProgress() {
        throw new UnsupportedOperationException(CrucibleMetadata.NECRO_TEMPUS_REQUIRED);
    }

    @Override
    public void addPlayer(Player player) {
        throw new UnsupportedOperationException(CrucibleMetadata.NECRO_TEMPUS_REQUIRED);
    }

    @Override
    public void removePlayer(Player player) {
        throw new UnsupportedOperationException(CrucibleMetadata.NECRO_TEMPUS_REQUIRED);
    }

    @Override
    public List<Player> getPlayers() {
        throw new UnsupportedOperationException(CrucibleMetadata.NECRO_TEMPUS_REQUIRED);
    }

    @Override
    public void setVisible(boolean visible) {
        throw new UnsupportedOperationException(CrucibleMetadata.NECRO_TEMPUS_REQUIRED);
    }

    @Override
    public boolean isVisible() {
        throw new UnsupportedOperationException(CrucibleMetadata.NECRO_TEMPUS_REQUIRED);
    }

    @Override
    public void show() {
        throw new UnsupportedOperationException(CrucibleMetadata.NECRO_TEMPUS_REQUIRED);
    }

    @Override
    public void hide() {
        throw new UnsupportedOperationException(CrucibleMetadata.NECRO_TEMPUS_REQUIRED);
    }

    @Override
    public void removeAll() {
        throw new UnsupportedOperationException(CrucibleMetadata.NECRO_TEMPUS_REQUIRED);

    }

}
