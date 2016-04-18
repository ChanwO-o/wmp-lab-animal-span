package edu.uci.wmp.animalspan.fragments.questions;

import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.uci.wmp.animalspan.R;

import edu.uci.wmp.animalspan.CSVWriter;
import edu.uci.wmp.animalspan.LevelManager;
import edu.uci.wmp.animalspan.Util;
import edu.uci.wmp.animalspan.fragments.MainActivityFragment;

public class EffortQuestion extends Fragment {

    int questionNum = 1;
    TextView question;
    SeekBar seekBar;
    RelativeLayout rlSeekBarLabels;
    ImageView ivEffortFirst;
    ImageView ivEffortSecond;
    ImageView ivEffortThird;
    ImageView ivNext;

    final double IMAGE_WIDTH_PERCENTAGE = 0.25;
    final double IMAGE_HEIGHT_PERCENTAGE = 0.33;

    // second question setup
    final String SECONDQUESTION = "How hard did you try to do your best at the task?";
    final String[] SECONDQUESTIONLABELS = new String[] {"I did not try very hard", "", "I tried my best"};

    public EffortQuestion() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_effort_question, container, false);

        question = (TextView) view.findViewById(R.id.tvEffortQuestion);
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        rlSeekBarLabels = (RelativeLayout) view.findViewById(R.id.rlSeekBarLabels);
        ivNext = (ImageView) view.findViewById(R.id.ivEffortQuestionsDone);

        ivEffortFirst = (ImageView) view.findViewById(R.id.ivEffortFirst);
        ivEffortSecond = (ImageView) view.findViewById(R.id.ivEffortSecond);
        ivEffortThird = (ImageView) view.findViewById(R.id.ivEffortThird);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int seekbarProgress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekbarProgress = progress;
                String progressText = seekbarProgress + "/" + seekBar.getMax();
//                tvProgress.setText(progressText);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // scale images
        int imageWidth = Double.valueOf(LevelManager.getInstance().screen_height * IMAGE_WIDTH_PERCENTAGE).intValue();
        int imageHeight = Double.valueOf(LevelManager.getInstance().screen_height * IMAGE_HEIGHT_PERCENTAGE).intValue();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(imageWidth, imageHeight);

        ivEffortFirst.setLayoutParams(layoutParams);
        ivEffortSecond.setLayoutParams(layoutParams);
        ivEffortThird.setLayoutParams(layoutParams);

        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CSVWriter.getInstance().collectQuestionResponse(seekBar.getProgress());
                if (questionNum == 1)
                    setUpNextQuestion();
                else {
                    // finished all questions, write from buffer to csv file
                    CSVWriter.getInstance().writeQuestionResponses();
                    Util.loadFragment(getActivity(), new MainActivityFragment());
                }

            }
        });

        return view;
    }

    /**
     * Setup for second effort question
     */
    public void setUpNextQuestion() {
        question.setText(SECONDQUESTION);

        ivEffortFirst.setImageResource(R.drawable.tryhard1);
        ivEffortSecond.setVisibility(View.GONE);
        ivEffortThird.setImageResource(R.drawable.tryhard2);

        for (int i = 0; i < SECONDQUESTIONLABELS.length; i++) {
            ((TextView) rlSeekBarLabels.getChildAt(i)).setText(SECONDQUESTIONLABELS[i]);
        }
        seekBar.setProgress(50);
        questionNum++;
    }
}
