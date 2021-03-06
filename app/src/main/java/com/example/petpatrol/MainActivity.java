package com.example.petpatrol;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        AddAnimalFragment.ResetButtons {

    private Fragment lostFragment = new LostFragment();
    private Fragment foundFragment = new FoundFragment();
    private Fragment addAnimalFragment = new AddAnimalFragment();
    private FloatingActionButton addButton;
    private FloatingActionButton exitButton;
    private Button lostButton;
    private Button foundButton;
    private SearchFilterViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = ViewModelProviders.of(this).get(SearchFilterViewModel.class);
        setContentView(R.layout.main_view);
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        switchFragment(lostFragment);

        lostButton = findViewById(R.id.lostButton);
        foundButton = findViewById(R.id.foundButton);
        addButton = findViewById(R.id.addButton);
        exitButton = findViewById(R.id.floatingExitButton);

        initializeButtons();
    }

    private void initializeButtons() {
        lostButton.setVisibility(View.VISIBLE);
        foundButton.setVisibility(View.VISIBLE);
        addButton.show();
        exitButton.hide();
        lostButton.setOnClickListener(this);
        foundButton.setOnClickListener(this);
        addButton.setOnClickListener(this);
        exitButton.setOnClickListener(this);
    }

    private void switchFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragmentContainer, fragment);
        ft.commit();
    }

    private void addFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragmentContainer, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        Log.w("onClick", "Id: " + i);
        if (i == R.id.lostButton) {
            model.setFilter(this, null, null, null, null, null, null, null,
                    "lost");
            switchFragment(lostFragment);
            lostButton.setBackgroundColor(getResources().getColor(R.color.petpatrol_default));
            foundButton.setBackgroundColor(getResources().getColor(R.color.petpatrol_default_light));
        }
        if (i == R.id.foundButton) {
            model.setFilter(this, null, null, null, null, null, null, null,
                    "found");
            switchFragment(foundFragment);
            lostButton.setBackgroundColor(getResources().getColor(R.color.petpatrol_default_light));
            foundButton.setBackgroundColor(getResources().getColor(R.color.petpatrol_default));
        }
        if (i == R.id.menuSignOut) {
            Log.w("onClick", "Testing");
        }
        if (i == R.id.addButton) {
            lostButton.setVisibility(View.GONE);
            foundButton.setVisibility(View.GONE);

            Bundle arguments = new Bundle();

            if(lostFragment.isVisible()) {
                arguments.putString("role", "lost");
            } else {
                arguments.putString("role", "found");
            }

            addAnimalFragment.setArguments(arguments);
            addFragment(addAnimalFragment);
        }
        Log.d("MainActivity", "clicked: " + i);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuSignOut:
                Log.w("onClick", "Testing");
                Intent loginIntent = new Intent(this, LoginActivity.class);
                loginIntent.putExtra("action", "logout");
                startActivity(loginIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void resetButtons() {
        initializeButtons();
    }
}