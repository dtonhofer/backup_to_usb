package backup;

import backup.helpers.ProcessingResult;
import org.junit.jupiter.api.Test;

public class TestDryrun {

    @Test
    void dryrunRsyncCommand() {
        // not really a test, more like a trial run
        String[] args = {"--with", "attic", "--with", "mostofhome", "--with", "largebutstable", "--dryrun", "/tmp"};
        ProcessingResult res = Mainer.doCmdLineAndBizProcessing(args, Mainer.SkipBizLogic.False);
    }


}
