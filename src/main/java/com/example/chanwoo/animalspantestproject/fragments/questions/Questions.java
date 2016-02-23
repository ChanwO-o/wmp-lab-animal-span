package com.example.chanwoo.animalspantestproject.fragments.questions;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.chanwoo.animalspantestproject.R;
import com.example.chanwoo.animalspantestproject.fragments.MainActivityFragment;

public class Questions extends Fragment {

    ImageView ivQuestionsDone;

    public Questions() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_questions, container, false);

        ivQuestionsDone = (ImageView) view.findViewById(R.id.ivQuestionsDone);
        ivQuestionsDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                MainActivityFragment mainActivityFragment = new MainActivityFragment();
                ft.replace(R.id.fragment_container, mainActivityFragment);
                ft.commit();
            }
        });


        return view;
    }
}
