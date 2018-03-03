package pl.mp107.plugtext.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.Toast;

import com.rustamg.filedialogs.FileDialog;
import com.rustamg.filedialogs.OpenFileDialog;
import com.rustamg.filedialogs.SaveFileDialog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import pl.mp107.plugtext.R;
import pl.mp107.plugtext.components.CodeEditor;
import pl.mp107.plugtext.constants.DefaultSyntaxHighlightColors;
import pl.mp107.plugtext.constants.SharedPreferencesKeys;
import pl.mp107.plugtext.db.DatabaseHandler;
import pl.mp107.plugtext.db.SyntaxSchema;
import pl.mp107.plugtext.utils.PluginManagerUtil;

public class MainActivity extends AppCompatActivity
        implements FileDialog.OnFileSelectedListener {


    private CodeEditor codeEditor;
    private boolean mStoragePermissionsGranted;
    private SharedPreferences sharedPreferences;
    private static final String TAG = "MainActivity";

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("settings", Context.MODE_MULTI_PROCESS);

        if (!sharedPreferences.getBoolean(SharedPreferencesKeys.FIRST_RUN_KEY_VERSION_1, false)) {
            storageInit();
            initializePluginsDirectory();
            sharedPreferences.edit().putBoolean(SharedPreferencesKeys.FIRST_RUN_KEY_VERSION_1, true).apply();
        }

        codeEditor = (CodeEditor) findViewById(R.id.editor);
        codeEditor.setText("");
        mStoragePermissionsGranted = checkStoragePermissions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        final MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        final SearchView searchView =
                (SearchView) searchMenuItem.getActionView();

        searchView.setQueryHint(getResources().getString(R.string.action_search));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                //Toast.makeText(getBaseContext(), query, Toast.LENGTH_LONG).show();
                codeEditor.setSearchedString(query);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Toast.makeText(getBaseContext(), newText, Toast.LENGTH_LONG).show();
                return false;
            }
        });

        searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                searchView.setFocusable(true);
                searchView.requestFocus();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                codeEditor.setSearchedString(null);
                return true;
            }
        });
        //searchView.setOn
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Intent intent;

        switch (id) {
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_open_file:
                onOpenDialogClick();
                return true;
            case R.id.action_save_file:
                onSaveDialogClick();
                return true;
            case R.id.action_back:
                Toast.makeText(MainActivity.this, R.string.action_back, Toast.LENGTH_SHORT).show();
                // TODO
                return true;
            case R.id.action_forward:
                Toast.makeText(MainActivity.this, R.string.action_forward, Toast.LENGTH_SHORT).show();
                // TODO
                return true;
            case R.id.action_search:
                return true;
            case R.id.action_language:
                Toast.makeText(MainActivity.this, R.string.language, Toast.LENGTH_SHORT).show();
                showLanguagesSelectingIntent();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showLanguagesSelectingIntent() {
        final DatabaseHandler dbHandler = new DatabaseHandler(this);

        List<SyntaxSchema> schemas = dbHandler.getAllSyntaxSchemas();

        /* Language names */
        final String[] languagesListLabels = new String[schemas.size() + 1];

        /* Language IDs */
        String[] languagesListValues = new String[schemas.size() + 1];
        languagesListLabels[0] = getResources().getString(R.string.language_none);
        for (int i = 0; i < schemas.size(); i++) {
            languagesListLabels[i + 1] = schemas.get(i).getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.language));
        builder.setItems(languagesListLabels, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SyntaxSchema schema = null;
                if (which != 0)
                    schema = dbHandler.getSyntaxSchemaByName(languagesListLabels[which]);
                setSyntaxSchemaForEditor(codeEditor, schema);
            }
        });
        builder.show();
    }

    private void setSyntaxSchemaForEditor(CodeEditor codeEditor, @Nullable SyntaxSchema schema) {
        if (schema == null) {
            // Disable syntax highlighting
            codeEditor.setPatternBuiltins(null);
            codeEditor.setPatternComments(null);
            codeEditor.setPatternKeywords(null);
            codeEditor.setPatternNumbers(null);
            codeEditor.setPatternPreprocessors(null);
            codeEditor.refreshSyntaxHighlight();
        } else {
            String patternBuiltinsString = schema.getPatternBuiltins();
            String patternCommentsString = schema.getPatternComments();
            String patternKeywordsString = schema.getPatternKeywords();
            String patternNumbersString = schema.getPatternNumbers();
            String patternPreprocessorsString = schema.getPatternPreprocessors();

            Pattern patternBuiltins = null;
            Pattern patternComments = null;
            Pattern patternKeywords = null;
            Pattern patternNumbers = null;
            Pattern patternPreprocessors = null;
            if (patternBuiltinsString != null && !patternBuiltinsString.equals(""))
                patternBuiltins = Pattern.compile(patternBuiltinsString);
            if (patternCommentsString != null && !patternCommentsString.equals(""))
                patternComments = Pattern.compile(patternCommentsString);
            if (patternKeywordsString != null && !patternKeywordsString.equals(""))
                patternKeywords = Pattern.compile(patternKeywordsString);
            if (patternNumbersString != null && !patternNumbersString.equals(""))
                patternNumbers = Pattern.compile(patternNumbersString);
            if (patternPreprocessorsString != null && !patternPreprocessorsString.equals(""))
                patternPreprocessors = Pattern.compile(patternPreprocessorsString);

            // Enable syntax highlighting and set patterns
            codeEditor.setPatternBuiltins(patternBuiltins);
            codeEditor.setPatternComments(patternComments);
            codeEditor.setPatternKeywords(patternKeywords);
            codeEditor.setPatternNumbers(patternNumbers);
            codeEditor.setPatternPreprocessors(patternPreprocessors);
            codeEditor.refreshSyntaxHighlight();
/*
            Log.d(TAG, "SyntaxSET Builtins: " + schema.getPatternBuiltins());
            Log.d(TAG, "SyntaxSET Comments: " + schema.getPatternComments());
            Log.d(TAG, "SyntaxSET Keywords: " + schema.getPatternKeywords());
            Log.d(TAG, "SyntaxSET Numbers: " + schema.getPatternNumbers());
            Log.d(TAG, "SyntaxSET Preprocessors: " + schema.getPatternPreprocessors());
*/
        }
    }

    private boolean checkStoragePermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                }, 1);
                return false;
            }
        } else { //permission is automatically granted on API<23 upon installation
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mStoragePermissionsGranted = (grantResults.length == 2 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED);
    }

    private void showStoragePermissionsError() {
        // TODO
        Toast.makeText(this, "Storage permission is not granted", Toast.LENGTH_LONG).show();
    }

    private void showFileDialog(FileDialog dialog, String tag) {
        // TODO
        dialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme);
        dialog.show(getSupportFragmentManager(), tag);
    }

    protected void onOpenDialogClick() {
        if (mStoragePermissionsGranted) {
            OpenFileDialog openFileDialog = new OpenFileDialog();
            showFileDialog(openFileDialog, OpenFileDialog.class.getName());
        } else {
            showStoragePermissionsError();
        }
    }

    @Override
    public void onFileSelected(FileDialog dialog, File file) {
        if (dialog instanceof OpenFileDialog) {
            onOpenFileSelected(dialog, file);
        } else if (dialog instanceof SaveFileDialog) {
            onSaveFileSelected(dialog, file);
        }

    }

    private void onOpenFileSelected(FileDialog dialog, File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] b = new byte[fis.available()];
            if (fis.read(b) == -1)
                throw new IOException();
            fis.close();
            codeEditor.setText(new String(b));
        } catch (Exception e) {
            codeEditor.setText("");
            Toast.makeText(this, R.string.file_opening_failed, Toast.LENGTH_LONG).show();
        }
    }

    private void onSaveFileSelected(FileDialog dialog, File file) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(codeEditor.getCleanText());
            bw.flush();
            bw.close();
        } catch (Exception e) {
            Toast.makeText(this, R.string.file_saving_failed, Toast.LENGTH_LONG).show();
        }
    }

    private void onSaveDialogClick() {
        if (mStoragePermissionsGranted) {
            SaveFileDialog saveFileDialog = new SaveFileDialog();
            showFileDialog(saveFileDialog, SaveFileDialog.class.getName());
        } else {
            showStoragePermissionsError();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        int oldBackgroundColor = codeEditor.getColorBackground();
        int oldBuiltinsColor = codeEditor.getColorBuiltin();
        int oldCommentsColor = codeEditor.getColorComment();
        int oldKeywordsColor = codeEditor.getColorKeyword();
        int oldNormalTextColor = codeEditor.getColorNormalText();
        int oldNumbersColor = codeEditor.getColorNumber();
        int oldPreprocessorsColor = codeEditor.getColorPreprocessors();

        int newBackgroundColor = sharedPreferences.getInt(
                SharedPreferencesKeys.EDITOR_BACKGROUND_COLOR, Integer.MIN_VALUE);
        int newBuiltinsColor = sharedPreferences.getInt(
                SharedPreferencesKeys.EDITOR_BUILTINS_COLOR, Integer.MIN_VALUE);
        int newCommentsColor = sharedPreferences.getInt(
                SharedPreferencesKeys.EDITOR_COMMENTS_COLOR, Integer.MIN_VALUE);
        int newKeywordsColor = sharedPreferences.getInt(
                SharedPreferencesKeys.EDITOR_KEYWORDS_COLOR, Integer.MIN_VALUE);
        int newNormalTextColor = sharedPreferences.getInt(
                SharedPreferencesKeys.EDITOR_NORMAL_TEXT_COLOR, Integer.MIN_VALUE);
        int newNumbersColor = sharedPreferences.getInt(
                SharedPreferencesKeys.EDITOR_NUMBERS_COLOR, Integer.MIN_VALUE);
        int newPreprocessorsColor = sharedPreferences.getInt(
                SharedPreferencesKeys.EDITOR_PREPROCESSORS_COLOR, Integer.MIN_VALUE);
/*
        Log.d(TAG, "oldBackgroundColor" + String.format("#%06X", 0xFFFFFF & oldBackgroundColor));
        Log.d(TAG, "newBackgroundColor" + String.format("#%06X", 0xFFFFFF & newBackgroundColor));
        Log.d(TAG, "oldBuiltinsColor" + String.format("#%06X", 0xFFFFFF & oldBuiltinsColor));
        Log.d(TAG, "newBuiltinsColor" + String.format("#%06X", 0xFFFFFF & newBuiltinsColor));
        Log.d(TAG, "oldCommentsColor" + String.format("#%06X", 0xFFFFFF & oldCommentsColor));
        Log.d(TAG, "newCommentsColor" + String.format("#%06X", 0xFFFFFF & newCommentsColor));
        Log.d(TAG, "oldKeywordsColor" + String.format("#%06X", 0xFFFFFF & oldKeywordsColor));
        Log.d(TAG, "newKeywordsColor" + String.format("#%06X", 0xFFFFFF & newKeywordsColor));
        Log.d(TAG, "oldNormalTextColor" + String.format("#%06X", 0xFFFFFF & oldNormalTextColor));
        Log.d(TAG, "newNormalTextColor" + String.format("#%06X", 0xFFFFFF & newNormalTextColor));
        Log.d(TAG, "oldNumbersColor" + String.format("#%06X", 0xFFFFFF & oldNumbersColor));
        Log.d(TAG, "newNumbersColor" + String.format("#%06X", 0xFFFFFF & newNumbersColor));
        Log.d(TAG, "oldPreprocessorsColor" + String.format("#%06X", 0xFFFFFF & oldPreprocessorsColor));
        Log.d(TAG, "newPreprocessorsColor" + String.format("#%06X", 0xFFFFFF & newPreprocessorsColor));
*/
        boolean colorsChanged = false;
        // Check if colors have change and replace if so
        if (newBackgroundColor != oldBackgroundColor) {
            codeEditor.setColorBackground(newBackgroundColor);
            colorsChanged = true;
        }
        if (newBuiltinsColor != oldBuiltinsColor) {
            codeEditor.setColorBuiltin(newBuiltinsColor);
            colorsChanged = true;
        }
        if (newCommentsColor != oldCommentsColor) {
            codeEditor.setColorComment(newCommentsColor);
            colorsChanged = true;
        }
        if (newKeywordsColor != oldKeywordsColor) {
            codeEditor.setColorKeyword(newKeywordsColor);
            colorsChanged = true;
        }
        if (newNormalTextColor != oldNormalTextColor) {
            codeEditor.setColorNormalText(newNormalTextColor);
            colorsChanged = true;
        }
        if (newNumbersColor != oldNumbersColor) {
            codeEditor.setColorNumber(newNumbersColor);
            colorsChanged = true;
        }
        if (newPreprocessorsColor != oldPreprocessorsColor) {
            codeEditor.setColorPreprocessors(newPreprocessorsColor);
            colorsChanged = true;
        }

        // Refresh syntax highlighting if any color have changed
        if (colorsChanged) {
            codeEditor.refreshSyntaxHighlight();
            //Log.d(TAG, "Syntax colors changed: true");
        }/* else
            Log.d(TAG, "Syntax colors changed: false");*/
    }

    private void storageInit() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SharedPreferencesKeys.EDITOR_BACKGROUND_COLOR, DefaultSyntaxHighlightColors.editor_background_color);
        editor.putInt(SharedPreferencesKeys.EDITOR_BUILTINS_COLOR, DefaultSyntaxHighlightColors.editor_builtins_color);
        editor.putInt(SharedPreferencesKeys.EDITOR_COMMENTS_COLOR, DefaultSyntaxHighlightColors.editor_comments_color);
        editor.putInt(SharedPreferencesKeys.EDITOR_KEYWORDS_COLOR, DefaultSyntaxHighlightColors.editor_keywords_color);
        editor.putInt(SharedPreferencesKeys.EDITOR_NORMAL_TEXT_COLOR, DefaultSyntaxHighlightColors.editor_normal_text_color);
        editor.putInt(SharedPreferencesKeys.EDITOR_NUMBERS_COLOR, DefaultSyntaxHighlightColors.editor_numbers_color);
        editor.putInt(SharedPreferencesKeys.EDITOR_PREPROCESSORS_COLOR, DefaultSyntaxHighlightColors.editor_preprocessors_color);
        editor.apply();
    }

    private void initializePluginsDirectory() {
        PluginManagerUtil.initializePluginsDirectory(this);
    }
}
