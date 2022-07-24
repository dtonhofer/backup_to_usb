package backup.config;

import backup.helpers.FilterRuleSeq;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

// ---
// Edit this class to configure what to include, what to exclude
// It should actually be created by a configuration file or a
// Groovy file so that one does not need to recompile.
//
// This class could have only static members, but we choose to
// have an instance instead, so that this configuration can be
// passed around (although unnecessarily)
//
// The problem is the "Batch" enum which, though practical, is essentially
// a hardcoded limitation of the at least the set of Batches.
//
// Additionally, the command line arguments that select the batches are
// also hardcoded, not read from a list at runtime for example.
//
// For information, look at the FILTER RULES and INCLUDE/EXCLUDE PATTERN RULES
// of rsync(1)
//
// Can this be made more flexible so as to read a config file instead?
// ---

public enum Batch {

    LARGE_BUT_STABLE(
            new String[]{"/home/attic/nsfw/","/home/attic/audiofiles/","/home/backups/","/home/osimages"}
    ),

    FAST_CHANGERS(
            new String[]{"/home/aloy/","/home/rost/","/home/wikis/"},
            new FilterRuleSeq("-.m2", "-/aloy/.cache/", "-/rost/.cache/", "-/aloy/.gradle/caches/")
    ),

    // Exclude "/nsfw" and "/audiofiles" relative to "the root of the transfer" i.e "/home/attic"
    // This filtering is not fully intuitive and one has to take a look at the verbose output to ascertain
    // that it actually works.

    REST_OF_ATTIC(
            new String[]{"/home/attic/"},
            new FilterRuleSeq("-/attic/nsfw/", "-/attic/audiofiles/")
    ),

    // interesting parts of the system; one should separately back up the main disk
    // the wildcard-using exclude notation creates the directories, but nothing underneath

    SYSTEM(
            new String[]{"/"},
            new FilterRuleSeq("-/home/*", "-/attic/*", "-/var/vaults/*", "-/run/*", "-/tmp/*", "-/dev/*", "-/sys/*", "-/proc/*", "-/root/*")
    );

    // ---
    // Constructor
    // ---

    Batch(@NotNull String[] sourcesIn) {
        this(sourcesIn, new FilterRuleSeq(){});
    }

    Batch(@NotNull String[] sourcesIn, @NotNull FilterRuleSeq seq) {
        SortedSet<File> tmpSources = new TreeSet<>();
        for (String src : sourcesIn) {
            tmpSources.add(new File(src));
        }
        if (tmpSources.isEmpty()) {
            throw new IllegalArgumentException("No sources have been passed");
        }
        this.sources = Collections.unmodifiableSortedSet(tmpSources);
        this.seq = seq;
    }

    // ---
    // Members of enum
    // ---

    @NotNull
    public final SortedSet<File> sources;

    @NotNull
    public final FilterRuleSeq seq;

    // ---
    // A replacement for valueOf() which works leniently.
    // The "batch" may be a "super-batch", in that case the returned
    // Set<Batch> will contain several "Batch" instances.
    // ---

    public static Set<Batch> myValueOf(String batchAsStr) {
        if (batchAsStr == null) {
            throw new IllegalArgumentException("The passed string is (null)");
        }
        String xclean = batchAsStr.trim().toLowerCase();
        Set<Batch> res = new HashSet<>();
        switch (xclean) {
            case "large_but_stable":
                res.add(Batch.LARGE_BUT_STABLE);
                break;
            case "fast_changers":
                res.add(Batch.FAST_CHANGERS);
                break;
            case "rest_of_attic":
                res.add(Batch.REST_OF_ATTIC);
                break;
            case "system":
                res.add(Batch.SYSTEM);
                break;
            case "all_except_large_but_stable":
                res.add(Batch.SYSTEM);
                res.add(Batch.REST_OF_ATTIC);
                res.add(Batch.FAST_CHANGERS);
                break;
            case "all":
                res.add(Batch.SYSTEM);
                res.add(Batch.REST_OF_ATTIC);
                res.add(Batch.FAST_CHANGERS);
                res.add(Batch.LARGE_BUT_STABLE);
                break;
            default:
                throw new IllegalArgumentException("There is no batch named '" + batchAsStr + "'");
        }
        return res;
    }
}
