package com.example.chanwoo.animalspantestproject;

import android.app.ActionBar;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.chanwoo.animalspantestproject.fragments.MainActivityFragment;
import com.example.chanwoo.animalspantestproject.fragments.Stage1;
import com.example.chanwoo.animalspantestproject.fragments.Stage2;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setDisplaySettings();
        initializeLevelManager();

//        LevelManager.getInstance().loadLevel(LevelManager.getInstance().startlevel);

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
    public void onBackPressed() { // I modified Level1 file abortallowed=1 to test this; fix it back!
        if (LevelManager.getInstance().testStarted && !LevelManager.getInstance().abortallowed)
            return; // don't allow user to exit

        if (LevelManager.getInstance().testStarted)
            LevelManager.getInstance().saveLevelToFile(); // save level before exit if in the middle of a test
        super.onBackPressed();
    }

    public void setDisplaySettings() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        Util.dimSystemBar(this);
    }

    public void initializeLevelManager() {
        LevelManager.getInstance().reset();
        LevelManager.getInstance().setContext(this);
        LevelManager.getInstance().loadSavedLevel(); // sets level variable if there is a saved instance
        LevelManager.getInstance().setScreenDimensions();
    }
}
