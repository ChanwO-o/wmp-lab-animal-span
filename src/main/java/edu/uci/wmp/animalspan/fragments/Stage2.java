package edu.uci.wmp.animalspan.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.uci.wmp.animalspan.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.uci.wmp.animalspan.CSVWriter;
import edu.uci.wmp.animalspan.LevelManager;
import edu.uci.wmp.animalspan.StimuliManager;
import edu.uci.wmp.animalspan.Util;

public class Stage2 extends Fragment {

    LinearLayout llGodMode;
    LinearLayout responseListLayout;
    LinearLayout stimuliChoicesLayout;
    List<Integer> stimuliChoices;
    int choiceStimuliSize;

    final double GODMODE_ANSWER_SIZE = 0.06;
    final double RESPNSE_SIZE = 0.13; // 13% of screen height
    final int RESPONSE_MARGINS = 5;
    final int PAUSE_TIME = 1000;

    private Handler handler = new Handler();
    long reactionStartTime;
    long pauseStartTime;
    boolean gamePaused;

    private Runnable pause = new Runnable() {
        @Override
        public void run() {
            long timeInMills = SystemClock.uptimeMillis() - pauseStartTime;
            if (timeInMills >= PAUSE_TIME)
                loadFeedbackScreen();
            else
                handler.postDelayed(this, 0);
        }
    };

