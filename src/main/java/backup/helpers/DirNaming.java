package backup.helpers;

import backup.config.Batch;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

// ---
// Note that files or directories may be:
//
// - qualified (absolute, when printed, containing a "/" on first position)
// - unqualified (relative to some other directory, when printed, containing no "/" on first position)
// - atomic (when printed, not containing any "/" at all)
// - indicated to be directories when printed by ending in a "/"
//
// - Represented by a java.io.File   (which is a name)
// - Represented by a String         (which is also a name, let's call it a "raw name")
//
// DestDir (as given on the command line)
//    |
//    |
//    +---- TargetDir (constructed from the current datetime, name liked "backup_${DATETIME}"
//              |
//              +---- logfile_${BATCH}.err
//              |
//              +---- logfile_${BATCH}.out
//              |
//              +---- DumpDir (called like copy_of_${BATCH})
//              |        |
//              |        +---- original1
//              |        |
//              |        +---- original2
//              |        |
//              |        +---- original3
//              |
//              +---- DumpDir (called like copy_of_${BATCH})
//              |
//              +---- DumpDir (called like copy_of_${BATCH})


public abstract class DirNaming {

    // https://docs.oracle.com/en/java/javase/16/docs/api/java.base/java/time/format/DateTimeFormatter.html

    public static String buildAtomicTargetDirRawName(@NotNull Instant when) {
        ZonedDateTime zonedWhen = ZonedDateTime.ofInstant(when, ZoneId.of("UTC"));
        String hostname = GetHostname.getHostname();
        if (hostname == null) {
            hostname = "MYSTERY";
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu_MM_dd_'UTC'_HH_mm");
        return "backup_(" + hostname + ")_(" + dtf.format(zonedWhen) + ")";
    }

    public static String buildAtomicDumpDirName(@NotNull Batch batch) {
        return "copy_of_" + batch.toString().toLowerCase();
    }

    public static String buildAtomicErrorFileName(@NotNull Batch batch) {
        return batch.toString().toLowerCase() + ".err";
    }

    public static String buildAtomicOutputFileName(@NotNull Batch batch) {
        return batch.toString().toLowerCase() + ".out";
    }

}
