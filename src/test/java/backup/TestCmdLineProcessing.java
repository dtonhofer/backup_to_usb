package backup;

import static backup.CmdLineDef.*;
import static org.assertj.core.api.Assertions.assertThat;

import backup.config.Batch;
import backup.helpers.ProcessingResult;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Set;

// https://assertj.github.io/doc/

public class TestCmdLineProcessing {

    private final static String CLASS = TestCmdLineProcessing.class.getName();

    // ---
    // A class used to carry a PrintWriter which is writing to a ByteArrayOutputStream.
    // The PrinterWriter will be used as a substitute for STDERR during command line processing.
    // ---

    public static class Capturer {

        final private ByteArrayOutputStream out = new ByteArrayOutputStream();
        final private PrintWriter pw = new PrintWriter(out);

        public @NotNull PrintWriter getPrintWriter() {
            return pw;
        }

        public String closeAndGet() {
            pw.close();
            return out.toString(StandardCharsets.UTF_8);
        }

    }

    @Test
    void noArguments() {
        String[] args = {};
        Capturer c = new Capturer();
        ProcessingResult res = Mainer.doCmdLineAndBizProcessing(args, Mainer.SkipBizLogic.True, c.getPrintWriter());
        // This prints help message to STDERR because a required arg is missing,
        // which is found out internally by PicoCli.
        assertThat(res.exitValue).isEqualTo(ProcessingResult.ExitValue.EXCEPTION_THROWN_BY_PICOCLI);
        assertThat(c.closeAndGet()).contains("[--dryrun]"); // help contains at least this
    }

    @Test
    void nonExistentTargetDir() {
        String[] args = {"--with", "fast_changers", "/vav/vav/vav"};
        Capturer c = new Capturer();
        ProcessingResult res = Mainer.doCmdLineAndBizProcessing(args, Mainer.SkipBizLogic.True, c.getPrintWriter());
        assertThat(res.exitValue).isEqualTo(ProcessingResult.ExitValue.ARG_POSTPROCESSING_PROBLEM);
    }

    @Test
    void existingTargetDir() {
        String[] args = {"--with", "fast_changers", "/tmp"};
        ProcessingResult res = Mainer.doCmdLineAndBizProcessing(args, Mainer.SkipBizLogic.True);
        assertThat(res.exitValue).isEqualTo(ProcessingResult.ExitValue.SKIPPED_BUSINESS_LOGIC);
    }

    @Test
    void unknownArguments() {
        String[] args = {"--with", "fast_changers", "--gobbledigook", "/tmp"};
        Capturer c = new Capturer();
        ProcessingResult res = Mainer.doCmdLineAndBizProcessing(args, Mainer.SkipBizLogic.True, c.getPrintWriter());
        assertThat(res.exitValue).isEqualTo(ProcessingResult.ExitValue.EXCEPTION_THROWN_BY_PICOCLI);
    }

    @Test
    void dryrunIsOn() {
        String[] args = {"--with", "fast_changers", DRYRUN, "/tmp"};
        ProcessingResult res = Mainer.doCmdLineAndBizProcessing(args, Mainer.SkipBizLogic.True);
        assertThat(res.cmdLineDef.isDryrun()).isTrue();
        assertThat(res.exitValue).isEqualTo(ProcessingResult.ExitValue.SKIPPED_BUSINESS_LOGIC);
    }

    @Test
    void dryrunIsOff() {
        String[] args = {"--with", "fast_changers", "/tmp"};
        ProcessingResult res = Mainer.doCmdLineAndBizProcessing(args, Mainer.SkipBizLogic.True);
        assertThat(res.cmdLineDef.isDryrun()).isFalse();
        assertThat(res.exitValue).isEqualTo(ProcessingResult.ExitValue.SKIPPED_BUSINESS_LOGIC);
    }

    @Test
    void noBatchGiven() {
        String[] args = {"/tmp"};
        Capturer c = new Capturer();
        ProcessingResult res = Mainer.doCmdLineAndBizProcessing(args, Mainer.SkipBizLogic.True, c.getPrintWriter());
        assertThat(res.exitValue).isEqualTo(ProcessingResult.ExitValue.ARG_POSTPROCESSING_PROBLEM);
        // This does not print help message because the argument processing went well
        // but the code exited on its own after checking the argument provided.
    }

