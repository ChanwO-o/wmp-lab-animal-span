package edu.uci.wmp.animalspan.fragments;

import android.app.Fragment;
import android.os.Bundle;
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

import com.uci.wmp.animalspan.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

import edu.uci.wmp.animalspan.Checks;
import edu.uci.wmp.animalspan.LevelManager;
import edu.uci.wmp.animalspan.StimuliManager;
import edu.uci.wmp.animalspan.Util;
import edu.uci.wmp.animalspan.fragments.questions.ReflectionQuestion;

public class LevelFeedback extends Fragment implements View.OnClickListener {

    TextView tvFeedbackPhrase;
    ImageView ivDemoQuit;
    ImageView ivLevelFeedbackNext;
    long levelFeedbackStartTime;

    final int LEVELFEEDBACK_SHOW_BUTTON_TIME = 1;
    final double FEEDBACK_PERCENTAGE = 0.25; // 25% of width
    final double FEEDBACK_TOPMARGIN_PERCENTAGE = 0.10; // top margin of height
    final int LEVEL_UP = 1;
    final int LEVEL_SAME = 0;
    final int LEVEL_DOWN = -1;

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

        tvFeedbackPhrase = (TextView) view.findViewById(R.id.tvFeedbackPhrase);
        ImageView ivFeedback = (ImageView) view.findViewById(R.id.ivFeedback);
        ivDemoQuit = (ImageView) view.findViewById(R.id.ivDemoQuit);
        ivLevelFeedbackNext = (ImageView) view.findViewById(R.id.ivLevelFeedbackNext);

        // set smiley face dimensions & gravity
        int imageSize = Double.valueOf(LevelManager.getInstance().screen_width * FEEDBACK_PERCENTAGE).intValue();
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(imageSize, imageSize);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.topMargin = Double.valueOf(LevelManager.getInstance().screen_height * FEEDBACK_TOPMARGIN_PERCENTAGE).intValue();
        ivFeedback.setLayoutParams(layoutParams);

        // display button 1 second after loading
        ivLevelFeedbackNext.setVisibility(View.GONE);
        ivLevelFeedbackNext.setOnClickListener(this);

        // show quit button for demo mode only
        if (!LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_DEMO))
            ivDemoQuit.setVisibility(View.GONE);
        ivDemoQuit.setOnClickListener(this);

        // feedback
        try {
            if (check())
                ivFeedback.setImageBitmap(StimuliManager.getInstance().getFeedbackAsset(StimuliManager.CORRECT));
            else
                ivFeedback.setImageBitmap(StimuliManager.getInstance().getFeedbackAsset(StimuliManager.INCORRECT));
        } catch ( IOException e) { e.printStackTrace(); }

        handler.postDelayed(showButton, 0);

        calculateNextLevel();

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
            long currentMillsInSession = SystemClock.uptimeMillis();
            int currentSecondsInSession = (int) (currentMillsInSession - LevelManager.getInstance().sessionStartMills) / 1000;
            if (currentSecondsInSession > LevelManager.getInstance().sessionLength) {
                viewResults();
                return;
            }
        }
        else if (LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_ROUNDS)
                || LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_DEMO)) {
            if (LevelManager.getInstance().numberoftrials == LevelManager.getInstance().trial) {
                viewResults();
                return;
            }
        }
        Util.loadFragment(getActivity(), new GetReady());
    }

    /**
     * Follows algorithm to determine which level comes next
     */
    public void calculateNextLevel() {
        boolean first = !LevelManager.getInstance().accuracyfirstpart.contains(StimuliManager.INCORRECT);
        boolean second = check();

        if (first && second && LevelManager.getInstance().level < LevelManager.MAX_LEVEL) { // advance to next level
            Log.wtf("LEVEL_UP", "++");
            LevelManager.getInstance().level++;
            if (LevelManager.getInstance().numberoftrials != LevelManager.getInstance().trial) // don't display feedback phrase on last trial
                tvFeedbackPhrase.setText(getFeedbackPhrase(LEVEL_UP));
        }
        else if (!second && LevelManager.getInstance().level > LevelManager.MIN_LEVEL) { // go back one level
            Log.wtf("LEVEL_DOWN", "--");
            LevelManager.getInstance().level--;
            if (LevelManager.getInstance().numberoftrials != LevelManager.getInstance().trial)
                tvFeedbackPhrase.setText(getFeedbackPhrase(LEVEL_DOWN));
        }
        else { // stay on current level
            Log.wtf("LEVEL_SAME", "==");
            if (LevelManager.getInstance().numberoftrials != LevelManager.getInstance().trial)
                tvFeedbackPhrase.setText(getFeedbackPhrase(LEVEL_SAME));
        }
    }

    public void viewResults() {
        Log.i("viewResults()", "Session is over");
        if (LevelManager.getInstance().questions)
            Util.loadFragment(getActivity(), new ReflectionQuestion());
        else
            Util.loadFragment(getActivity(), new SessionResults());
    }

    /**
     * Return random phrase from feedback phrase files with removed quotation marks, separated into lines
     * Iterate down random number of lines in file
     */
    public String getFeedbackPhrase(int next) {
        String whichFile = "";
        if (next == LEVEL_UP)
	        whichFile = "roundfeedback_up.txt";
        else if (next == LEVEL_DOWN)
	        whichFile = "roundfeedback_down.txt";
        else if (next == LEVEL_SAME)
	        whichFile = "roundfeedback_same.txt";

	    String path = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + Checks.FEEDBACKFOLDER_PATH + whichFile;
	    String line = "";
	    File feedbackFile = new File(path);
        try
        {
	        InputStream in = new FileInputStream(feedbackFile);
	        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//            reader.mark(0); // when reset() is called, reader will return to line 0 after reaching last line in file
            int r = new Random().nextInt(200);
            for (int i = 0; i < r; ++i) {
                if ((line = reader.readLine()) == null) { // reached end of phrases file
	                in = new FileInputStream(feedbackFile); // reset inputstream and reader
	                reader = new BufferedReader(new InputStreamReader(in));
                    line = reader.readLine();
                }
//                Log.d("getFeedbackPhrase()", "Reading line " + line);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        // replace punctuation with newline characters
        String[] punc = new String[] {". ", "! ", "? "}; // keep one space after punctuation for effectiveness
        for (String p : punc)
            if (line != null)
                line = line.replace(p, p + "\n");
        return line;
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
