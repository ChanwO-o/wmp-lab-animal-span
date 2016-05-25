package edu.uci.wmp.animalspan.fragments;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import edu.uci.wmp.animalspan.CSVWriter;
import edu.uci.wmp.animalspan.LevelManager;
import com.uci.wmp.animalspan.R;
import edu.uci.wmp.animalspan.StimuliManager;
import edu.uci.wmp.animalspan.Util;

import java.io.IOException;

public class Stage1 extends Fragment implements View.OnClickListener {

    final int FEEDBACK_TIME = 1000;
    final int HIDE_TIME = 250;
    final double STIMULI_SIZE_PERCENTAGE = 0.35; // 35% of width
    final int NULL = -1;

    private TextView tvTimer;
    private ImageView ivStage1Stimuli;

    boolean responded;
    long stimuliStartTime;
    long feedbackStartTime;
    long responseTime;

    private Handler handler = new Handler();

    /**
     * Measure user's response time in milliseconds
     * If response time has surpassed LevelManager.timetoanswerfirstpart milliseconds, show red feedback and go to next stimuli
     * Once user has responded, show feedback for LevelManager.FEEDBACK_TIME milliseconds, then move on to next step
     */
    private Runnable response = new Runnable() {
        @Override
        public void run() {
            responseTime = SystemClock.uptimeMillis() - stimuliStartTime;

            // timer text
            int seconds = (int) (responseTime/1000);
            int milliseconds = (int)(responseTime % 1000);
            String time = String.format("%02d", seconds) + ":" + String.format("%03d", milliseconds);
            tvTimer.setText(time);

            // user responds too slow
            if (seconds >= LevelManager.getInstance().timetoanswerfirstpart && !responded) {
                answer(StimuliManager.NOANSWER);
                LevelManager.getInstance().rtfirstpart.add((long) NULL);
                CSVWriter.getInstance().collectData();
            }

            // show feedback for FEEDBACK_TIME
            if (responded) {
                long feedbackTimeInMills = SystemClock.uptimeMillis() - feedbackStartTime;
//                int feedbackSeconds = (int) (feedbackTimeInMills/1000);
                if (FEEDBACK_TIME <= feedbackTimeInMills && feedbackTimeInMills <= FEEDBACK_TIME + HIDE_TIME) {
                    setViewsVisible(View.GONE);
                    handler.postDelayed(this, 0);
                }
                else if (FEEDBACK_TIME + HIDE_TIME <= feedbackTimeInMills) {
                    nextStimuliOrProceed();
                }
                else
                    handler.postDelayed(this, 0); // keep showing feedback
            }
            else
                handler.postDelayed(this, 0); // loop
        }
    };

    public Stage1() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LevelManager.getInstance().part = LevelManager.STAGE1;
        LevelManager.getInstance().currentStimuliIndex = 0;
        responded = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stage1, container, false);

//        TextView tvStimuliList = (TextView) view.findViewById(R.id.tvStimuliListStage1);
        tvTimer = (TextView) view.findViewById(R.id.tvStage1Timer);
        ivStage1Stimuli = (ImageView) view.findViewById(R.id.ivStage1Stimuli);
        ImageView ivUpdoswn = (ImageView) view.findViewById(R.id.ivUpdown);
        ImageView ivRightup = (ImageView) view.findViewById(R.id.ivRightup);

        /*Double percentage = Double.valueOf(LevelManager.getInstance().sizeoffirststimuli.split(",")[1]) / 100; // percentage of screen height
        int imageSize = Double.valueOf(LevelManager.getInstance().screen_width * percentage).intValue(); // 50% of screen width ("50,50") */

        int imageSize = Double.valueOf(LevelManager.getInstance().screen_width * STIMULI_SIZE_PERCENTAGE).intValue();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(imageSize, imageSize); // can use LinearLayout.LayoutParams because ivStage1Stimuli is inside of a LinearLayout
        ivStage1Stimuli.setLayoutParams(layoutParams);

        ivUpdoswn.setOnClickListener(this);
        ivRightup.setOnClickListener(this);

