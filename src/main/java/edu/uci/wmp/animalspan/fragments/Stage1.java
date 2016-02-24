package edu.uci.wmp.animalspan.fragments;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import edu.uci.wmp.animalspan.LevelManager;
import com.uci.wmp.animalspan.R;
import edu.uci.wmp.animalspan.StimuliManager;

import java.io.IOException;

public class Stage1 extends Fragment implements View.OnClickListener {

    final int FEEDBACK_TIME = 1000;
    final int HIDE_TIME = 250;
    final double STIMULI_SIZE_PERCENTAGE = 0.35; // 35% of width

    private TextView tvTimer;
    private ImageView ivStage1Stimuli;
    private ImageView ivUpdoswn;
    private ImageView ivRightup;

    int currentStimuliIndex; // index of current pic
    boolean responded;
    long stimuliStartTime;
    long feedbackStartTime;

    private Handler handler = new Handler();

    /**
     * Measure user's response time in milliseconds
     * If response time has surpassed LevelManager.timetoanswerfirstpart milliseconds, show red feedback and go to next stimuli
     * Once user has responded, show feedback for LevelManager.FEEDBACK_TIME milliseconds, then move on to next step
     */
    private Runnable response = new Runnable() {
        @Override
        public void run() {
            long timeInMills = SystemClock.uptimeMillis() - stimuliStartTime;

            // timer text
            int seconds = (int) (timeInMills/1000);
            int milliseconds = (int)(timeInMills % 1000);
            String time = String.format("%02d", seconds) + ":" + String.format("%03d", milliseconds);
            tvTimer.setText(time);

            // user responds too slow
            if (seconds >= LevelManager.getInstance().timetoanswerfirstpart && !responded)
                answer(StimuliManager.NOANSWER);

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
        currentStimuliIndex = 0;
        responded = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stage1, container, false);

        TextView tvStimuliList = (TextView) view.findViewById(R.id.tvStimuliListStage1);
        tvTimer = (TextView) view.findViewById(R.id.tvStage1Timer);
        ivStage1Stimuli = (ImageView) view.findViewById(R.id.ivStage1Stimuli);
        ivUpdoswn = (ImageView) view.findViewById(R.id.ivUpdown);
        ivRightup = (ImageView) view.findViewById(R.id.ivRightup);

        /*Double percentage = Double.valueOf(LevelManager.getInstance().sizeoffirststimuli.split(",")[1]) / 100; // percentage of screen height
        int imageSize = Double.valueOf(LevelManager.getInstance().screen_width * percentage).intValue(); // 50% of screen width ("50,50") */

        int imageSize = Double.valueOf(LevelManager.getInstance().screen_width * STIMULI_SIZE_PERCENTAGE).intValue();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(imageSize, imageSize); // can use LinearLayout.LayoutParams because ivStage1Stimuli is inside of a LinearLayout
        ivStage1Stimuli.setLayoutParams(layoutParams);

        ivUpdoswn.setOnClickListener(this);
        ivRightup.setOnClickListener(this);

        tvStimuliList.setText(StimuliManager.listToString(LevelManager.getInstance().stimulisequence));
        stimuliStartTime = SystemClock.uptimeMillis();

        handler.postDelayed(response, 0); // start_old loop
        displayStimuli();

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivUpdown:
                if (!responded)
                    answer(StimuliManager.UPDOWN);
                break;

            case R.id.ivRightup:
                if (!responded)
                    answer(StimuliManager.RIGHTUP);
                break;
        }
    }

    /**
     * Fix: only the stimuli appears/disappears, leave the buttons visible
     */
    public void setViewsVisible(int visibility) {
        ivStage1Stimuli.setVisibility(visibility);
//        ivUpdoswn.setVisibility(visibility);
//        ivRightup.setVisibility(visibility);
    }

    /**
     * Checks for next stimuli, or proceed to Stage 2
     */
    public void nextStimuliOrProceed() {
        if (currentStimuliIndex < LevelManager.getInstance().stimulisequence.size() - 1) { // go on to next stimuli
            currentStimuliIndex++;
            responded = false;
            ivStage1Stimuli.setBackgroundColor(Color.TRANSPARENT); // reset feedback
            stimuliStartTime = SystemClock.uptimeMillis();
            handler.postDelayed(response, 0); // loop again starting with next stimuli
            displayStimuli();
            setViewsVisible(View.VISIBLE); // put this line here than inside the runnable to prevent views popping up suddenly when proceeding to Stage2
        } else { // done with all stimuli
            handler.removeCallbacks(response); //finish
            FragmentManager fm = getActivity().getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            Stage2 stage2 = new Stage2();
            ft.replace(R.id.fragment_container, stage2);
            ft.commit();
        }
    }

    /**
     * Answer current stimuli orientation with given answer
     * Using StimuliManager.UPDOWN, RIGHTUP or NOANSWER
     */
    public void answer(int orientation) {
        LevelManager.getInstance().responsesfirstpart.add(orientation); // append response to responses list
        int responseTime = (int) (SystemClock.uptimeMillis() - stimuliStartTime);
        LevelManager.getInstance().rtfirstpart.add(responseTime); // append reaction time
        if (LevelManager.getInstance().showbuttonpressfeedback) // give feedback if showbuttonpressfeedback is true
            giveFeedback(orientation);
        responded = true;
        feedbackStartTime = SystemClock.uptimeMillis(); // start_old displaying feedback
    }

    /**
     * Display stimuli at current index, set rotation
     */
    public void displayStimuli() {
        try {
            Bitmap b = StimuliManager.getStimuli(getActivity(), LevelManager.getInstance().stimulisequence.get(currentStimuliIndex));
            ivStage1Stimuli.setImageBitmap(b);
            if (LevelManager.getInstance().presentationstyle.get(currentStimuliIndex) == StimuliManager.UPDOWN)
                ivStage1Stimuli.setRotation(180);
            else
                ivStage1Stimuli.setRotation(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Draws a red or green border around stimuli
     * @param orientation user's choice on whether stimuli is upright or upside down
     */
    public void giveFeedback(int orientation) {
        if (orientation == LevelManager.getInstance().presentationstyle.get(currentStimuliIndex)) {
            ivStage1Stimuli.setBackgroundColor(Color.GREEN); // correct
            LevelManager.getInstance().accuracyfirstpart.add(StimuliManager.CORRECT);
        }
        else {
            ivStage1Stimuli.setBackgroundColor(Color.RED); // incorrect
            LevelManager.getInstance().accuracyfirstpart.add(StimuliManager.INCORRECT);
        }
    }
}
