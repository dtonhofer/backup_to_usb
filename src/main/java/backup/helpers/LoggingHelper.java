package backup.helpers;

import backup.config.Batch;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Optional;
import java.util.SortedMap;

// ---
// Small methods used for logging
// ---

public abstract class LoggingHelper {

    public static String stringifyRsyncCmds(@NotNull SortedMap<Batch, RsyncCmd> map) {
        StringBuilder buf = new StringBuilder();
        boolean addEOL = false;
        for (Batch batch : map.keySet()) {
            if (addEOL) {
                buf.append(("\n"));
            }
            else {
                addEOL = true;
            }
            RsyncCmd cmd = map.get(batch);
            // header
            buf.append(batch);
            buf.append(("\n"));
            // multiline NL-less rsync command, indented, NOT followed by NL
            buf.append(cmd.toMultilineString(4));
        }
        return buf.toString();
    }

    public static String stringifyCpuDuration(@NotNull Process p) {
        Optional<Duration> cpu = p.info().totalCpuDuration();
        if (cpu.isPresent()) {
            // https://docs.oracle.com/en/java/javase/16/docs/api/java.base/java/time/Duration.html
            return "CPU duration: " + DurationPrinter.formatDuration(cpu.get());
        }
        else {
            return "[No CPU duration information]";
        }
    }

}
