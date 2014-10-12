package pt.ua.adis.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.adis.project.core.User;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Eduardo Duarte (<a href="mailto:eduarte@ubiwhere.com">eduarte@ubiwhere.com</a>)
 * @version 1.0
 */
public class Context {

    private static final Logger logger = LoggerFactory.getLogger(Context.class);

    private static final Map<String, BufferedImage> ASSETS = new HashMap<>();

    private static User loggedInUser;


    public static BufferedImage getAsset(String assetFileName) {
        BufferedImage result = ASSETS.get(assetFileName + ".png");

        if (result == null) {
            try {
                InputStream stream = Context.class.getResourceAsStream("assets" +
                        File.separator + assetFileName + ".png");

                result = ImageIO.read(stream);
                ASSETS.put(assetFileName, result);

            } catch (IOException | IllegalArgumentException e) {
                // This exception will only occur if the folder contains
                // a non-image file for some unspecified reason.
                // The resources folder should only contain image files that
                // are used in the application's execution.

                JOptionPane.showMessageDialog(null, "Asset file " + assetFileName + " not detected or corrupted!\n"
                                + "Please reinstall the application.", "Nonexistent or corrupted asset file",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                System.exit(1);
            }
        }

        return result;
    }


    public static User getLoggedInUser() {
        return loggedInUser;
    }


    public static void setLoggedInUser(User newLoggedInUser) {
        loggedInUser = newLoggedInUser;
    }


    public static void installUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler(){
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                if (e instanceof ThreadDeath) {
                    logger.warn("Ignoring uncaught ThreadDead exception.");
                    return;
                }
                logger.error("Uncaught exception on cli thread, aborting.", e);
                System.exit(0);
            }
        });
    }
}
