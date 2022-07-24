package backup;

import backup.config.Batch;
import backup.helpers.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

// ---
// Extremely simple "main" which just calls picocli.CommandLine with a
// an instance of a class carrying all required annotations, here called
// "CmdLineDefiner". It's kind of like a Java Bean, but with annotations
// instead of getters and setters.
// ---

// ---
// In order to build an "uberjar" see
// https://blog.jetbrains.com/idea/2010/08/quickly-create-jar-artifact/
//
// It's defined in File -> Project Structure -> (Project Settings -> Artifact), where you can define it.
//
// After definition, go to Build -> Build Artifacts
// and the "uberjar" will be in ~aloy/IdeaProjects/backup_to_usb/out/artifacts/backup_to_usb_jar/backup_to_usb.jar
//
// In order to do it via maven, use the "Apache Maven Shade Plugin":
//
// https://maven.apache.org/plugins/maven-shade-plugin/index.html
// ---

public abstract class Mainer {

    private final static String CLASS = Mainer.class.getName();

    // A "typed boolean"

    public enum SkipBizLogic {True, False};

    private static ProcessingResult.ExitValue runRsyncCmd(@NotNull RsyncCmd cmd, @NotNull Batch batch, @NotNull CmdLineDef cdf, @NotNull File targetDirName) {
        Logger logger = LoggerFactory.getLogger(CLASS + ".runRsyncCmd");
        ProcessBuilder pb = new ProcessBuilder(cmd.pull());
        // The process will have the "target directory" as current directory
        pb.directory(targetDirName);
        // Interestingly 6the above seems to not work. The unqualified files for STDERR and STDOUT appear
        // in the current process's working directory (well, maybe that's expected). We thus have to qualify
        // these files fully.
        // Should we delete them afterwards?
        pb.redirectError(new File(targetDirName,DirNaming.buildAtomicErrorFileName(batch)));
        pb.redirectOutput(new File(targetDirName,DirNaming.buildAtomicOutputFileName(batch)));
        try {
            Instant startInstant = Instant.now();
            // https://docs.oracle.com/en/java/javase/16/docs/api/java.base/java/lang/Process.html
            Process p = pb.start();
            logger.info("Rsync process " + p.pid() + " handling batch '" + batch + "' has started");
            logger.info(cmd.toMultilineString(0));
            while (p.isAlive()) {
                try {
                    p.waitFor();
                } catch (InterruptedException ex2) {
                    // may have been woken up early!
                }
            }
            Instant stopInstant = Instant.now();
            RsyncRetVal rrv = RsyncRetVal.byId(p.exitValue());
            String rrvDesc = (rrv == null) ? "unknown id" : rrv.getDesc();
            RsyncVerdict verdict = (rrv == null) ? RsyncVerdict.FAILURE : rrv.getRsyncVerdict();
            logger.info("Rsync process for batch '" + batch + "' ended");
            logger.info("Exit value         : " + p.exitValue() + " " + rrvDesc + " -> " + verdict);
            logger.info("CPU duration       : " + LoggingHelper.stringifyCpuDuration(p)); // this seems to not yield any good info
            logger.info("Wallclock duration : " + DurationPrinter.formatDuration(Duration.between(startInstant,stopInstant)));
            switch (verdict) {
                case SUCCESS -> { return ProcessingResult.ExitValue.RSYNC_SUCCEEDED; }
                case FAILURE -> { return ProcessingResult.ExitValue.RSYNC_FAILED; }
                case PARTIAL_SUCCESS -> { return ProcessingResult.ExitValue.RSYNC_PARTIALLY_SUCCEEDED; }
                default -> { throw new IllegalStateException("Unknown verdict " + verdict); }
            }
        }
        catch (IOException ex) {
            logger.error("Processing of batch '" + batch + "' ended with an exception", ex);
            return ProcessingResult.ExitValue.STARTING_RSYNC_THREW_EXCEPTION;
        }
        // TODO: Update the "data directory" listing the disk contents
    }

    // ---
    // Create the target directory inside the destination directory.
    // The target directory is target for the file trees created by rsync.
    // ---

    private static File createTargetDir(@NotNull CmdLineDef cdf) {
        Logger logger = LoggerFactory.getLogger(CLASS + ".createTargetDir");
        File qualTargetDirName = new File(cdf.getDestDir(), DirNaming.buildAtomicTargetDirRawName(cdf.getWhen()));
        // The old-school "mkdir" does not allow to set permissions and does not say what went wrong
        // Use Files.createDirectory() for that.
        boolean created = qualTargetDirName.mkdir();
        if (!created) {
            throw new IllegalStateException("Could not create the target directory '" + qualTargetDirName + "'");
        }
        if (!qualTargetDirName.exists()) {
            throw new IllegalStateException("The target directory '" + qualTargetDirName + "' does not exist after creation");
        }
        if (!qualTargetDirName.isDirectory()) {
            throw new IllegalStateException("The target directory '" + qualTargetDirName + "' exists but is not a directory");
        }
        logger.info("The target directory '" + qualTargetDirName + "' has been created successfully");
        return qualTargetDirName;
    }

