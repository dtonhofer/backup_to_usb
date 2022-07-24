package backup;

import backup.helpers.GetHostname;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestGetHostname {

    @Test
    public void obtainHostname() {
        String hostname = GetHostname.getHostname();
        System.out.println("'" + hostname + "'");
        assertThat(hostname).isNotNull();
    }
}
