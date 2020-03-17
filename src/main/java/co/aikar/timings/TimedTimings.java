package co.aikar.timings;

import io.github.crucible.CrucibleModContainer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TimedTimings extends Thread{

    public static TimedTimings timedTimings = null;

    public static boolean isRunning(){
        return timedTimings != null && timedTimings.isAlive();
    }

    public static TimedTimings getCurrentTimedTimings(){
        return isRunning() ? timedTimings : null;
    }

    public static void interruptCurrent(){
        if (isRunning()) timedTimings.interrupt();
    }

    public final int waitSeconds;
    public final long startTime;
    public long endTime = 0;

    public long getStartTime(){
        return this.startTime;
    }

    public long getEndTime(){
        return endTime;
    }

    public long getSecondsTillEnds(){
        long secondsLeft = TimeUnit.MILLISECONDS.toSeconds(endTime - System.currentTimeMillis());
        return secondsLeft > 0 ? secondsLeft : 0;
    }

    private TimedTimings(int waitSeconds) {
        this.waitSeconds = waitSeconds;
        this.startTime = System.currentTimeMillis();
    }

    public static void scheduleTimedTimings(int waitSeconds){
        interruptCurrent();
        timedTimings = new TimedTimings(waitSeconds);
        timedTimings.setName("Crucible Timed TimingsV2");
        timedTimings.setDaemon(true);
        timedTimings.start();
    }

    private void runSync(Runnable runnable){
        new BukkitRunnable(){
            @Override
            public void run() {
                runnable.run();
            }
        }.runTask(CrucibleModContainer.instance);
    }

    @Override
    public void run() {
        try{
            runSync(() -> Timings.setTimingsEnabled(true)); //Start

            long millisToWait = TimeUnit.SECONDS.toMillis(waitSeconds);
            this.endTime = System.currentTimeMillis() + millisToWait;
            Thread.sleep(millisToWait); //then sleep

            List<CommandSender> staffPlayers = new ArrayList<>();
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.hasPermission(TimingsCommand.PERMISSION_NODE)){
                    staffPlayers.add(onlinePlayer);
                }
            }
            TimingsReportListener timingsReportListener = new TimingsReportListener(staffPlayers, () -> {
                Timings.setTimingsEnabled(false);//then Stop
            });

            runSync(() -> Timings.generateReport(timingsReportListener)); //then Paste
        }catch (InterruptedException ignored){
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
