package com.example.traveling;

import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class PostFragment extends Fragment {

    //views
    ImageView ivPostIm;
    EditText etDescription, etTag, etAdress, etGroup;
    ImageButton ibVoiceDesc, ibAiTag;
    TextView tvAddTag;
    ChipGroup chipGroupTags;
    CheckBox cbPublic, cbPrivate;
    AppCompatButton btnPost;
    View view;

    //State
    Uri selectedImageUri = null;
    List<String> tags = new ArrayList<>();

    //Firebase
    FirebaseAuth mAuth;
    FirebaseFirestore db;


    public PostFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    private void init() {

    }
    private void setListeners() {

    }
    private void handleFilter(String filter) {
        //TODO
    }
}