    private static ProcessingResult.ExitValue doBizProcessing(@NotNull CmdLineDef cdf) {
        Logger logger = LoggerFactory.getLogger(CLASS + ".doBizProcessing");
        File qualTargetDirName = createTargetDir(cdf);
        SortedMap<Batch, RsyncCmd> cmds = RsyncCmdBuilder.buildAllRsyncCmds(RsyncCmd.RSYNC_EXE_NAME,cdf,qualTargetDirName);
        if (logger.isInfoEnabled()) {
            logger.info(LoggingHelper.stringifyRsyncCmds(cmds));
        }
        float successCount = 0;
        for (Batch batch : cmds.keySet()) {
            ProcessingResult.ExitValue res = runRsyncCmd(cmds.get(batch), batch, cdf, qualTargetDirName);
            if (ProcessingResult.ExitValue.RSYNC_SUCCEEDED == res) {
                successCount += 1.0;
            }
            else if (ProcessingResult.ExitValue.RSYNC_PARTIALLY_SUCCEEDED == res ) {
                successCount += 0.5;
            }
        }
        if (successCount == cmds.size()) {
            return ProcessingResult.ExitValue.ALL_RSYNC_SUCCEEDED;
        }
        else if (successCount == 0) {
            return ProcessingResult.ExitValue.ALL_RSYNC_FAILED;
        }
        else {
            return ProcessingResult.ExitValue.SOME_RSYNC_FAILED;
        }
    }

    // ---
    // Do "command line postprocessing", verifying additional conditions,
    // and then invoke the biz logic
    // ---

    private static @NotNull ProcessingResult.ExitValue doArgPostprocessingAndBizProcessing(@NotNull CmdLineDef cld, @NotNull SkipBizLogic skip, @NotNull PrintWriter stderr) {
        Logger logger = LoggerFactory.getLogger(CLASS + ".doArgPostprocessingAndBizProcessing");
        ProcessingResult.ExitValue res = cld.argPostprocessing(stderr);
        if (ProcessingResult.ExitValue.ARG_POSTPROCESSING_OK.equals(res)) {
            if (SkipBizLogic.False == skip) {
                try {
                    res = doBizProcessing(cld);
                }
                catch (Exception ex) {
                    logger.error("Business logic threw exception", ex);
                    res = ProcessingResult.ExitValue.EXCEPTION_THROWN_BY_BUSINESS_LOGIC;
                }
            }
            else {
                // it's a junit test, skip everything!
                res = ProcessingResult.ExitValue.SKIPPED_BUSINESS_LOGIC;
            }
        }
        return res;
    }

    // ---
    // Invoke command line processing and eventually business logic (if all goes well)
    // without a replacement for STDERR to capture messages by the PicoCli command line
    // processing
    // ---

    public static @NotNull ProcessingResult doCmdLineAndBizProcessing(@NotNull String[] args, @NotNull SkipBizLogic skip) {
        return doCmdLineAndBizProcessing(args, skip, null);
    }

    // ---
    // Do command line processing and eventually business processing.
    // WITH a replacement for STDERR to capture messages by the PicoCli command line
    // processing (may be null)
    // See https://picocli.info/#_diy_command_execution
    // ---

    public static @NotNull ProcessingResult doCmdLineAndBizProcessing(@NotNull String[] args, @NotNull SkipBizLogic skip, PrintWriter stderrReplacement) {
        CmdLineDef cld = new CmdLineDef();
        CommandLine cmdLine = new CommandLine(cld);
        // In the case of jUnit tests, stderr will be captured by a "stderr replacement"
        PrintWriter stderr = (stderrReplacement == null ? cmdLine.getErr() : stderrReplacement);
        assert stderr != null;
        ProcessingResult.ExitValue exitValue = null;
        try {
            // parseArgs() deposits the results of parse into the at-construction-time-passed "cld"
            // parseArgs() returns a "CommandLine.ParseResult" but we don't need that as
            // we can interrogate "cmdLine" and "cld"
            cmdLine.parseArgs(args);
            if (cmdLine.isUsageHelpRequested()) {
                // user requested usage help; use the PrintWriter of "cmdLine" to print
                cmdLine.usage(stderr);
                exitValue = ProcessingResult.ExitValue.HELP_REQUESTED;
            } else if (cmdLine.isVersionHelpRequested()) {
                // user request version; use the PrintWriter of "cmdLine" to print
                cmdLine.printVersionHelp(stderr);
                exitValue = ProcessingResult.ExitValue.VERSION_REQUESTED;
            } else {
                // ---->
                exitValue = doArgPostprocessingAndBizProcessing(cld, skip, stderr);
                // <----
            }
        } catch (CommandLine.ParameterException ex) {
            // invalid user input: print error message to stderr or what currently captures it
            // and follow up with usage help
            stderr.println(ex.getMessage());
            if (!CommandLine.UnmatchedArgumentException.printSuggestions(ex, stderr)) {
                // use the PrintWriter of "cmdLine" to print error string
                cmdLine.usage(stderr);
            }
            exitValue = ProcessingResult.ExitValue.EXCEPTION_THROWN_BY_PICOCLI;
        }
        assert exitValue != null;
        return new ProcessingResult(cld,exitValue);
    }

    // ---
    // Entry point
    // ---

    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(CLASS + ".main");
        ProcessingResult pr = doCmdLineAndBizProcessing(args, SkipBizLogic.False);
        logger.info("At the end of processing. Obtained the exit value: " + pr.exitValue);
        if (pr.exitValue.exitValue < 0) {
            logger.error("The exit value " + pr.exitValue + " is not expected to be seen here");
            System.exit(1);
        }
        else {
            System.exit(pr.exitValue.exitValue);
        }
    }

}
