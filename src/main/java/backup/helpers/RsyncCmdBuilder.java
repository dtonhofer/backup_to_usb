package backup.helpers;

import backup.CmdLineDef;
import backup.config.Batch;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.SortedMap;
import java.util.TreeMap;

public abstract class RsyncCmdBuilder {

    private static RsyncCmd buildSingleRsyncCommand(@NotNull String rsyncExeName, @NotNull FilterRuleSeq frSeq, @NotNull Batch batch, @NotNull CmdLineDef cdf, @NotNull File targetDirName) {
        RsyncCmd.Verbosity verbosity = (cdf.isVerbose() ? RsyncCmd.Verbosity.Normal : RsyncCmd.Verbosity.Off);
        RsyncCmd.DryRun dryRun = (cdf.isDryrun() ? RsyncCmd.DryRun.On : RsyncCmd.DryRun.Off);
        RsyncCmd res = new RsyncCmd(rsyncExeName, batch, dryRun, verbosity);
        // all the filter rules, if any
        for (FilterRule fr : frSeq.seq) {
            if (fr.isInclude()) {
                // if the "include" has a slash at the end, then the origin must be a directory
                res.addParam("--include");
                res.addParam(fr.getPath());
            } else {
                // if the "exclude" has a slash at the end, then the origin must be a directory
                res.addParam("--exclude");
                res.addParam(fr.getPath());
            }
        }
        // all the sources (there is at least one)
        assert !batch.sources.isEmpty();
        for (File src : batch.sources) {
            res.addParam(src.toString());
        }
        // the destination
        File qualifiedDumpDirName = new File(targetDirName, DirNaming.buildAtomicDumpDirName(batch));
        // Adding a slash at the end of the string representation ensures that the directory will be freshly created
        res.addParam(qualifiedDumpDirName + "/");
        return res;
    }

    public static SortedMap<Batch, RsyncCmd> buildAllRsyncCmds(@NotNull String rsyncExeName, @NotNull CmdLineDef cdf, @NotNull File targetDirName) {
        SortedMap<Batch, RsyncCmd> res = new TreeMap<>();
        cdf.getBatchSet().forEach(batch -> {
            FilterRuleSeq frSeq = batch.seq;
            assert frSeq != null : "Batch " + batch + " has no entry in settings";
            RsyncCmd cmd = buildSingleRsyncCommand(rsyncExeName, frSeq, batch, cdf, targetDirName);
            res.put(batch, cmd);
        });
        return res;
    }

}
