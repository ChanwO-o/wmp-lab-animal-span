package edu.uci.wmp.animalspan.fragments.questions;

import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.os.SystemClock;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.uci.wmp.animalspan.R;

import java.io.IOException;

import edu.uci.wmp.animalspan.CSVWriter;
import edu.uci.wmp.animalspan.LevelManager;
import edu.uci.wmp.animalspan.StimuliManager;
import edu.uci.wmp.animalspan.Util;
import edu.uci.wmp.animalspan.fragments.MainActivityFragment;

public class ReflectionQuestion extends Fragment {

    int questionNum = 1;
    TextView question;
    LinearLayout llFaces;

    final int FACESIZE = 150;
    final String SECONDQUESTION = "I think I did pretty well on this task.";
    long SHOWGREEN = 1000; // face turns green for 1 second

    boolean responded;
    long responseStartTime;
    int faceClickedID;
    private Handler handler = new Handler();

    private Runnable response = new Runnable() {
        @Override
        public void run() {
            if (SystemClock.uptimeMillis() - responseStartTime > SHOWGREEN) {
                if (questionNum == 1) // setup for second reflection question
                    setUpNextQuestion();
                else
                    Util.loadFragment(getActivity(), new EffortQuestion());
            } else {
                handler.postDelayed(this, 0);
            }
        }
    };

    public ReflectionQuestion() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reflection_question, container, false);

        question = (TextView) view.findViewById(R.id.tvReflQuestion);
        llFaces = (LinearLayout) view.findViewById(R.id.llFaces);

        fillFaces();
        responded = false;
        return view;
    }

    public void fillFaces() {
        for (int i = 1; i <= 5; i++)
            llFaces.addView(createFace(StimuliManager.FACE_LABEL + i));
    }

    public ImageView createFace(final int labeledFilename) {
        final ImageView ivFace = new ImageView(getActivity());
        setFace(ivFace, labeledFilename);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(FACESIZE, FACESIZE);
        int margin = (int) LevelManager.getInstance().gapbetweenimages * 5; // margin between each image in grid
        layoutParams.setMargins(margin, margin, margin, margin);
        ivFace.setLayoutParams(layoutParams);
        ivFace.setTag(labeledFilename); // set tag for click listener

        ivFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!responded) {
                    // set face green
                    int greenLabel = labeledFilename + 10 * labeledFilename % 100;
                    Log.wtf("gl", "" + greenLabel);
                    setFace((ImageView) v, greenLabel);

                    // log to csv
                    CSVWriter.getInstance().collectQuestionResponse((int) v.getTag() % 100);

                    responded = true;
                    faceClickedID = (int) v.getTag() - StimuliManager.FACE_LABEL - 1;
                    responseStartTime = SystemClock.uptimeMillis();
                    handler.postDelayed(response, 0);
                }
            }
        });
        return ivFace;
    }

    private void setFace(ImageView ivFace, int labeledFilename) {
        try {
            ivFace.setImageBitmap(StimuliManager.getStimuli(getActivity(), labeledFilename));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Setup for second reflection question
     */
    public void setUpNextQuestion() {
        // restore face color
        ImageView face = (ImageView) llFaces.getChildAt(faceClickedID);
        setFace(face, (int) face.getTag());

        question.setText(SECONDQUESTION);
        questionNum++;
        responded = false;
    }
}
