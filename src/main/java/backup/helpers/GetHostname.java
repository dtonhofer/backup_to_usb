package backup.helpers;

public abstract class GetHostname {

    public static String getHostname() {
        String res = null;
        try {
            Process p = Runtime.getRuntime().exec("uname --nodename");
            byte[] bytes = p.getInputStream().readAllBytes();
            res = new String(bytes,"ASCII").trim();
        }
        catch (Exception ex) {
            // nothing happens
        }
        return res;
    }
}
