package backup.helpers;

import backup.config.Batch;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

// ---
// A class that holds the Strings composing an rsync command.
// These strings will be used to start the rsync process via a Java Process Builder
// https://docs.oracle.com/en/java/javase/16/docs/api/java.base/java/lang/ProcessBuilder.html
// ---

public class RsyncCmd {

    // rsync has complex verbosity options, for now just have Off and Normal

    public enum Verbosity {Off, Normal}

    // "dry run" is a flag passed to rsync to tell whether to do something or not
    // This is used instead of a boolean for better typing.

    public enum DryRun { Off, On }

    // this list accumulates the substrings of the command

    private final List<String> params = new LinkedList<>();

    // standard executable

    public static final String RSYNC_EXE_NAME = "/usr/bin/rsync";

    // ---
    // Constructor also takes the executable name
    // ----

    public RsyncCmd(@NotNull String rsyncExeName, @NotNull Batch batch, @NotNull DryRun dryRun, @NotNull Verbosity verbosity) {
        params.add(rsyncExeName);
        if (verbosity == Verbosity.Normal) {
            params.add("--verbose"); // verbosity can be very fine-tuned in rsync, this simply switches on logging of transferred files
        }
        if (dryRun == DryRun.On) {
            params.add("--dry-run"); // perform a trial run with no changes made
        }
        params.add("--archive"); // archive mode is -rlptgoD (no -A (--acls),-X (--xattrs),-U (--atimes),-N (--crtimes),-H (--hard-links))
        // not sure about any of these (especially the "hard links", how do you even detect them; muts be for Windows?)
        // params.add("--hard-links"); // preserve hard links
        params.add("--acls"); // preserve ACLs (implies --perms)
        params.add("--xattrs");  // preserve extended attributes
        params.add("--backup"); // make backups (see --suffix & --backup-dir): preexisting destination files are renamed as each file is transferred or deleted
        // params.add("--one-file-system"); // it would make sense to make this depend on the batch
        // params.add("--crtimes"); // preserve create times (newness); create time does not exist on Linux
        // In POSIX we have:
        // https://www.howtogeek.com/517098/linux-file-timestamps-explained-atime-mtime-and-ctime/
        // - The access timestamp is the last time a file was read. This means someone used a program
        //   to display the contents of the file or read some values from it. Nothing was edited or added
        //   to the file. The data was referenced but unchanged.
        // - A modified timestamp signifies the last time the contents of a file were modified. A
        //   program or process either edited or manipulated the file. “Modified” means something
        //   inside the file was amended or deleted, or new data was added.
        // - Changed timestamps aren’t referring to changes made to the contents of a file. Rather,
        //   it’s the time at which the metadata related to the file was changed. File permission changes,
        //   for example, will update the changed timestamp.
        // Additionally, a "birth date" exists in BSD and in externded attributes of Linux
        // (It *should* be picked up by "backup")
        //  https://askubuntu.com/questions/918300/when-is-birth-date-for-a-file-actually-used
    }

    // ---
    // More parameters must be added to the rump command; use this!
    // ---

    public void addParam(@NotNull String x) {
        params.add(x);
    }

    // ---
    // Return a deep-copy of the rsync command set up os far
    // ---

    public List<String> pull() {
        List<String> res = new ArrayList<>(params.size());
        res.addAll(this.params);
        return res;
    }

    // ---
    // Make a string (without a terminating EOL)
    // ---

    public String toMultilineString(int indent) {
        StringBuilder buf = new StringBuilder();
        boolean addEOL = false;
        for (String str : params) {
            if (addEOL) {
                buf.append("\n");
                buf.append("   ");
            } else {
                addEOL = true;
            }
            addIndentation(buf,indent);
            buf.append(str);
        }
        return buf.toString();
    }

    private void addIndentation(StringBuilder buf, int indent) {
        for (int i=0;i<indent;i++) {
            buf.append(" ");
        }
    }
}
