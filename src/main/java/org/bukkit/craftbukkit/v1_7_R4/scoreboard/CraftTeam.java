package org.bukkit.craftbukkit.v1_7_R4.scoreboard;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.Team;

import java.util.Set;


final class CraftTeam extends CraftScoreboardComponent implements Team {
    private final net.minecraft.scoreboard.ScorePlayerTeam team;

    CraftTeam(CraftScoreboard scoreboard, net.minecraft.scoreboard.ScorePlayerTeam team) {
        super(scoreboard);
        this.team = team;
        scoreboard.teams.put(team.getRegisteredName(), this);
    }

    public String getName() throws IllegalStateException {
        CraftScoreboard scoreboard = checkState();

        return team.getRegisteredName();
    }

    public String getDisplayName() throws IllegalStateException {
        CraftScoreboard scoreboard = checkState();

        return team.func_96669_c();
    }

    public void setDisplayName(String displayName) throws IllegalStateException {
        Validate.notNull(displayName, "Display name cannot be null");
        Validate.isTrue(displayName.length() <= 32, "Display name '" + displayName + "' is longer than the limit of 32 characters");
        CraftScoreboard scoreboard = checkState();

        team.setTeamName(displayName);
    }

    public String getPrefix() throws IllegalStateException {
        CraftScoreboard scoreboard = checkState();

        return team.getColorPrefix();
    }

    public void setPrefix(String prefix) throws IllegalStateException, IllegalArgumentException {
        Validate.notNull(prefix, "Prefix cannot be null");
        Validate.isTrue(prefix.length() <= 32, "Prefix '" + prefix + "' is longer than the limit of 32 characters");
        CraftScoreboard scoreboard = checkState();

        team.setNamePrefix(prefix);
    }

    public String getSuffix() throws IllegalStateException {
        CraftScoreboard scoreboard = checkState();

        return team.getColorSuffix();
    }

    public void setSuffix(String suffix) throws IllegalStateException, IllegalArgumentException {
        Validate.notNull(suffix, "Suffix cannot be null");
        Validate.isTrue(suffix.length() <= 32, "Suffix '" + suffix + "' is longer than the limit of 32 characters");
        CraftScoreboard scoreboard = checkState();

        team.setNameSuffix(suffix);
    }

    public boolean allowFriendlyFire() throws IllegalStateException {
        CraftScoreboard scoreboard = checkState();

        return team.getAllowFriendlyFire();
    }

    public void setAllowFriendlyFire(boolean enabled) throws IllegalStateException {
        CraftScoreboard scoreboard = checkState();

        team.setAllowFriendlyFire(enabled);
    }

    public boolean canSeeFriendlyInvisibles() throws IllegalStateException {
        CraftScoreboard scoreboard = checkState();

        return team.func_98297_h();
    }

    public void setCanSeeFriendlyInvisibles(boolean enabled) throws IllegalStateException {
        CraftScoreboard scoreboard = checkState();

        team.setSeeFriendlyInvisiblesEnabled(enabled);
    }

    public Set<OfflinePlayer> getPlayers() throws IllegalStateException {
        CraftScoreboard scoreboard = checkState();

        ImmutableSet.Builder<OfflinePlayer> players = ImmutableSet.builder();
        for (Object o : team.getMembershipCollection()) {
            players.add(Bukkit.getOfflinePlayer(o.toString()));
        }
        return players.build();
    }

    public int getSize() throws IllegalStateException {
        CraftScoreboard scoreboard = checkState();

        return team.getMembershipCollection().size();
    }

    public void addPlayer(OfflinePlayer player) throws IllegalStateException, IllegalArgumentException {
        Validate.notNull(player, "OfflinePlayer cannot be null");
        CraftScoreboard scoreboard = checkState();

        scoreboard.board.func_151392_a(player.getName(), team.getRegisteredName());
    }

    public boolean removePlayer(OfflinePlayer player) throws IllegalStateException, IllegalArgumentException {
        Validate.notNull(player, "OfflinePlayer cannot be null");
        CraftScoreboard scoreboard = checkState();

        if (!team.getMembershipCollection().contains(player.getName())) {
            return false;
        }

        scoreboard.board.removePlayerFromTeam(player.getName(), team);
        return true;
    }

    public boolean hasPlayer(OfflinePlayer player) throws IllegalArgumentException, IllegalStateException {
        Validate.notNull(player, "OfflinePlayer cannot be null");
        CraftScoreboard scoreboard = checkState();

        return team.getMembershipCollection().contains(player.getName());
    }

    @Override
    public void unregister() throws IllegalStateException {
        CraftScoreboard scoreboard = checkState();

        scoreboard.board.removeTeam(team);
        scoreboard.teams.remove(team.getRegisteredName());
        setUnregistered();
    }
}
