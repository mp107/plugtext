package pl.mp107.plugtext.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import pl.mp107.plugtext.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                Toast.makeText(MainActivity.this,R.string.action_open_file,Toast.LENGTH_SHORT).show();
                // TODO
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
}
