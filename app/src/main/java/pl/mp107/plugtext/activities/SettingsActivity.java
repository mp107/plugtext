package pl.mp107.plugtext.activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import pl.mp107.plugtext.R;
import pl.mp107.plugtext.exceptions.ApplicationPluginException;
import pl.mp107.plugtext.plugins.BaseApplicationPlugin;
import pl.mp107.plugtext.utils.TextFileApplicationPluginUtil;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || EditorPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class EditorPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_editor);
            setHasOptionsMenu(true);

            final SharedPreferences sharedPreferences = getActivity().getSharedPreferences("settings", Context.MODE_MULTI_PROCESS);

            /* Handling clicks on preferences */
            /* Colors */
            setColorPickerBehaviour(sharedPreferences, "editor_background_color");
            setColorPickerBehaviour(sharedPreferences, "editor_builtins_color");
            setColorPickerBehaviour(sharedPreferences, "editor_comments_color");
            setColorPickerBehaviour(sharedPreferences, "editor_keywords_color");
            setColorPickerBehaviour(sharedPreferences, "editor_normal_text_color");
            setColorPickerBehaviour(sharedPreferences, "editor_numbers_color");
            setColorPickerBehaviour(sharedPreferences, "editor_preprocessors_color");
            /* DB Update */
            Preference pref = (Preference) findPreference("editor_plugin_db_update");
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    try {
                        String[] additionalDirectories = {
                                getActivity().getExternalFilesDir(null).getCanonicalPath() + "/plugins"
                        };
                        Log.d("Updater", "Directories: " + Arrays.toString(additionalDirectories));
                        File[] pluginFiles;
                        for (String directoryPath : additionalDirectories) {
                            pluginFiles = getPlugins(directoryPath);
                            for (File pluginFile : pluginFiles) {
                                Log.d("Updater", "Plugin file: " + pluginFile.getCanonicalPath());
                                BufferedReader br = new BufferedReader(new FileReader(pluginFile));
                                StringBuilder sb = new StringBuilder();
                                String line = br.readLine();
                                while (line != null) {
                                    sb.append(line);
                                    sb.append(System.lineSeparator());
                                    line = br.readLine();
                                }
                                String fileContent = sb.toString();
                                try {
                                    BaseApplicationPlugin plugin = TextFileApplicationPluginUtil
                                            .createTextFileApplicationPluginFromString(fileContent);
                                    // TODO - add plugin to plugin DB
                                    Log.i("PluginLoader", "Plugin " + plugin.getName() + " has been loaded successfully");
                                } catch (ApplicationPluginException e) {
                                    Log.i("PluginLoader", "Plugin loading failed (ApplicationPluginException)");
                                }
                            }
                        }
                    } catch (IOException e) {
                        Log.i("PluginLoader", "Plugin loading failed (IOExcpetion)");
                    }
                    Toast.makeText(getActivity(), R.string.plugins_loaded_successfully, Toast.LENGTH_LONG).show();
                    return true;
                }

                private File[] getPlugins(String path) {
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
            });

        }

        private void setColorPickerBehaviour(final SharedPreferences sharedPreferences, final String key) {
            Preference pref = (Preference) findPreference(key);
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    final int color = sharedPreferences.getInt(key, Integer.MIN_VALUE);
                    int defaultColorR = 0, defaultColorG = 0, defaultColorB = 0;
                    if (color != Integer.MIN_VALUE) {
                        defaultColorR = Color.red(color);
                        defaultColorG = Color.green(color);
                        defaultColorB = Color.blue(color);
                    }
                    final ColorPicker cp = new ColorPicker(getActivity(), defaultColorR, defaultColorG, defaultColorB);
                    cp.show();
                    cp.setCallback(new ColorPickerCallback() {
                        @Override
                        public void onColorChosen(@ColorInt int color) {
                            sharedPreferences.edit().putInt(key, color).apply();
                            // Log.d("DEBUG", "Saved color value="+String.format("#%06X", 0xFFFFFF & color)+" for " + key);
                            cp.cancel();
                        }
                    });
                    return true;
                }
            });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
