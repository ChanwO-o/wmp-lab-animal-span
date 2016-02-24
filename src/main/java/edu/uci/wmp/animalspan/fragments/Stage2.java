package edu.uci.wmp.animalspan.fragments;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Stage2 extends Fragment {

    LinearLayout responseListLayout;
    LinearLayout stimuliChoicesLayout;
    List<Integer> stimuliChoices;
    int choiceStimuliSize;

    final int RESPONSE_MARGINS = 5;
    final int PAUSE_TIME = 1000;

    private Handler handler = new Handler();
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
        stimuliChoices = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stage2, container, false);

        TextView tvStimuliList = (TextView) view.findViewById(R.id.tvStimuliListStage2);
        responseListLayout = (LinearLayout) view.findViewById(R.id.responselist);
        stimuliChoicesLayout = (LinearLayout) view.findViewById(R.id.stimulichoices);

        tvStimuliList.setText(StimuliManager.listToString(LevelManager.getInstance().stimulisequence));

        addChoicesToSet();
        displayChoices();

        return view;
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

    public void displayChoices() {
        try {
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
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns one of the ImageViews for the choice grid
     */
    public ImageView createChoiceStimuli(int labeledFilename) throws IOException {
        final ImageView ivStim = new ImageView(getActivity());
        ivStim.setImageBitmap(StimuliManager.getStimuli(getActivity(), labeledFilename));
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
                    // check if response is complete
                    if (LevelManager.getInstance().secondpartsequence.size() == LevelManager.getInstance().setsize) { // if number of choices matches target size
                        pauseStartTime = SystemClock.uptimeMillis();
                        gamePaused = true;
                        handler.postDelayed(pause, 0);
                    }
                }
            }
        });
        return ivStim;
    }

    public int setChoiceStimuliSize(int rows) {
        switch(rows) {
            case 1:
                return StimuliManager.MIN_CHOICE_STIMULI_SIZE + StimuliManager.CHOICE_STIMULI_SIZE_MULTIPLIER * 4;
            case 2:
                return StimuliManager.MIN_CHOICE_STIMULI_SIZE + StimuliManager.CHOICE_STIMULI_SIZE_MULTIPLIER * 3;
            case 3:
                return StimuliManager.MIN_CHOICE_STIMULI_SIZE + StimuliManager.CHOICE_STIMULI_SIZE_MULTIPLIER * 2;
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
            ivResponse.setImageBitmap(StimuliManager.getStimuli(getActivity(), imageTag));
            LinearLayout.LayoutParams responseLayoutParams = new LinearLayout.LayoutParams(100, 100);
            responseLayoutParams.setMargins(RESPONSE_MARGINS, RESPONSE_MARGINS, RESPONSE_MARGINS, RESPONSE_MARGINS);
            ivResponse.setLayoutParams(responseLayoutParams);
            responseListLayout.addView(ivResponse);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFeedbackScreen() {
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        FragmentManager fm = getActivity().getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        LevelFeedback levelFeedback = new LevelFeedback();
        ft.replace(R.id.fragment_container, levelFeedback);
        ft.commit();
    }
}
