/*
 * This file is licensed under the MIT License (MIT).
 *
 * Copyright (c) 2014 Daniel Ennis <http://aikar.co>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package co.aikar.timings;

import com.google.common.collect.ImmutableList;
import io.github.crucible.CrucibleConfigs;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.util.StringUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.jetbrains.annotations.NotNull;


public class TimingsCommand extends BukkitCommand {
    private static final List<String> TIMINGS_SUBCOMMANDS = ImmutableList.of("report", "reset", "on", "off", "paste", "verbon", "verboff", "timed", "timedverbose");
    private long lastResetAttempt = 0;
    public static final String PERMISSION_NODE = "bukkit.command.timings";

    private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
    private String getFormatedDate(Long millis){
        Date date = new Date(millis);
        return sdf.format(date);
    }

    public TimingsCommand(@NotNull String name) {
        super(name);
        this.description = "Manages Spigot Timings data to see performance of the server.";
        this.usageMessage = "/timings <reset|report|on|off|verbon|verboff|timed>";
        this.setPermission(PERMISSION_NODE);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String currentAlias, @NotNull String[] args) {
        if (!testPermission(sender)) {
            return true;
        }
        final String arg = args.length > 0 ? args[0] : "";

        TimedTimings timedTimings = TimedTimings.getCurrentTimedTimings();
        if (timedTimings != null){
            if (arg.equalsIgnoreCase("stop")){
                TimedTimings.interruptCurrent();
                sender.sendMessage(ChatColor.RED + " TimedTimings has stoped!");
                return true;
            }
            else if (arg.equalsIgnoreCase("off")){
                TimedTimings.interruptCurrent();
                Timings.setTimingsEnabled(false);
                sender.sendMessage(ChatColor.RED + " TimedTimings and the TimingsProfiller has stoped");
                return true;
            }else if (arg.equalsIgnoreCase("stats")){
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&a&l Timings Stats"));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&2   Start Time: &e" + getFormatedDate(TimingsManager.timingStart)));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&2   End Time: &e" + getFormatedDate(timedTimings.getEndTime())));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&2   Time Remaining: &3" + timedTimings.getSecondsTillEnds() + " seconds."));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&2   Verbose: &3" + Timings.verboseEnabled));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&2   Ultra-Verbose: &3" + CrucibleConfigs.configs.timings_ultraverbose_enabled));
                return true;
            }else if (arg.equalsIgnoreCase("cost")){
                sender.sendMessage("Timings cost: " + TimingsExport.getCost());
                return true;
            }
            sender.sendMessage(ChatColor.DARK_BLUE + "There is a timed-timings running!");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&c  - &e" + currentAlias + " stop &7- &aStop the timed-paste, but not the profiller."));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&c  - &e" + currentAlias + " off &7- &aStop the timed-paste and the profiller."));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&c  - &e" + currentAlias + " stats &7- &aGet time until paste."));
            return true;
        }

        if (arg.isEmpty()){
            sendHelp(sender,currentAlias,args);
            return true;
        }else if (arg.equalsIgnoreCase("stats")){
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&a&l Timings Stats"));
            if (Timings.timingsEnabled){
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&2   Enabled Since: &e" + getFormatedDate(TimingsManager.timingStart)));
            }else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&c   Not enabled!"));
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&2   Verbose: &3" + Timings.verboseEnabled));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&2   Ultra-Verbose: &3" + CrucibleConfigs.configs.timings_ultraverbose_enabled));
            return true;
        }else if ("on".equalsIgnoreCase(arg)) {
            Timings.setTimingsEnabled(true);
            sender.sendMessage("Enabled Timings & Reset");
            return true;
        } else if ("off".equalsIgnoreCase(arg)) {
            Timings.setTimingsEnabled(false);
            sender.sendMessage("Disabled Timings");
            return true;
        }else if ("timed".equalsIgnoreCase(arg) ||
                "timeout".equalsIgnoreCase(arg) ||
                "timedverbose".equalsIgnoreCase(arg) ||
                "timeoutverbose".equalsIgnoreCase(arg)){
            timedTimings(sender,currentAlias,args);
            return true;
        }


        if (!Timings.isTimingsEnabled()) {
            sender.sendMessage("Please enable timings by typing /timings on");
            return true;
        }

        if ("verbon".equalsIgnoreCase(arg)) {
            Timings.setVerboseTimingsEnabled(true);
            sender.sendMessage("Enabled Verbose Timings");
            return true;
        } else if ("verboff".equalsIgnoreCase(arg)) {
            Timings.setVerboseTimingsEnabled(false);
            sender.sendMessage("Disabled Verbose Timings");
            return true;
        } else if ("ultraverbon".equalsIgnoreCase(arg)) {
            CrucibleConfigs.configs.timings_ultraverbose_enabled = true;
            Timings.setVerboseTimingsEnabled(true);
            sender.sendMessage("Enabled Ultra-Verbose Timings");
            return true;
        } else if ("ultraverboff".equalsIgnoreCase(arg)) {
            CrucibleConfigs.configs.timings_ultraverbose_enabled = false;
            sender.sendMessage("Disabled Ultra-Verbose Timings");
            return true;
        } else if ("reset".equalsIgnoreCase(arg)) {
            long now = System.currentTimeMillis();
            if (now - lastResetAttempt < 30000) {
                TimingsManager.reset();
                sender.sendMessage(ChatColor.RED + "Timings reset. Please wait 5-10 minutes before using /timings report.");
            } else {
                lastResetAttempt = now;
                sender.sendMessage(ChatColor.RED + "WARNING: Timings v2 should not be reset. If you are encountering lag, please wait 3 minutes and then issue a report. The best timings will include 10+ minutes, with data before and after your lag period. If you really want to reset, run this command again within 30 seconds.");
            }
        } else if ("cost".equals(arg)) {
            sender.sendMessage("Timings cost: " + TimingsExport.getCost());
        } else if (
                "paste".equalsIgnoreCase(arg) ||
                        "report".equalsIgnoreCase(arg) ||
                        "get".equalsIgnoreCase(arg) ||
                        "merged".equalsIgnoreCase(arg) ||
                        "separate".equalsIgnoreCase(arg)
        ) {
            Timings.generateReport(sender);
        }else {
            sendHelp(sender,currentAlias,args);
        }
        return true;
    }

    private void timedTimings(@NotNull CommandSender sender, @NotNull String currentAlias, @NotNull String[] args) {
        if (args.length < 2){
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&c  - &e" + currentAlias + " " + args[0] + " <seconds>"));
            return;
        }

        Integer secondsToWait;
        try{
            secondsToWait = Integer.parseInt(args[1]);
            if (secondsToWait <= 0){
                sender.sendMessage(ChatColor.RED + "The <seconds> must be an integer positive!");
                return;
            }
        }catch (Exception e){
            sender.sendMessage(ChatColor.RED + "Invalid args, please specify a number!");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&c  - &e" + currentAlias + " " + args[0] + " <seconds>"));
            return;
        }
        TimedTimings.scheduleTimedTimings(secondsToWait);
        sender.sendMessage(ChatColor.GREEN + "Timings Scheduled!" + ChatColor.DARK_GREEN + " It will be pasted in " + ChatColor.YELLOW + secondsToWait + ChatColor.DARK_GREEN + " seconds!");
        return;
    }

    private void sendHelp(@NotNull CommandSender sender, @NotNull String currentAlias, @NotNull String[] args) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&c&m-----------------&c( &aTimings V.2 &7&oCrucible &c)&m-----------------"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&c  - &e" + currentAlias + " on &7- &aStart timings profiller."));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&c  - &e" + currentAlias + " paste &7- &aPaste current timings profiller."));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&c  - &e" + currentAlias + " reset &7- &aReset current timings profiller."));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&c  - &e" + currentAlias + " verbon &7- &aVerbose On. &7&otinyurl.com/wtf-is-verbose"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&c  - &e" + currentAlias + " verboff &7- &aVerbose Off. &7&otinyurl.com/wtf-is-verbose"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&c  - &e" + currentAlias + " ultraverbon" +
                "" +
                "" +
                " &7- &aUltraVerbose On. (Show tiles data)"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&c  - &e" + currentAlias + " ultraverboff &7- &aUltraVerbose Off"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&c  - &e" + currentAlias + " off &7- &aStop timings profiller."));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&c  - &e" + currentAlias + " timed <seconds> &7- &aStart, then paste, then stop."));
    }

    @NotNull
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(args, "Arguments cannot be null");
        Validate.notNull(alias, "Alias cannot be null");

        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], TIMINGS_SUBCOMMANDS,
                    new ArrayList<String>(TIMINGS_SUBCOMMANDS.size()));
        }
        return ImmutableList.of();
    }
}
