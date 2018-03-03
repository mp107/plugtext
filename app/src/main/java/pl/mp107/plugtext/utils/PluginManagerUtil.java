package pl.mp107.plugtext.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.regex.Pattern;

import pl.mp107.plugtext.R;
import pl.mp107.plugtext.db.DatabaseHandler;
import pl.mp107.plugtext.db.SyntaxSchema;
import pl.mp107.plugtext.exceptions.ApplicationPluginException;
import pl.mp107.plugtext.plugins.BaseApplicationPlugin;

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

    public static void pluginsReload(Context context) {
        try {
            String[] additionalDirectories = {
                    context.getApplicationContext().getExternalFilesDir(null).getCanonicalPath() + "/plugins"
            };
            Log.d(TAG, "PluginUpdater - Directories: " + Arrays.toString(additionalDirectories));
            File[] pluginFiles;
            DatabaseHandler dbHandler = new DatabaseHandler(context.getApplicationContext());
            dbHandler.clearDatabase();
            for (String directoryPath : additionalDirectories) {
                pluginFiles = getPlugins(directoryPath);
                for (File pluginFile : pluginFiles) {
                    Log.d(TAG, "PluginUpdater - Plugin file: " + pluginFile.getCanonicalPath());
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(
                                    new FileInputStream(pluginFile), "UTF8"));
                    StringBuilder sb = new StringBuilder();
                    String line = br.readLine();
                    while (line != null) {
                        sb.append(line);
                        sb.append(System.lineSeparator());
                        line = br.readLine();
                    }
                    String fileContent = sb.toString();
                    BaseApplicationPlugin plugin = null;
                    try {
                        plugin = TextFileApplicationPluginUtil
                                .createTextFileApplicationPluginFromString(fileContent);
                        // Adding syntax schema to db
                        Pattern patternBuiltins = plugin.getPatternBuiltins();
                        Pattern patternComments = plugin.getPatternComments();
                        Pattern patternFileExtension = plugin.getPatternFileExtensions();
                        Pattern patternKeywords = plugin.getPatternKeywords();
                        Pattern patternLines = plugin.getPatternLines();
                        Pattern patternNumbers = plugin.getPatternNumbers();
                        Pattern patternPreprocessors = plugin.getPatternPreprocessors();

                        String patternBuiltinsString = null;
                        String patternCommentsString = null;
                        String patternFileExtensionString = null;
                        String patternKeywordsString = null;
                        String patternLinesString = null;
                        String patternNumbersString = null;
                        String patternPreprocessorsString = null;
                        if (patternBuiltins != null)
                            patternBuiltinsString = patternBuiltins.pattern();
                        if (patternComments != null)
                            patternCommentsString = patternComments.pattern();
                        if (patternFileExtension != null)
                            patternFileExtensionString = patternFileExtension.pattern();
                        if (patternKeywords != null)
                            patternKeywordsString = patternKeywords.pattern();
                        if (patternLines != null)
                            patternLinesString = patternLines.pattern();
                        if (patternNumbers != null)
                            patternNumbersString = patternNumbers.pattern();
                        if (patternPreprocessors != null)
                            patternPreprocessorsString = patternPreprocessors.pattern();

                        dbHandler.addSyntaxSchema(
                                new SyntaxSchema(
                                        patternBuiltinsString,
                                        patternCommentsString,
                                        patternFileExtensionString,
                                        patternKeywordsString,
                                        patternLinesString,
                                        patternNumbersString,
                                        patternPreprocessorsString,
                                        plugin.getDescription(),
                                        plugin.getName(),
                                        plugin.getVersion()
                                )
                        );
                        Log.i(TAG, "PluginUpdater - Plugin " + plugin.getName() + " has been loaded successfully");
                    } catch (ApplicationPluginException | NullPointerException e) {
                        if (e instanceof ApplicationPluginException) {
                            Log.i(TAG, "PluginUpdater - Plugin loading failed (ApplicationPluginException)");
                        }
                        if (e instanceof NullPointerException) {
                            Log.i(TAG, "PluginUpdater - Plugin loading failed (NullPointerException)");
                        }
                        if (plugin != null && plugin.getName() != null && !plugin.getName().equals("")) {
                            Toast.makeText(context.getApplicationContext(), R.string.plugin_load_error + "\"" + plugin.getName() + "\"", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
            Toast.makeText(context.getApplicationContext(), R.string.plugins_loaded_successfully, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.i(TAG, "PluginUpdater - Plugins loading failed (IOException)");
            Toast.makeText(context.getApplicationContext(), R.string.plugins_load_error, Toast.LENGTH_LONG).show();
        }
    }

    private static File[] getPlugins(String path) {
        File directory = new File(path);
        File[] files = {};
        if (directory.exists() && directory.isDirectory() && directory.canRead()) {
            files = directory.listFiles(
                    new FilenameFilter() {
                        public boolean accept(File dir, String filename) {
                            return filename.toUpperCase().endsWith(".PLU");
                        }
                    }
            );
        }
        return files;
    }
}