//        tvStimuliList.setText(StimuliManager.listToString(LevelManager.getInstance().stimulisequence));
        stimuliStartTime = SystemClock.uptimeMillis();

        handler.postDelayed(response, 0); // start_old loop
        displayStimuli();

        return view;
    }

    @Override
    public void onPause() {
        Log.d("Stage1", "onPause()");
        handler.removeCallbacks(response);
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivUpdown:
                if (!responded) {
                    answer(StimuliManager.UPDOWN);
                    CSVWriter.getInstance().collectData();
                }

                break;

            case R.id.ivRightup:
                if (!responded) {
                    answer(StimuliManager.RIGHTUP);
                    CSVWriter.getInstance().collectData();
                }
                break;
        }
    }

    /**
     * Fix: only the stimuli appears/disappears, leave the buttons visible
     */
    public void setViewsVisible(int visibility) {
        ivStage1Stimuli.setVisibility(visibility);
    }

    /**
     * Checks for next stimuli, or proceed to Stage 2
     */
    public void nextStimuliOrProceed() {
        if (LevelManager.getInstance().currentStimuliIndex < LevelManager.getInstance().stimulisequence.size() - 1) { // go on to next stimuli
            LevelManager.getInstance().currentStimuliIndex++;
            responded = false;
            ivStage1Stimuli.setBackgroundColor(Color.TRANSPARENT); // reset feedback
            stimuliStartTime = SystemClock.uptimeMillis();
            handler.postDelayed(response, 0); // loop again starting with next stimuli
            displayStimuli();
            setViewsVisible(View.VISIBLE); // put this line here than inside the runnable to prevent views popping up suddenly when proceeding to Stage2
        } else { // done with all stimuli
            handler.removeCallbacks(response); //finish
            Util.loadFragment(getActivity(), new Stage2());
        }
    }

    /**
     * Answer current stimuli orientation with given answer
     * Using StimuliManager.UPDOWN, RIGHTUP or NOANSWER
     */
    public void answer(int orientation) {
        LevelManager.getInstance().responsesfirstpart.add(orientation); // append response to responses list
        LevelManager.getInstance().rtfirstpart.add(responseTime); // append reaction time

        if (orientation == LevelManager.getInstance().presentationstyle.get(LevelManager.getInstance().currentStimuliIndex)) // append accuracy
            LevelManager.getInstance().accuracyfirstpart.add(StimuliManager.CORRECT);
        else
            LevelManager.getInstance().accuracyfirstpart.add(StimuliManager.INCORRECT);

        if (LevelManager.getInstance().showbuttonpressfeedback) // give feedback if showbuttonpressfeedback is true
            giveFeedback();
        responded = true;
        feedbackStartTime = SystemClock.uptimeMillis(); // start_old displaying feedback
    }

    /**
     * Display stimuli at current index, set rotation
     */
    public void displayStimuli() {
        try {
//            Bitmap b = StimuliManager.getStimuli(getActivity(), LevelManager.getInstance().stimulisequence.get(LevelManager.getInstance().currentStimuliIndex));
            Bitmap b = StimuliManager.getStimuli(LevelManager.getInstance().stimulisequence.get(LevelManager.getInstance().currentStimuliIndex));
            ivStage1Stimuli.setImageBitmap(b);
            if (LevelManager.getInstance().presentationstyle.get(LevelManager.getInstance().currentStimuliIndex) == StimuliManager.UPDOWN)
                ivStage1Stimuli.setRotation(180);
            else
                ivStage1Stimuli.setRotation(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Draws a red or green border around stimuli
     */
    public void giveFeedback() {
        if (LevelManager.getInstance().accuracyfirstpart.get(LevelManager.getInstance().currentStimuliIndex) == StimuliManager.CORRECT)
            ivStage1Stimuli.setBackgroundColor(Color.GREEN); // correct
        else
            ivStage1Stimuli.setBackgroundColor(Color.RED); // incorrect
    }
}