    @Test
    void multipleBatchesGiven() {
        String[] args = {"--with", "rest_of_attic", "--with", "fast_changers", "/tmp"};
        ProcessingResult res = Mainer.doCmdLineAndBizProcessing(args, Mainer.SkipBizLogic.True);
        Set<Batch> bs = res.cmdLineDef.getBatchSet();
        assertThat(bs).isEqualTo(Set.of(new Batch[]{Batch.REST_OF_ATTIC, Batch.FAST_CHANGERS}));
        assertThat(res.exitValue).isEqualTo(ProcessingResult.ExitValue.SKIPPED_BUSINESS_LOGIC);
    }

    @Test
    void requestLongHelpWithCorrectArgs() {
        String[] args = {"--help", "--with", "fast_changers", "/tmp"};
        Capturer c = new Capturer();
        ProcessingResult res = Mainer.doCmdLineAndBizProcessing(args, Mainer.SkipBizLogic.True, c.getPrintWriter());
        // This **does** print help message to STDERR because it was explicitly requested.
        assertThat(c.closeAndGet()).contains("[--dryrun]"); // help contains at least this
        assertThat(res.exitValue).isEqualTo(ProcessingResult.ExitValue.HELP_REQUESTED);
    }

    @Test
    void requestShortHelpWithCorrectArgs() {
        String[] args = {"-h", "--with", "fast_changers", "/tmp"};
        Capturer c = new Capturer();
        ProcessingResult res = Mainer.doCmdLineAndBizProcessing(args, Mainer.SkipBizLogic.True, c.getPrintWriter());
        // This **does** print help message to STDERR because it was explicitly requested.
        assertThat(c.closeAndGet()).contains("[--dryrun]"); // help contains at least this
        assertThat(res.exitValue).isEqualTo(ProcessingResult.ExitValue.HELP_REQUESTED);
    }

    @Test
    void requestLongVersionWithCorrectArgs() {
        String[] args = {"--version", "--with", "fast_changers", "/tmp"};
        Capturer c = new Capturer();
        ProcessingResult res = Mainer.doCmdLineAndBizProcessing(args, Mainer.SkipBizLogic.True, c.getPrintWriter());
        // This **does** print the version message to STDERR because it was explicitly requested.
        assertThat(c.closeAndGet()).contains("backup_to_usb"); // help contains at least this
        assertThat(res.exitValue).isEqualTo(ProcessingResult.ExitValue.VERSION_REQUESTED);
    }

    @Test
    void requestShortVersionWithCorrectArgs() {
        String[] args = {"-V", "--with", "fast_changers", "/tmp"};
        Capturer c = new Capturer();
        ProcessingResult res = Mainer.doCmdLineAndBizProcessing(args, Mainer.SkipBizLogic.True, c.getPrintWriter());
        // This **does** print the version message to STDERR because it was explicitly requested.
        assertThat(c.closeAndGet()).contains("backup_to_usb"); // help contains at least this
        assertThat(res.exitValue).isEqualTo(ProcessingResult.ExitValue.VERSION_REQUESTED);
    }

    @Test
    void requestLongHelpWithIncorrectArgs() {
        String[] args = {"--help", "--with", "fast_changers"};
        Capturer c = new Capturer();
        ProcessingResult res = Mainer.doCmdLineAndBizProcessing(args, Mainer.SkipBizLogic.True, c.getPrintWriter());
        // This **does** print help message to STDERR because it was explicitly requested.
        assertThat(c.closeAndGet()).contains("[--dryrun]"); // help contains at least this
        assertThat(res.exitValue).isEqualTo(ProcessingResult.ExitValue.HELP_REQUESTED);
    }

    @Test
    void requestLongHelpWithNoOtherArgs() {
        String[] args = {"--help"};
        Capturer c = new Capturer();
        ProcessingResult res = Mainer.doCmdLineAndBizProcessing(args, Mainer.SkipBizLogic.True, c.getPrintWriter());
        // This **does** print help message to STDERR because it was explicitly requested.
        assertThat(c.closeAndGet()).contains("[--dryrun]"); // help contains at least this
        assertThat(res.exitValue).isEqualTo(ProcessingResult.ExitValue.HELP_REQUESTED);
    }

    @Test
    void verifyObtainedBatches() {
        String[] args = {"--with", "fast_changers", "--with", "rest_of_attic", "/tmp"};
        ProcessingResult res = Mainer.doCmdLineAndBizProcessing(args, Mainer.SkipBizLogic.True);
        Set<Batch> bs = res.cmdLineDef.getBatchSet();
        assertThat(bs).isEqualTo(Set.of(new Batch[]{Batch.FAST_CHANGERS, Batch.REST_OF_ATTIC}));
        assertThat(res.exitValue).isEqualTo(ProcessingResult.ExitValue.SKIPPED_BUSINESS_LOGIC);
    }

}

