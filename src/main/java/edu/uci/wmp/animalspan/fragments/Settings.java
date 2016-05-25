package edu.uci.wmp.animalspan.fragments;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import edu.uci.wmp.animalspan.Checks;
import edu.uci.wmp.animalspan.LevelManager;
import edu.uci.wmp.animalspan.Util;

import com.uci.wmp.animalspan.R;

import java.lang.reflect.Field;

public class Settings extends Fragment {

    LinearLayout llSettingsWidgets;
    EditText etSubject, etSession, etRoundsTime;
    Switch swQuestions;
    Switch swTrainingMode;
    TextView tvRTPrompt, tvRTUnit;
    Button bPerformChecks;
    ImageView ivBack;

    public Settings() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        llSettingsWidgets = (LinearLayout) view.findViewById(R.id.llSettingsWidgets);
        etSubject = (EditText) view.findViewById(R.id.etSubject);
        etSession = (EditText) view.findViewById(R.id.etSession);
        swQuestions = (Switch) view.findViewById(R.id.swQuestions);
        swTrainingMode = (Switch) view.findViewById(R.id.swTrainingMode);
        tvRTPrompt = (TextView) view.findViewById(R.id.tvRTPrompt);
        etRoundsTime = (EditText) view.findViewById(R.id.etRoundsTime);
        tvRTUnit = (TextView) view.findViewById(R.id.tvRTUnit);
        bPerformChecks = (Button) view.findViewById(R.id.bPerformChecks);
        ivBack = (ImageView) view.findViewById(R.id.ivSettingsBack);

        // initial setup
        swQuestions.setTextOn("On");
        swQuestions.setTextOff("Off");
        swTrainingMode.setTextOn("Time");
        swTrainingMode.setTextOff("Rounds");
        etSubject.setText(String.valueOf(LevelManager.getInstance().subject));
        etSession.setText(String.valueOf(LevelManager.getInstance().session));

        // set default mode of switches
        swQuestions.setChecked(LevelManager.getInstance().questions);
        boolean trainingModeIsTime = LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_TIME);
        swTrainingMode.setChecked(trainingModeIsTime); // set switch default on/off

        // setup RoundsTime input
        if (trainingModeIsTime)
            setRTLayoutTime();
        else
            setRTLayoutLevels();

        swTrainingMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    LevelManager.getInstance().trainingmode = LevelManager.TRAININGMODE_TIME;
                    setRTLayoutTime();
                } else {
                    LevelManager.getInstance().trainingmode = LevelManager.TRAININGMODE_ROUNDS;
                    setRTLayoutLevels();
                }
            }
        });

        bPerformChecks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Checks.getInstance().runAllChecks();
            }
        });

        /**
         * Check valid inputs of subject and session numbers
         * Update these values in LevelManager, then return back to main screen
         */
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // read inputs
                    int subject = Integer.valueOf(etSubject.getText().toString());
                    int session = Integer.valueOf(etSession.getText().toString());
                    LevelManager.getInstance().subject = subject;
                    LevelManager.getInstance().session = session;

                    // set trainingmode values
                    // TODO: check for input == 0, should not proceed
                    if (LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_ROUNDS))
                        LevelManager.getInstance().numberoftrials = Integer.valueOf(etRoundsTime.getText().toString());
                    else if (LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_TIME))
                        LevelManager.getInstance().sessionLength = Integer.valueOf(etRoundsTime.getText().toString());

                    LevelManager.getInstance().questions = swQuestions.isChecked(); // set questions

                    Util.loadFragment(getActivity(), new MainActivityFragment());
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Invalid inputs: Subject & session numbers must be integers", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    public void setRTLayoutLevels() {
        tvRTPrompt.setText("Rounds:");
        etRoundsTime.setText(String.valueOf(LevelManager.getInstance().numberoftrials));
        tvRTUnit.setText("");
    }

    public void setRTLayoutTime() {
        tvRTPrompt.setText("Time:");
        etRoundsTime.setText(String.valueOf(LevelManager.getInstance().sessionLength));
        tvRTUnit.setText("(s)");
    }
}
