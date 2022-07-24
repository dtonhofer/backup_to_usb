package backup;

import backup.helpers.DurationPrinter;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

// Published in gist:
// https://gist.github.com/dtonhofer/e2a0603a2eff556b37f4c07b20d193d3

public class TestDurationPrinter {

    @Test
    void testDurationPrinter() {
        SortedMap<Duration,String> durs = new TreeMap<>();
        durs.put(Duration.of(0, ChronoUnit.MILLIS),"0 ms");
        durs.put(Duration.of(1000, ChronoUnit.MILLIS),"1 s, 0 ms");
        durs.put(Duration.of(1500, ChronoUnit.MILLIS),"1 s, 500 ms");
        durs.put(Duration.of(60000, ChronoUnit.MILLIS),"1 min, 0 s, 0 ms");
        durs.put(Duration.of(120000, ChronoUnit.MILLIS),"2 mins, 0 s, 0 ms");
        durs.put(Duration.of(122222, ChronoUnit.MILLIS),"2 mins, 2 s, 222 ms");
        durs.put(Duration.of(5677821, ChronoUnit.MILLIS),"1 hour, 34 mins, 37 s, 821 ms");
        durs.put(Duration.of(861862066L, ChronoUnit.MILLIS),"9 days, 23 hours, 24 mins, 22 s, 66 ms");
        durs.put(Duration.of(9877461771L, ChronoUnit.MILLIS),"114 days, 7 hours, 44 mins, 21 s, 771 ms");
        durs.keySet().forEach( dur -> assertThat(DurationPrinter.formatDuration(dur)).isEqualTo(durs.get(dur)));
        /*
        for (long l = 0; l < 9877461771L; l += 533) {
            System.out.println(l + " == " + DurationPrinter.formatDuration(Duration.of(l, ChronoUnit.MILLIS)));
        }
         */
    }
}
