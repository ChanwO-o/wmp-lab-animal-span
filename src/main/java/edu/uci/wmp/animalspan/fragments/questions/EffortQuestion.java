package edu.uci.wmp.animalspan.fragments.questions;

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
    TextView tvQuestion;
    SeekBar seekBar;
    RelativeLayout rlSeekBarLabels;
    ImageView ivEffortFirst;
    ImageView ivEffortSecond;
    ImageView ivEffortThird;
    ImageView ivNext;
    View[] hiddenViews;

    final double IMAGE_WIDTH_PERCENTAGE = 0.35;
    final double IMAGE_HEIGHT_PERCENTAGE = 0.33;

    // second question setup
    final String SECONDQUESTION = "How hard did you try to do your best at the task?";
    final String[] SECONDQUESTIONLABELS = new String[] {"I did not try very hard", "", "I tried my best"};

    boolean responded;
    long responseStartTime;
    final int HIDE_TIME = 250; // hide all views
    private Handler handler = new Handler();

    private Runnable response = new Runnable() {
        @Override
        public void run() {
            long current = SystemClock.uptimeMillis() - responseStartTime;
            if (current < HIDE_TIME) {
                setViewsVisible(View.GONE);
                handler.postDelayed(this, 0);
            } else
                setViewsVisible(View.VISIBLE);
        }
    };

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

        tvQuestion = (TextView) view.findViewById(R.id.tvEffortQuestion);
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        rlSeekBarLabels = (RelativeLayout) view.findViewById(R.id.rlSeekBarLabels);
        ivNext = (ImageView) view.findViewById(R.id.ivEffortQuestionsDone);

        ivEffortFirst = (ImageView) view.findViewById(R.id.ivEffortFirst);
        ivEffortSecond = (ImageView) view.findViewById(R.id.ivEffortSecond);
        ivEffortThird = (ImageView) view.findViewById(R.id.ivEffortThird);

        // scale images
        int imageWidth = Double.valueOf(LevelManager.getInstance().screen_height * IMAGE_WIDTH_PERCENTAGE).intValue();
        int imageHeight = Double.valueOf(LevelManager.getInstance().screen_height * IMAGE_HEIGHT_PERCENTAGE).intValue();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(imageWidth, imageHeight);

        ivEffortFirst.setLayoutParams(layoutParams);
        ivEffortSecond.setLayoutParams(layoutParams);
        ivEffortThird.setLayoutParams(layoutParams);

        // add margins to labels
//        RelativeLayout.LayoutParams labelLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//        int labelMarginHeight = seekBar.getHeight() / 2;
//        Log.wtf("labelheight", "" + labelMarginHeight);
//        labelLayoutParams.setMargins(0, labelMarginHeight, 0, 0);
//        for (int i = 0; i < rlSeekBarLabels.getChildCount(); i++)
//            rlSeekBarLabels.getChildAt(i).setLayoutParams(labelLayoutParams);

        responded = false;
        hiddenViews = new View[7];
        fillHiddenViews();

        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!responded) {
                    responseStartTime = SystemClock.uptimeMillis();
                    responded = true;

                    CSVWriter.getInstance().collectQuestionResponse(tvQuestion.getText().toString(), seekBar.getProgress());
                    CSVWriter.getInstance().writeQuestionResponse();

                    if (questionNum == 1) {
                        handler.postDelayed(response, 0);
                        setUpNextQuestion();
                    }
                    else
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
        tvQuestion.setText(SECONDQUESTION);

        ivEffortFirst.setImageResource(R.drawable.tryhard1);
        ivEffortThird.setImageResource(R.drawable.tryhard2);

        for (int i = 0; i < SECONDQUESTIONLABELS.length; i++)
            ((TextView) rlSeekBarLabels.getChildAt(i)).setText(SECONDQUESTIONLABELS[i]);
        seekBar.setProgress(50);
        questionNum++;
        responded = false;
    }

    /**
     * Fills array with views to hide/show after each question
     */
    public void fillHiddenViews() {
        hiddenViews[0] = tvQuestion;
        hiddenViews[1] = ivEffortFirst;
        hiddenViews[2] = ivEffortSecond;
        hiddenViews[3] = ivEffortThird;
        hiddenViews[4] = rlSeekBarLabels;
        hiddenViews[5] = seekBar;
        hiddenViews[6] = ivNext;
    }

    /**
     * Hides/shows all views between questions
     */
    public void setViewsVisible(int visibility) {
        for (View v : hiddenViews)
            v.setVisibility(visibility);
        ivEffortSecond.setVisibility(View.INVISIBLE);
    }
}
