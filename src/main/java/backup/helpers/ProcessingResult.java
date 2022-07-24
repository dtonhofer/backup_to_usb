package backup.helpers;

import backup.CmdLineDef;
import org.jetbrains.annotations.NotNull;

// ---
// This is the result of invoking command line process and business processing.
// The idea is that we want to propose something for the process' "exit value"
// and also return results of the processing so that certains things can be tested
// in JUnit code.
// ----

public class ProcessingResult {

    // The exit values are used as is in calls to System.exit.
    // 0 => success, everything went well
    // 1 => some kind of failure
    // 2 => command line usage error
    // We read at https://picocli.info/#_exception_exit_codes :
    // By default, the execute method returns CommandLine.ExitCode.OK (0) on success,
    // CommandLine.ExitCode.SOFTWARE (1) when an exception occurred in the Runnable,
    // Callable or command method, and CommandLine.ExitCode.USAGE (2) for invalid input.
    // (These are common values according to this StackOverflow answer).

    public enum ExitValue {

        HELP_REQUESTED(0, "User requested 'help' through command line option, as detected by PicoCli"),
        VERSION_REQUESTED(0, "User requested 'help' through command line option, as detected by PicoCli"),
        ARG_POSTPROCESSING_PROBLEM(2, "Problem found with command line arguments during argument postprocessing"),
        ARG_POSTPROCESSING_OK(-1, "Command line argument postprocessing yielded no error (internal)"),
        SKIPPED_BUSINESS_LOGIC(0, "For jUnit tests. Everything was done except the call to the business logic"),
        EXCEPTION_THROWN_BY_PICOCLI(2, "Picocli threw an exception, signaling that there was something wrong with the arguments"),
        EXCEPTION_THROWN_BY_BUSINESS_LOGIC(1, "The business logic part threw an exception"),
        RSYNC_SUCCEEDED(-1, "The just called 'rsync' process succeeded (internal)"),
        RSYNC_PARTIALLY_SUCCEEDED(-1, "The just called 'rsync' process succeeded partially (internal)"),
        RSYNC_FAILED(-1, "The just called 'rsync' process failed (internal)"),
        ALL_RSYNC_SUCCEEDED(0, "All 'rsync' calls succeeded"),
        ALL_RSYNC_FAILED(1, "All 'rsync' calls failed"),
        SOME_RSYNC_FAILED(1, "Some, but not all, of the 'rsync' calls failed"),
        STARTING_RSYNC_THREW_EXCEPTION(-1, "");

        public final int exitValue;
        public final String desc;

        ExitValue(int x, @NotNull String desc) {
            this.exitValue = x;
            this.desc = desc;
        }
    }

    @NotNull
    public final CmdLineDef cmdLineDef;
    public final ExitValue exitValue;

    public ProcessingResult(@NotNull CmdLineDef cmdLineDef, @NotNull ExitValue exitValue) {
        this.cmdLineDef = cmdLineDef;
        this.exitValue = exitValue;
    }
}
