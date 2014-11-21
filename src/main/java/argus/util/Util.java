package argus.util;

import java.io.File;
import java.text.DecimalFormat;

/**
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public class Util {

    public static final String PROJECT_DIRECTORY =
            System.getProperty("user.dir") + File.separator + "src" + File.separator + "main";

    public static final String INSTALL_DIR = new File(
            Util.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath())
            .getAbsoluteFile()
            .getParentFile()
            .getAbsolutePath()
            .replaceAll("%20", " ");

    public static long difference(long n1, long n2) {
        long result = n1 - n2;
        return result >= 0 ? result : -result;
    }

    public static int difference(int n1, int n2) {
        int result = n1 - n2;
        return result >= 0 ? result : -result;
    }
}
