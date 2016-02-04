package com.example.chanwoo.animalspantestproject.fragments;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.chanwoo.animalspantestproject.LevelManager;
import com.example.chanwoo.animalspantestproject.R;
import com.example.chanwoo.animalspantestproject.StimuliManager;

import java.io.IOException;

public class LevelFeedback extends Fragment implements View.OnClickListener {

    ImageView ivDemoQuit;
    ImageView ivLevelFeedbackNext;
    long levelFeedbackStartTime;

    final int LEVELFEEDBACK_SHOW_BUTTON_TIME = 1;
    final double FEEDBACK_PERCENTAGE = 0.25; // 25% of width

    private Handler handler = new Handler();

    private Runnable showButton = new Runnable() {
        @Override
        public void run() {
            long timeInMills = SystemClock.uptimeMillis() - levelFeedbackStartTime;
            int seconds = (int) timeInMills / 1000;

            if (seconds >= LEVELFEEDBACK_SHOW_BUTTON_TIME) {
                ivLevelFeedbackNext.setVisibility(View.VISIBLE);
            } else {
                handler.postDelayed(this, 0); // loop until button is visible
            }
        }
    };

    public LevelFeedback() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        levelFeedbackStartTime = SystemClock.uptimeMillis();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_level_feedback, container, false);

        TextView tvTrialsLeft = (TextView) view.findViewById(R.id.tvTrialsLeft);
        ImageView ivFeedback = (ImageView) view.findViewById(R.id.ivFeedback);
        ivDemoQuit = (ImageView) view.findViewById(R.id.ivDemoQuit);
        ivLevelFeedbackNext = (ImageView) view.findViewById(R.id.ivLevelFeedbackNext);

        // set smiley face dimensions & gravity
        int imageSize = Double.valueOf(LevelManager.getInstance().screen_width * FEEDBACK_PERCENTAGE).intValue();
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(imageSize, imageSize);
        layoutParams.gravity = Gravity.CENTER;
        ivFeedback.setLayoutParams(layoutParams);

        // display button 1 second after loading
        ivLevelFeedbackNext.setVisibility(View.GONE);
        ivLevelFeedbackNext.setOnClickListener(this);

        // show quit button for demo mode only
        if (!LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_DEMO))
            ivDemoQuit.setVisibility(View.GONE);
        ivDemoQuit.setOnClickListener(this);

        Log.d("stimsequence", StimuliManager.listToString(LevelManager.getInstance().stimulisequence));
        Log.d("Answer", StimuliManager.listToString(LevelManager.getInstance().correctstimulisequence));
        Log.d("Response", StimuliManager.listToString(LevelManager.getInstance().secondpartsequence));
        Log.w("checking", "" + check());

        // feedback
        try {
            if (check())
                ivFeedback.setImageBitmap(StimuliManager.getFeedbackAsset(getActivity(), StimuliManager.CORRECT));
            else
                ivFeedback.setImageBitmap(StimuliManager.getFeedbackAsset(getActivity(), StimuliManager.INCORRECT));
        } catch ( IOException e) { e.printStackTrace(); }

        LevelManager.getInstance().numberoftrials -= 1; // one trial completed
        String trials = "Trials left: " + LevelManager.getInstance().numberoftrials;
        tvTrialsLeft.setText(trials);

        handler.postDelayed(showButton, 0);

        return view;
    }

    /**
     * Check if answers in secondpartsequence corespond to correctstimulisequence (checking Targets only, ignore distractors etc.)
     */
    public boolean check() {
        for (int i = 0; i < LevelManager.getInstance().correctstimulisequence.size(); i++) {
            if (LevelManager.getInstance().correctstimulisequence.get(i) != LevelManager.getInstance().secondpartsequence.get(i))
                return false;
        }
        return true;
    }

    public void goToNextLevel() {
        if (LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_TIME)) {
            Log.d("mode", "time");
            long currentMillsInSession = SystemClock.uptimeMillis();
            int currentSecondsInSession = (int) (currentMillsInSession - LevelManager.getInstance().sessionStartMills) / 1000;
            if (currentSecondsInSession > LevelManager.getInstance().sessionLength) {
                Log.d("Results", "session over");
                viewResults();
                return; // remove this after implementing viewResults()
            }
        }
        else if (LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_LEVELS)) {
            Log.d("mode", "levels");
            if (LevelManager.getInstance().numberoftrials == 0) {
                Log.d("Results", "session over");
                viewResults();
                return; // remove this after implementing viewResults()
            }

        }
        calculateNextLevel();
        FragmentManager fm = getActivity().getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        GetReady getReady = new GetReady();
        ft.replace(R.id.fragment_container, getReady);
        ft.commit();
    }

    /**
     * Follows algorithm to determine which level comes next
     */
    public void calculateNextLevel() {
        boolean first = !LevelManager.getInstance().accuracyfirstpart.contains(StimuliManager.INCORRECT);
        boolean second = check();

        if (first && second && LevelManager.getInstance().level < LevelManager.MAX_LEVEL) // advance to next level
            LevelManager.getInstance().level++;
        else if (!second && LevelManager.getInstance().level > LevelManager.MIN_LEVEL) // go back one level
            LevelManager.getInstance().level--;
//        else if (!first && second) // stay on current level
            // do nothing;
    }

    public void viewResults() {
        FragmentManager fm = getActivity().getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        SessionResults sessionResults = new SessionResults();
        ft.replace(R.id.fragment_container, sessionResults);
        ft.commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivLevelFeedbackNext:
                goToNextLevel();
                break;

            case R.id.ivDemoQuit:
                FragmentManager fm = getActivity().getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                MainActivityFragment mainActivityFragment = new MainActivityFragment();
                ft.replace(R.id.fragment_container, mainActivityFragment);
                ft.commit();
                break;
        }
    }
}
