package pt.ua.adis.argus.core;

import pt.ua.adis.argus.Context;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

/**
 * Class that stores common variables used along the application.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>)
 * @version 1.0
 */
public class Constants {

    /**
     * Returns a pattern used to check valid emails.
     */
    public static final Pattern PATTERN_EMAIL =
            Pattern.compile("^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=" +
                    "?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9]" +
                    "(?:[a-z0-9-]*[a-z0-9])?$");


    public static final String PROJECT_DIRECTORY =
            System.getProperty("user.dir") + File.separator + "src" +
                    File.separator + "main";
    public static final Font FONT;

    static {
        Font loadedFont = null;
        try {

            InputStream fontStream = Context.class.getResourceAsStream("HelveticaNeue.otf");

            // create the font to use
            loadedFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(12f);

            // register the font
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(loadedFont);

        } catch (IOException | FontFormatException ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        FONT = loadedFont;
    }

    private static final String INSTALL_DIR = new File(
            Constants.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath())
            .getAbsoluteFile()
            .getParentFile()
            .getAbsolutePath()
            .replaceAll("%20", " ");

}
