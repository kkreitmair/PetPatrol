package com.example.petpatrol;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.example.petpatrol.R;

public class MainActivity extends AppCompatActivity {

    private LinearLayout inAppScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        exampleView();
    }

    private void exampleView() {
        setContentView(R.layout.main_view);
        inAppScrollView = findViewById(R.id.inAppScrollView);
        for (int index=0; index <=4; index++) {
            generateAdvert();
        }
    }

    private void generateAdvert() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View advert = inflater.inflate(R.layout.advert, null);
        inAppScrollView.addView(advert, inAppScrollView.getChildCount() - 1);
    }
}
