package edu.uci.wmp.animalspan;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.uci.wmp.animalspan.R;

import edu.uci.wmp.animalspan.fragments.MainActivityFragment;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setDisplaySettings();
        initializeManagers();

        // Check whether the activity is using the layout version with
        // the fragment_container FrameLayout. If so, we must add the first fragment
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create an instance of ExampleFragment
            MainActivityFragment mainActivityFragment = new MainActivityFragment();

            // In case this activity was started with special instructions from an Intent,
            // pass the Intent's extras to the fragment as arguments
//            mainActivityFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getFragmentManager().beginTransaction().add(R.id.fragment_container, mainActivityFragment).commit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // punish user for aborting the test by saving decreased level by 1. Successful session closure is handled at SessionResults
        if (LevelManager.getInstance().testStarted && !LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_DEMO)) {
            LevelManager.getInstance().saveLevelToFile(false);
            Log.wtf("Activity onPause()", "You aborted the test!");
	        LevelManager.getInstance().testStarted = false;
            finish(); // quit entire session, user must restart game
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Util.dimSystemBar(this);
    }

    public void setDisplaySettings() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
    }

    public void initializeManagers() {
        LevelManager.getInstance().reset();
        LevelManager.getInstance().setContext(this);
        LevelManager.getInstance().readSharedPreferences();
        LevelManager.getInstance().setScreenDimensions();
        StimuliManager.getInstance().setContext(this);
        Checks.getInstance().setContext(this);
        CSVWriter.getInstance().setContext(this);
    }
}
