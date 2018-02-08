package pl.mp107.plugtext.activities;

import android.Manifest;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.rustamg.filedialogs.FileDialog;
import com.rustamg.filedialogs.OpenFileDialog;

import java.io.InputStream;

import pl.mp107.plugtext.R;
import pl.mp107.plugtext.components.CodeEditor;

public class MainActivity extends AppCompatActivity {

    private CodeEditor codeEditor;
    private boolean mStoragePermissionsGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        codeEditor = (CodeEditor)findViewById(R.id.editor);

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
                //Toast.makeText(MainActivity.this,R.string.action_open_file,Toast.LENGTH_SHORT).show();
                // TODO
                if (mStoragePermissionsGranted) {
                    showFileDialog(new OpenFileDialog(), OpenFileDialog.class.getName());
                } else {
                    showStoragePermissionsError();
                }
                return true;
            case R.id.action_save_file:
                Toast.makeText(MainActivity.this,R.string.action_save_file,Toast.LENGTH_SHORT).show();
                // TODO
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void checkStoragePermissions() {
        int writeStoragePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int readStoragePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (writeStoragePermission == PackageManager.PERMISSION_GRANTED && readStoragePermission == PackageManager.PERMISSION_GRANTED) {
            mStoragePermissionsGranted = true;
        } else {
            mStoragePermissionsGranted = false;
        }
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
}
