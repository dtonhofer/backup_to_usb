package backup;

import backup.config.Batch;
import backup.helpers.ProcessingResult;
import org.jetbrains.annotations.NotNull;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.*;

// ---
// The command supports -h/--help (help) and -V/--version (version print)
// through the "mixinStandardHelpOptions" annotation
//
// Info at:
//
// https://docs.oracle.com/en/java/javase/16/docs/api/java.base/java/util/concurrent/Callable.html
// https://picocli.info/
// ---

@Command(name = "backup_to_usb",
        version = "backup_to_usb 0.1",  // TODO: How do you update the version number automatically?
        mixinStandardHelpOptions = true,
        usageHelpWidth = 100,
        description = "Copies certain parts of the system's file tree using rsync(1). Meant to be used for backups")
public class CmdLineDef {

    private final static String CLASS = CmdLineDef.class.getName();

    public final static String DRYRUN = "--dryrun";
    public final static String VERBOSE = "--verbose";
    public final static String WITH = "--with";

    @Parameters(index = "0", description = "Destination directory under which the backup target directory will be created.", paramLabel = "DESTDIR")
    private String destDirAsStr;

    @Option(names = {VERBOSE}, description = "Switch on 'rsync' verbosity.")
    private boolean verbose;

    @Option(names = {DRYRUN, "--dry-run"}, description = "Just perform a rsync 'dryrun', do not really copy anything.")
    private boolean dryrun;

    // --
    // This description has to be sadly updated manually whenever the backup
    // options are rearranged. There must be a better way
    // ---

    @Option(names = {WITH}, description =
            "Include some batch in backup.\n" +
                    "One of: 'large_but_stable', 'fast_changers', 'rest_of_attic', 'system'.\n" +
                    "There is also 'all_except_large_but_stable' and 'all', which do what they say.")
    private String[] batches;

    // ---
    // These are filled by "argPostprocessing()".
    // The set of "batch" is a "SortedSet" to get a consistent order.
    // The "Instant" is used to generate filenames. It currently cannot be set from the command line.
    // ---

    private File destDir;
    private SortedSet<Batch> batchSet;
    private final Instant when = Instant.now();

    // ---
    // If the constructor is called with doNothing = true, processing via a picocli
    // CommandLine will result in no real action, which is perfect for tests.
    // ---

    private void argPostprocessing_TargetDir(List<String> problems) {
        assert destDirAsStr != null;
        this.destDir = new File(destDirAsStr);
        if (!destDir.exists()) {
            problems.add("The target directory '" + destDirAsStr + "' does not exist!");
        }
        else if (!destDir.isDirectory()) {
            problems.add("The target directory '" + destDirAsStr + "' exists but is not a directory!");
        }
        else if (!destDir.canWrite() || !destDir.canRead() || !destDir.canExecute()) {
            problems.add("The target directory '" + destDirAsStr + "' exists, is a directory, but cannot be read, written or listed!");
        }
    }

    private void argPostprocessing_Batches(List<String> problems) {
        SortedSet<Batch> tmpBatchSet = new TreeSet<>();
        if (batches != null) {
            for (String batchAsStr : batches) {
                try {
                    Set<Batch> batchSet = Batch.myValueOf(batchAsStr);
                    tmpBatchSet.addAll(batchSet);
                } catch (IllegalArgumentException ex) {
                    problems.add("Bad 'batch' value: " + ex.getMessage());
                }
            }
        }
        if (tmpBatchSet.isEmpty()) {
            // Actually this can't happen as already checked by PicoCli
            problems.add("No batch to include. Add at least one of the '--with <batch>' arguments!");
        }
        this.batchSet = Collections.unmodifiableSortedSet(tmpBatchSet);
    }

    // ---
    // Once the arguments have been parsed, we do additional checks
    // Print to "PrintWriter", not STDERR
    // ---

    public ProcessingResult.ExitValue argPostprocessing(@NotNull PrintWriter stderr) {
        List<String> problems = new LinkedList<>();
        argPostprocessing_TargetDir(problems);
        argPostprocessing_Batches(problems);
        if (!problems.isEmpty()) {
            printListOfProblemsFound(problems,stderr);
            // Consider the problem to be "with" or "fall under the purview of" USAGE
            // as it makes sense that the user reading the "usage" may fix things
            return ProcessingResult.ExitValue.ARG_POSTPROCESSING_PROBLEM;
        }
        else {
            return ProcessingResult.ExitValue.ARG_POSTPROCESSING_OK;
        }
    }

    private void printListOfProblemsFound(List<String> problems,@NotNull PrintWriter stderr) {
        for (String str : problems) {
            stderr.println(str);
        }
        stderr.println("Run the command with '--help' to obtain description of command usage.");
    }

    // ---
    // Getter
    // ---

    public boolean isDryrun() {
        return dryrun;
    }

    public boolean isVerbose() {
        return verbose;
    }

    // ---
    // Get the unmodifiable "sorted set" containing the "batches", which is set in
    // argPostprocessing() and thus not null only after that call.
    // ---

    public SortedSet<Batch> getBatchSet() {
        return batchSet;
    }

    // ---
    // Get the target directory extracted by argument processing,, which is set in
    // argPostprocessing() and thus not null only after that call.
    // ---

    public File getDestDir() {
        return destDir;
    }

    // ---
    // Get the non-null "now" time
    // ---

    public Instant getWhen() {
        return when;
    }
}
