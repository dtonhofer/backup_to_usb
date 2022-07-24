package backup.helpers;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

// ---
// A sequence of include/exclude filter rules read from strings.
//
// The rsync manual states:
//
// > As  the list of files/directories to transfer is built, rsync checks each name to be transferred against
// > the list of include/exclude patterns in turn, and the first matching pattern is acted on: if it is an exclude
// > pattern, then that file is skipped; if it is an include pattern then that filename is not skipped; if no
// > matching pattern is found, then the filename is not skipped.
// ---

public class FilterRuleSeq {

    private final static String CLASS = FilterRuleSeq.class.getName();
    private final static Logger logger = LoggerFactory.getLogger(CLASS);

    // ---
    // The "seq" is set to an unmodifiable list in the constructor
    // ---

    @NotNull
    public List<FilterRule> seq;

    // ---
    // Passed "paths" must be preceded by a "+" for include and by a "-" for exclude
    // rules. The order of the "paths" is important for rsync behaviour and is
    // upheld when the "paths" are used as rsync parameters in their FilterRule form.
    // ---

    public FilterRuleSeq(String... paths) {
        List<FilterRule> tmpSeq = new LinkedList<>();
        for (String path : paths) {
            if (path.startsWith("+")) {
                String cleanPath = path.substring(1);
                tmpSeq.add(new FilterRule(cleanPath,true));
            }
            else if (path.startsWith("-")) {
                String cleanPath = path.substring(1);
                tmpSeq.add(new FilterRule(cleanPath,false));
            }
            else {
                logger.error("Dropping the argument '" + path + "' because it doesn't start with '+' or '-'");
            }
        }
        seq = Collections.unmodifiableList(tmpSeq);
    }

}
