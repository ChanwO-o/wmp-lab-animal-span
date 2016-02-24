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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.Toast;

import edu.uci.wmp.animalspan.LevelManager;
import com.uci.wmp.animalspan.R;

import java.lang.reflect.Field;

public class Settings extends Fragment {

    LinearLayout llSettingsWidgets;
    EditText etSubject, etSession;
    Switch swQuestions;
    Switch swTrainingMode;
    ImageView ivBack;
    NumberPicker picker;
    final String[] levelOptions = new String[] {"5", "10", "15", "20", "25", "30", "35", "40"};
    final String[] timeOptions = new String[] {"100", "150", "200", "250", "300"};


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
        ivBack = (ImageView) view.findViewById(R.id.ivSettingsBack);


        etSubject.setText(String.valueOf(LevelManager.getInstance().subject));
        etSession.setText(String.valueOf(LevelManager.getInstance().session));
        swQuestions.setTextOn("On");
        swQuestions.setTextOff("Off");
        swTrainingMode.setTextOn("Time");
        swTrainingMode.setTextOff("Levels");

        // set default mode of switches
        swQuestions.setChecked(LevelManager.getInstance().questions);
        boolean trainingModeIsTime = LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_TIME);
        swTrainingMode.setChecked(trainingModeIsTime); // set switch default on/off

        // setup numberpicker
        picker = new NumberPicker(getActivity());
        if (trainingModeIsTime) // set initial numberpicker mode
            setNumberPickerTime();
        else
            setNumberPickerLevels();
        picker.setScrollBarStyle(NumberPicker.SCROLLBARS_OUTSIDE_OVERLAY);
        setNumberPickerTextColor(picker, Color.WHITE);
        llSettingsWidgets.addView(picker);

        swTrainingMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    LevelManager.getInstance().trainingmode = LevelManager.TRAININGMODE_TIME;
                    setNumberPickerTime();
                } else {
                    LevelManager.getInstance().trainingmode = LevelManager.TRAININGMODE_LEVELS;
                    setNumberPickerLevels();
                }
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
                    if (LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_LEVELS))
                        LevelManager.getInstance().numberoftrials = Integer.valueOf(levelOptions[picker.getValue()]);
                    else if (LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_TIME))
                        LevelManager.getInstance().sessionLength = Integer.valueOf(timeOptions[picker.getValue()]);

                    LevelManager.getInstance().questions = swQuestions.isChecked(); // set questions

                    FragmentManager fm = getActivity().getFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    MainActivityFragment mainActivityFragment = new MainActivityFragment();
                    ft.replace(R.id.fragment_container, mainActivityFragment);
                    ft.commit();
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Invalid inputs: Subject & session numbers must be integers", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    public void setNumberPickerLevels() {
        picker.setDisplayedValues(null); // added this line to prevent indexoutofbounds error
        picker.setMaxValue(levelOptions.length - 1);
        picker.setMinValue(0);
        picker.setDisplayedValues(levelOptions);
        picker.setValue(findIndex(LevelManager.getInstance().numberoftrials, levelOptions));
    }

    public void setNumberPickerTime() {
        picker.setDisplayedValues(null);
        picker.setMaxValue(timeOptions.length - 1);
        picker.setMinValue(0);
        picker.setDisplayedValues(timeOptions);
        picker.setValue(findIndex(LevelManager.getInstance().sessionLength, timeOptions));
    }

    /**
     * Find index of option in option list, used for setting initial value of number picker
     */
    public int findIndex(int value, String[] options) {
        for (int i = 0; i < options.length; i++) {
            if (Integer.valueOf(options[i]) == value)
                return i;
        }
        return 0;
    }

    public static boolean setNumberPickerTextColor(NumberPicker numberPicker, int color)
    {
        final int count = numberPicker.getChildCount();
        for(int i = 0; i < count; i++){
            View child = numberPicker.getChildAt(i);
            if(child instanceof EditText){
                try{
                    Field selectorWheelPaintField = numberPicker.getClass()
                            .getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    ((Paint)selectorWheelPaintField.get(numberPicker)).setColor(color);
                    ((EditText)child).setTextColor(color);
                    numberPicker.invalidate();
                    return true;
                }
                catch(NoSuchFieldException e){
                    Log.w("setNPTextColor", e);
                }
                catch(IllegalAccessException e){
                    Log.w("setNPTextColor", e);
                }
                catch(IllegalArgumentException e){
                    Log.w("setNPTextColor", e);
                }
            }
        }
        return false;
    }
}
