package backup.helpers;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum RsyncRetVal {

    SUCCESS(0,"Success",RsyncVerdict.SUCCESS),
    ERR_1(1,"Syntax or usage error",RsyncVerdict.FAILURE),
    ERR_2(2,"Protocol incompatibility",RsyncVerdict.FAILURE),
    ERR_3(3,"Errors selecting input/output files, dirs",RsyncVerdict.FAILURE),
    ERR_4(4,"Requested action not supported",RsyncVerdict.FAILURE),
    ERR_5(5,"Error starting client-server protocol",RsyncVerdict.FAILURE),
    ERR_6(6,"Daemon unable to append to log-file",RsyncVerdict.FAILURE),
    ERR_10(10,"Error in socket I/O",RsyncVerdict.FAILURE),
    ERR_11(11,"Error in file I/O",RsyncVerdict.FAILURE),
    ERR_12(12,"Error in rsync protocol data stream",RsyncVerdict.FAILURE),
    ERR_13(13,"Errors with program diagnostics",RsyncVerdict.FAILURE),
    ERR_14(14,"Error in IPC code",RsyncVerdict.FAILURE),
    ERR_20(20,"Received SIGUSR1 or SIGINT",RsyncVerdict.FAILURE),
    ERR_21(21,"Some error returned by waitpid()",RsyncVerdict.FAILURE),
    ERR_22(22,"Error allocating core memory buffers",RsyncVerdict.FAILURE),
    ERR_23(23,"Partial transfer due to error",RsyncVerdict.PARTIAL_SUCCESS),
    ERR_24(24,"Partial transfer due to vanished source files",RsyncVerdict.PARTIAL_SUCCESS),
    ERR_25(25,"The --max-delete limit stopped deletions",RsyncVerdict.FAILURE),
    ERR_30(30,"Timeout in data send/receive",RsyncVerdict.FAILURE),
    ERR_35(35,"Timeout waiting for daemon connection",RsyncVerdict.FAILURE);

    private final int id;
    private final String desc;
    private final RsyncVerdict verdict;

    private static Map<Integer,RsyncRetVal> map;

    static {
        Map<Integer,RsyncRetVal> mapTmp = new HashMap<>();
        for (RsyncRetVal rrv : RsyncRetVal.values()) {
            mapTmp.put(rrv.id,rrv);
        }
        map = Collections.unmodifiableMap(mapTmp);
    }

    RsyncRetVal(int id, @NotNull String desc,RsyncVerdict verdict) {
        this.id = id;
        this.desc = desc;
        this.verdict = verdict;
    }

    // Returns null on no such data

    public static RsyncRetVal byId(int id) {
        return map.get(id);
    }

    public int getId() {
        return id;
    }

    public String getDesc() {
        return desc;
    }

    public RsyncVerdict getRsyncVerdict() {
        return this.verdict;
    }
}
