package edu.uci.wmp.animalspan.fragments.questions;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.uci.wmp.animalspan.R;

import edu.uci.wmp.animalspan.Util;

public class EffortQuestion extends Fragment {

    int questionNum = 1;
    TextView question;
    SeekBar seekBar;
    TextView tvProgress;
    RelativeLayout rlSeekBarLabels;
    ImageView ivNext;

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
        tvProgress = (TextView) view.findViewById(R.id.tvProgress);
        rlSeekBarLabels = (RelativeLayout) view.findViewById(R.id.rlSeekBarLabels);
        ivNext = (ImageView) view.findViewById(R.id.ivEffortQuestionsDone);

        // initial progress text (50 / 100)
        String progressText = seekBar.getProgress() + "/" + seekBar.getMax();
        tvProgress.setText(progressText);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int seekbarProgress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekbarProgress = progress;
                String progressText = seekbarProgress + "/" + seekBar.getMax();
                tvProgress.setText(progressText);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (questionNum == 1)
                    setUpNextQuestion();
                else
                    Util.loadFragment(getActivity(), new ReflectionQuestion());
            }
        });

        return view;
    }

    /**
     * Setup for second effort question
     */
    public void setUpNextQuestion() {
        question.setText(SECONDQUESTION);
        for (int i = 0; i < SECONDQUESTIONLABELS.length; i++) {
            ((TextView) rlSeekBarLabels.getChildAt(i)).setText(SECONDQUESTIONLABELS[i]);
        }
        seekBar.setProgress(50);
        questionNum++;
    }
}
