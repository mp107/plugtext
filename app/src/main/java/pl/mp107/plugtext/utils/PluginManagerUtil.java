package pl.mp107.plugtext.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PluginManagerUtil {

    private static final String TAG = "PluginManagerUtil";

    public static void initializePluginsDirectory(Context context) {
        /* Create plugins directory */
        try {
            String destinationDirectoryPath = context.getExternalFilesDir(null).getCanonicalPath() + "/plugins";
            File sourceDirectory = new File(destinationDirectoryPath);
            File destinationDirectory = new File(destinationDirectoryPath);

            Log.i(TAG, "PluginLoader - Trying to create plugin directory in " + destinationDirectoryPath);
            /* Create directory if not exists or is not a directory */
            if (!destinationDirectory.exists() || !destinationDirectory.isDirectory()) {
                destinationDirectory.mkdirs();
            }
            AssetManager assetManager = context.getAssets();
            String[] sourcePluginsPaths = context.getAssets().list("plugins");
            String destinationPluginFilePath = "";
            /* For any of the source plugin files... */
            for (String sourcePluginPath : sourcePluginsPaths) {
                sourcePluginPath = "plugins/" + sourcePluginPath;
                try {
                    /* Read source file */
                    File file = new File(sourcePluginPath);
                    Log.i(TAG, "PluginLoader - Loading raw Asset: " + sourcePluginPath);
                    InputStream is = context.getAssets().open(sourcePluginPath);
                    byte[] b = new byte[is.available()];
                    if (is.read(b) == -1)
                        throw new IOException();
                    is.close();
                    /* Write content to destination file */
                    destinationPluginFilePath = destinationDirectory + "/" + file.getName();
                    Log.i(TAG, "PluginLoader - Writing raw Asset: " + destinationPluginFilePath);
                    FileOutputStream out = new FileOutputStream(destinationPluginFilePath);
                    out.write(b);
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    Log.w(TAG, "PluginLoader - Plugin loading failed (IOExcpetion)");
                }
            }
        } catch (IOException e) {
            Log.i(TAG, "PluginLoader - Directory creeating failed (IOExcpetion)");
        } catch (NullPointerException e) {
            Log.i(TAG, "PluginLoader - Directory creeating failed (NullPointerException)");
        }
    }
}