    public Stage2() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	    LevelManager.getInstance().part = LevelManager.STAGE2;
        LevelManager.getInstance().currentStimuliIndex = 0;
        stimuliChoices = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stage2, container, false);

        TextView tvStimuliList = (TextView) view.findViewById(R.id.tvStimuliListStage2);
        llGodMode = (LinearLayout) view.findViewById(R.id.llGodMode);
        responseListLayout = (LinearLayout) view.findViewById(R.id.responselist);
        stimuliChoicesLayout = (LinearLayout) view.findViewById(R.id.stimulichoices);

        try {
            if (LevelManager.getInstance().debug) {
                tvStimuliList.setText(Util.iterableToString(LevelManager.getInstance().stimulisequence));
                fillGodModeLayout();
            }
            addChoicesToSet();
            displayChoices();
        } catch (IOException e) {
            e.printStackTrace();
        }


        reactionStartTime = SystemClock.uptimeMillis(); // first reaction timer starts when fragment is created

        return view;
    }

    private void fillGodModeLayout() throws IOException {
        for (Integer answer : LevelManager.getInstance().correctstimulisequence) {
            ImageView ivAnswer = new ImageView(getActivity());
            ivAnswer.setImageBitmap(StimuliManager.getInstance().getStimuli(answer));
            int godAnswerImageSize = Double.valueOf(LevelManager.getInstance().screen_height * GODMODE_ANSWER_SIZE).intValue();
            LinearLayout.LayoutParams godLayoutParams = new LinearLayout.LayoutParams(godAnswerImageSize, godAnswerImageSize);
            godLayoutParams.setMargins(RESPONSE_MARGINS, RESPONSE_MARGINS, RESPONSE_MARGINS, RESPONSE_MARGINS);
            ivAnswer.setLayoutParams(godLayoutParams);
            llGodMode.addView(ivAnswer);
        }
    }

    public void addChoicesToSet() {
        stimuliChoices.addAll(LevelManager.getInstance().distincttargets); // add targets
        stimuliChoices.addAll(LevelManager.getInstance().distinctdistractors); // add distractors
        stimuliChoices.addAll(getLures());
        if (LevelManager.getInstance().randomlydistribute)
            Collections.shuffle(stimuliChoices);
    }

    public List<Integer> getLures() {
        List<Integer> lureList = new ArrayList<>();
        for (int i = 0; i < LevelManager.getInstance().numberofperceptuallures; i++) {
            int imageNum = LevelManager.getInstance().distincttargets.get(i) - 100; // get the unlabeled image number
            lureList.add(imageNum + StimuliManager.PERCEPTUAL_LABEL);
        }
        for (int i = 0; i < LevelManager.getInstance().numberofsemanticlures; i++) {
            int imageNum = LevelManager.getInstance().distincttargets.get(i) - 100; // get the unlabeled image number
            lureList.add(imageNum + StimuliManager.SEMANTIC_LABEL);
        }
        return lureList;
    }

    public void displayChoices() throws IOException {
        int numberOfRows = stimuliChoices.size() / LevelManager.getInstance().stimuliperline;   // number of LinearLayouts needed
        int remainder = stimuliChoices.size() % LevelManager.getInstance().stimuliperline;      // checking remainder to determine number of rows needed

        if (remainder != 0) // e.g. 13 stimuli with 4 stim per line would give 3 rows only
            numberOfRows++; // so add 1

        choiceStimuliSize = setChoiceStimuliSize(numberOfRows); // set choice stimuli size after calculating rows

        for (int i = 0; i < numberOfRows; i++) {
            LinearLayout stimuliRow = new LinearLayout(getActivity()); // create a new LinearLayout for each row
            stimuliRow.setOrientation(LinearLayout.HORIZONTAL);

            for (int j = 0; j < LevelManager.getInstance().stimuliperline; j++) { // fill each layout with stimuli
                int stimuliIndex = (LevelManager.getInstance().stimuliperline * i +  j); // index to get stimuli from stimuliChoices list
                ImageView ivStim = createChoiceStimuli(stimuliChoices.get(stimuliIndex));
                stimuliRow.addView(ivStim);

                if (i == numberOfRows - 1 && j + 1 == remainder)    // for stages that have few stimuli on the last row (e.g. Level 12) break out of loop to prevent indexoutofbounds
                    break;
            }
            stimuliChoicesLayout.addView(stimuliRow);
        }
    }

    /**
     * Returns one of the ImageViews for the choice grid
     */
    public ImageView createChoiceStimuli(int labeledFilename) throws IOException {
        final ImageView ivStim = new ImageView(getActivity());
        ivStim.setImageBitmap(StimuliManager.getInstance().getStimuli(labeledFilename));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(choiceStimuliSize, choiceStimuliSize);
        int margin = (int) LevelManager.getInstance().gapbetweenimages * 5; // margin between each image in grid
        layoutParams.setMargins(margin, margin, margin, margin);
        ivStim.setLayoutParams(layoutParams);
        ivStim.setTag(labeledFilename); // set tag for click listener

        ivStim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gamePaused) {
                    addResponseImage((int) ivStim.getTag());
                    LevelManager.getInstance().rtsecondpart.add(SystemClock.uptimeMillis() - reactionStartTime); // add rt
                    reactionStartTime = SystemClock.uptimeMillis(); // reset rt timer

                    // add accuracy: checks if answers in secondpartsequence corespond to correctstimulisequence (which includes Targets only, ignoring distractors etc.)
                    int cur = LevelManager.getInstance().currentStimuliIndex;
	                Log.d("correct ss", LevelManager.getInstance().correctstimulisequence.get(cur) + "");
	                Log.d("second s", LevelManager.getInstance().secondpartsequence.get(cur) + "");
                    if (LevelManager.getInstance().correctstimulisequence.get(cur).equals(LevelManager.getInstance().secondpartsequence.get(cur))) {
                        LevelManager.getInstance().accuracysecondpart.add(StimuliManager.CORRECT);
                        LevelManager.getInstance().points += LevelManager.getInstance().level; // points added by current level
                    }
                    else
                        LevelManager.getInstance().accuracysecondpart.add(StimuliManager.INCORRECT);

                    CSVWriter.getInstance().collectData();

                    // check if response is complete
                    if (LevelManager.getInstance().secondpartsequence.size() == LevelManager.getInstance().setsize) { // if number of choices matches target size
                        pauseStartTime = SystemClock.uptimeMillis();
                        gamePaused = true;
                        handler.postDelayed(pause, 0);
                    }
                    LevelManager.getInstance().currentStimuliIndex++; // increment current index
                }
            }
        });
        return ivStim;
    }

    public int setChoiceStimuliSize(int rows) {
        switch(rows) {
            case 1:
                return StimuliManager.MIN_CHOICE_STIMULI_SIZE + StimuliManager.CHOICE_STIMULI_SIZE_MULTIPLIER * 10;
            case 2:
                return StimuliManager.MIN_CHOICE_STIMULI_SIZE + StimuliManager.CHOICE_STIMULI_SIZE_MULTIPLIER * 6;
            case 3:
                return StimuliManager.MIN_CHOICE_STIMULI_SIZE + StimuliManager.CHOICE_STIMULI_SIZE_MULTIPLIER * 3;
            case 4:
                return StimuliManager.MIN_CHOICE_STIMULI_SIZE + StimuliManager.CHOICE_STIMULI_SIZE_MULTIPLIER;
            case 5:
                return StimuliManager.MIN_CHOICE_STIMULI_SIZE;
            default:
                return StimuliManager.MIN_CHOICE_STIMULI_SIZE;
        }
    }

    public void addResponseImage(int imageTag) {
        LevelManager.getInstance().secondpartsequence.add(imageTag); // keeping track of user's selections
        if (!LevelManager.getInstance().showchoicefeedback) // add image to responselist only if this is true
            return;

        try {
            ImageView ivResponse = new ImageView(getActivity());
            ivResponse.setImageBitmap(StimuliManager.getInstance().getStimuli(imageTag));
            int responseImageSize = Double.valueOf(LevelManager.getInstance().screen_height * RESPNSE_SIZE).intValue();
            LinearLayout.LayoutParams responseLayoutParams = new LinearLayout.LayoutParams(responseImageSize, responseImageSize);
            responseLayoutParams.setMargins(RESPONSE_MARGINS, RESPONSE_MARGINS, RESPONSE_MARGINS, RESPONSE_MARGINS);
            ivResponse.setLayoutParams(responseLayoutParams);
            responseListLayout.addView(ivResponse);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFeedbackScreen() {
        Util.loadFragment(getActivity(), new LevelFeedback());
    }
}
