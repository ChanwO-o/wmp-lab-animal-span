package edu.uci.wmp.animalspan.fragments;

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
import android.widget.TextView;

import edu.uci.wmp.animalspan.LevelManager;
import com.uci.wmp.animalspan.R;
import edu.uci.wmp.animalspan.StimuliManager;
import edu.uci.wmp.animalspan.Util;
import edu.uci.wmp.animalspan.fragments.questions.ReflectionQuestion;

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
        LevelManager.getInstance().part = LevelManager.STAGE0;
        levelFeedbackStartTime = SystemClock.uptimeMillis();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_level_feedback, container, false);

//        TextView tvTrialsLeft = (TextView) view.findViewById(R.id.tvTrialsLeft); // moved this to GetReady
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

        Log.d("stimsequence", StimuliManager.iterableToString(LevelManager.getInstance().stimulisequence));
        Log.d("Answer", StimuliManager.iterableToString(LevelManager.getInstance().correctstimulisequence));
        Log.d("Response", StimuliManager.iterableToString(LevelManager.getInstance().secondpartsequence));
        Log.w("checking", "" + check());

        // feedback
        try {
            if (check())
                ivFeedback.setImageBitmap(StimuliManager.getFeedbackAsset(getActivity(), StimuliManager.CORRECT));
            else
                ivFeedback.setImageBitmap(StimuliManager.getFeedbackAsset(getActivity(), StimuliManager.INCORRECT));
        } catch ( IOException e) { e.printStackTrace(); }

//        LevelManager.getInstance().trial++; // one trial completed
//        String trials = "Trials left: " + (LevelManager.getInstance().numberoftrials - LevelManager.getInstance().trial);
//        tvTrialsLeft.setText(trials); // moved this chunk to GetReady

        handler.postDelayed(showButton, 0);

        return view;
    }

    /**
     * Returns stage pass / no pass
     */
    public boolean check() {
        return !LevelManager.getInstance().accuracysecondpart.contains(StimuliManager.INCORRECT);
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
        else if (LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_ROUNDS)) {
            Log.d("mode", "levels");
            if (LevelManager.getInstance().numberoftrials == LevelManager.getInstance().trial) {
                Log.d("Results", "session over");
                viewResults();
                return; // remove this after implementing viewResults()
            }

        }
        calculateNextLevel();
        Util.loadFragment(getActivity(), new GetReady());
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
//        Util.loadFragment(getActivity(), new SessionResults());
        if (LevelManager.getInstance().questions)
            Util.loadFragment(getActivity(), new ReflectionQuestion());
        else
            Util.loadFragment(getActivity(), new SessionResults());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivLevelFeedbackNext:
                goToNextLevel();
                break;

            case R.id.ivDemoQuit:
                Util.loadFragment(getActivity(), new MainActivityFragment());
                break;
        }
    }
}
