package backup.helpers;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

// Published in gist:
// https://gist.github.com/dtonhofer/e2a0603a2eff556b37f4c07b20d193d3

public abstract class DurationPrinter {

    private final static Map<ChronoUnit, StringSet> stringsOfChronoUnit;

    static {
        Map<ChronoUnit, StringSet> tmp = new HashMap<>();
        tmp.put(ChronoUnit.DAYS, new StringSet("day", "days"));
        tmp.put(ChronoUnit.HOURS, new StringSet("hour", "hours"));
        tmp.put(ChronoUnit.MINUTES, new StringSet("min", "mins"));
        tmp.put(ChronoUnit.SECONDS, new StringSet("s", "s"));
        tmp.put(ChronoUnit.MILLIS, new StringSet("ms", "ms"));
        stringsOfChronoUnit = Collections.unmodifiableMap(tmp);
    }

    private static class StringSet {

        private final String usual;
        private final String mono;

        public StringSet(@NotNull String mono, @NotNull String usual) {
            this.mono = mono;
            this.usual = usual;
        }

        public String of(int count) {
            if (count == 1) {
                return mono;
            } else {
                return usual;
            }
        }
    }

    private static void stringify(@NotNull List<String> res, int count, @NotNull ChronoUnit unit) {
        if (count == 0 && res.isEmpty() && !ChronoUnit.MILLIS.equals(unit)) {
            // suppress adding anything unless something already exists and this is not "seconds" (because
            // we print at least seconds)
        } else {
            StringSet ss = stringsOfChronoUnit.get(unit);
            if (!res.isEmpty()) {
                res.add(", "); // separator
            }
            res.add(Integer.toString(count));
            res.add(" ");
            if (ss == null) {
                // missing chrono unit!?!
                res.add(unit.toString());
            } else {
                res.add(ss.of(count));
            }
        }
    }

    // ---
    // Simple helper to print duration
    // https://stackoverflow.com/questions/3471397/how-can-i-pretty-print-a-duration-in-java
    // ---

    public static String formatDuration(Duration duration) {
        List<String> parts = new ArrayList<>(10);
        stringify(parts, (int) (duration.toDaysPart()), ChronoUnit.DAYS);
        stringify(parts, duration.toHoursPart(), ChronoUnit.HOURS);
        stringify(parts, duration.toMinutesPart(), ChronoUnit.MINUTES);
        stringify(parts, duration.toSecondsPart(), ChronoUnit.SECONDS);
        stringify(parts, duration.toMillisPart(), ChronoUnit.MILLIS);
        return String.join("", parts);
    }

}
