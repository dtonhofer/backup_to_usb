package backup.helpers;

import org.jetbrains.annotations.NotNull;

// ---
// A class that indicates whether a path should be included or excluded.
// The path is a string as it may contain wildcards. Consult the manual for
// "rsync" under "FILTER RULES" and "INCLUDE/EXCLUDE PATTERN RULES"
// ---

public class FilterRule {

    private final boolean include;

    @NotNull
    private final String path;

    public FilterRule(@NotNull String path, boolean include) {
        this.path = path;
        this.include = include;
    }

    public boolean isInclude() {
        return include;
    }

    public String getPath() {
        return path;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(include ? "+" : "-");
        buf.append(path);
        return buf.toString();
    }
}
