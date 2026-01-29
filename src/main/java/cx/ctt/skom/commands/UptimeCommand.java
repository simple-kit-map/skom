package cx.ctt.skom.commands;

import cx.ctt.skom.Main;
import net.minestom.server.command.builder.Command;

import java.util.concurrent.TimeUnit;

public class UptimeCommand extends Command {
    public UptimeCommand() {
        super("uptime");
        setDefaultExecutor((sender, context)-> {
            long timestamp = Main.STARTED_AT;

            long currentTime = System.currentTimeMillis();
            long duration = currentTime - timestamp;


            long days = TimeUnit.MILLISECONDS.toDays(duration);
            long hours = TimeUnit.MILLISECONDS.toHours(duration) % 24;
            long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60;
            long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60;

            StringBuilder timeMessage = new StringBuilder();
            boolean isFirst = true;

            if (days > 0) {
                timeMessage.append(days).append(" days");
                isFirst = false;
            }
            if (hours > 0) {
                if (!isFirst) timeMessage.append(", ");
                timeMessage.append(hours).append(" hours");
                isFirst = false;
            }
            if (minutes > 0) {
                if (!isFirst) timeMessage.append(", ");
                timeMessage.append(minutes).append(" minutes");
                isFirst = false;
            }
            if (seconds > 0 || (days == 0 && hours == 0)) { // Add seconds if all else is zero
                if (!isFirst) timeMessage.append(", ");
                timeMessage.append(seconds).append(" seconds");
            }

            sender.sendMessage(timeMessage.toString());
        });
    }
}
