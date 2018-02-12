package pl.mp107.plugtext.activities;

import android.Manifest;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.rustamg.filedialogs.FileDialog;
import com.rustamg.filedialogs.OpenFileDialog;
import com.rustamg.filedialogs.SaveFileDialog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;

import pl.mp107.plugtext.R;
import pl.mp107.plugtext.components.CodeEditor;
import pl.mp107.plugtext.constants.DefaultSyntaxHighlightColors;

public class MainActivity extends AppCompatActivity
        implements FileDialog.OnFileSelectedListener {

    private CodeEditor codeEditor;
    private boolean mStoragePermissionsGranted;
    private SharedPreferences sharedPreferences;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("settings", Context.MODE_MULTI_PROCESS);

        if (!sharedPreferences.getBoolean("first_run", false)) {
            storageInit();
            sharedPreferences.edit().putBoolean("first_run", true).apply();
        }

        codeEditor = (CodeEditor) findViewById(R.id.editor);
        try {
            Resources res = getResources();
            InputStream in_s = res.openRawResource(R.raw.java_code);
            byte[] b = new byte[in_s.available()];
            in_s.read(b);
            codeEditor.setText(new String(b));
        } catch (Exception e) {
            // e.printStackTrace();
            codeEditor.setText("Error: can't show help.");
        }

        checkStoragePermissions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
                Toast.makeText(MainActivity.this, R.string.action_search, Toast.LENGTH_SHORT).show();
                // TODO
                return true;
            case R.id.action_language:
                Toast.makeText(MainActivity.this, R.string.language, Toast.LENGTH_SHORT).show();
                // TODO
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void checkStoragePermissions() {
        int writeStoragePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int readStoragePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        mStoragePermissionsGranted =
                (writeStoragePermission == PackageManager.PERMISSION_GRANTED
                        && readStoragePermission == PackageManager.PERMISSION_GRANTED);
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
            fis.read(b);
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

        int newBackgroundColor = sharedPreferences.getInt("editor_background_color", Integer.MIN_VALUE);
        int newBuiltinsColor = sharedPreferences.getInt("editor_builtins_color", Integer.MIN_VALUE);
        int newCommentsColor = sharedPreferences.getInt("editor_comments_color", Integer.MIN_VALUE);
        int newKeywordsColor = sharedPreferences.getInt("editor_keywords_color", Integer.MIN_VALUE);
        int newNormalTextColor = sharedPreferences.getInt("editor_normal_text_color", Integer.MIN_VALUE);
        int newNumbersColor = sharedPreferences.getInt("editor_numbers_color", Integer.MIN_VALUE);
        int newPreprocessorsColor = sharedPreferences.getInt("editor_preprocessors_color", Integer.MIN_VALUE);
/*
        Log.d("DEBUG", "oldBackgroundColor" + String.format("#%06X", 0xFFFFFF & oldBackgroundColor));
        Log.d("DEBUG", "newBackgroundColor" + String.format("#%06X", 0xFFFFFF & newBackgroundColor));
        Log.d("DEBUG", "oldBuiltinsColor" + String.format("#%06X", 0xFFFFFF & oldBuiltinsColor));
        Log.d("DEBUG", "newBuiltinsColor" + String.format("#%06X", 0xFFFFFF & newBuiltinsColor));
        Log.d("DEBUG", "oldCommentsColor" + String.format("#%06X", 0xFFFFFF & oldCommentsColor));
        Log.d("DEBUG", "newCommentsColor" + String.format("#%06X", 0xFFFFFF & newCommentsColor));
        Log.d("DEBUG", "oldKeywordsColor" + String.format("#%06X", 0xFFFFFF & oldKeywordsColor));
        Log.d("DEBUG", "newKeywordsColor" + String.format("#%06X", 0xFFFFFF & newKeywordsColor));
        Log.d("DEBUG", "oldNormalTextColor" + String.format("#%06X", 0xFFFFFF & oldNormalTextColor));
        Log.d("DEBUG", "newNormalTextColor" + String.format("#%06X", 0xFFFFFF & newNormalTextColor));
        Log.d("DEBUG", "oldNumbersColor" + String.format("#%06X", 0xFFFFFF & oldNumbersColor));
        Log.d("DEBUG", "newNumbersColor" + String.format("#%06X", 0xFFFFFF & newNumbersColor));
        Log.d("DEBUG", "oldPreprocessorsColor" + String.format("#%06X", 0xFFFFFF & oldPreprocessorsColor));
        Log.d("DEBUG", "newPreprocessorsColor" + String.format("#%06X", 0xFFFFFF & newPreprocessorsColor));
*/
        if (newBackgroundColor != oldBackgroundColor
                || newBuiltinsColor != oldBuiltinsColor
                || newCommentsColor != oldCommentsColor
                || newKeywordsColor != oldKeywordsColor
                || newNormalTextColor != oldNormalTextColor
                || newNumbersColor != oldNumbersColor
                || newPreprocessorsColor != oldPreprocessorsColor) {
            codeEditor.setColorBackground(newBackgroundColor);
            codeEditor.setColorBuiltin(newBuiltinsColor);
            codeEditor.setColorComment(newCommentsColor);
            codeEditor.setColorKeyword(newKeywordsColor);
            codeEditor.setColorNormalText(newNormalTextColor);
            codeEditor.setColorNumber(newNumbersColor);
            codeEditor.setColorPreprocessors(newPreprocessorsColor);
            codeEditor.refreshSyntaxHighlight();
            //Log.d("DEBUG", "Syntax colors changed: true");
        }/* else
            Log.d("DEBUG", "Syntax colors changed: false");*/
    }

    private void storageInit() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("editor_background_color", DefaultSyntaxHighlightColors.editor_background_color);
        editor.putInt("editor_builtins_color", DefaultSyntaxHighlightColors.editor_builtins_color);
        editor.putInt("editor_comments_color", DefaultSyntaxHighlightColors.editor_comments_color);
        editor.putInt("editor_keywords_color", DefaultSyntaxHighlightColors.editor_keywords_color);
        editor.putInt("editor_normal_text_color", DefaultSyntaxHighlightColors.editor_normal_text_color);
        editor.putInt("editor_numbers_color", DefaultSyntaxHighlightColors.editor_numbers_color);
        editor.putInt("editor_preprocessors_color", DefaultSyntaxHighlightColors.editor_preprocessors_color);
        editor.apply();
    }
}
