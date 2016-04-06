package edu.uci.wmp.animalspan.fragments.questions;

import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.uci.wmp.animalspan.R;

import java.io.IOException;

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
        return view;
    }

    public void fillFaces() {
        try {
            for (int i = 1; i <= 5; i++)
                llFaces.addView(createFace(StimuliManager.FACE_LABEL + i));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public ImageView createFace(int labeledFilename) throws IOException {
        Log.wtf("lf", "" + labeledFilename);
        final ImageView ivFace = new ImageView(getActivity());
        ivFace.setImageBitmap(StimuliManager.getStimuli(getActivity(), labeledFilename));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(FACESIZE, FACESIZE);
        int margin = (int) LevelManager.getInstance().gapbetweenimages * 5; // margin between each image in grid
        layoutParams.setMargins(margin, margin, margin, margin);
        ivFace.setLayoutParams(layoutParams);
        ivFace.setTag(labeledFilename); // set tag for click listener

        ivFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                recordResponse((int) ivFace.getTag());
                if (questionNum == 1) { // setup for second reflection question
                    question.setText(SECONDQUESTION);
                    questionNum++;
                }
                else
                    Util.loadFragment(getActivity(), new MainActivityFragment());
            }
        });
        return ivFace;
    }
}
