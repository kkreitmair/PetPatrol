package com.example.petpatrol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PetPatrol";
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth firebase;
    private GoogleSignInClient authClient;
    private TextView userInfo;
    private TextView userInfoDetail;
    private ProgressDialog progressDialog;

    private LinearLayout inAppScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        initializeUserInfo();
        activateOnClickListeners();
        initializeGoogleAuth();
    }

    private void initializeUserInfo() {
        userInfo = findViewById(R.id.status);
        userInfoDetail = findViewById(R.id.detail);
    }

    private void activateOnClickListeners() {
        findViewById(R.id.signInButton).setOnClickListener(this);
        findViewById(R.id.signOutButton).setOnClickListener(this);
        findViewById(R.id.disconnectButton).setOnClickListener(this);
    }

    private void initializeGoogleAuth() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(
                getString(R.string.default_web_client_id)
        )
                .requestEmail()
                .build();

        authClient = GoogleSignIn.getClient(this, gso);
        firebase = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebase.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
                updateUI(null);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        showProgressDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebase.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = firebase.getCurrentUser();
                            updateUI(user);
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.main_layout),
                                    "Authentication Failed.",
                                    Snackbar.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                        hideProgressDialog();
                    }
                });
    }

    private void showProgressDialog(){
        if(progressDialog == null ) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setIndeterminate(true);
        }
        progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            userInfo.setText(getString(R.string.google_status_fmt, user.getEmail()));
            userInfoDetail.setText(getString(R.string.firebase_status_fmt, user.getUid()));

            exampleView();
        } else {
            userInfo.setText(R.string.signed_out);
            userInfoDetail.setText(null);

            setButtonVisibility(View.VISIBLE, View.GONE);
        }
    }

    private void exampleView() {
        setButtonVisibility(View.GONE, View.VISIBLE);
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

    private void setButtonVisibility(int signIn, int signOutandDisconnect) {
        findViewById(R.id.signInButton).setVisibility(signIn);
        findViewById(R.id.signOutButton).setVisibility(signOutandDisconnect);
        findViewById(R.id.disconnectButton).setVisibility(signOutandDisconnect);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.signInButton) {
            signIn();
        } else if (i == R.id.signOutButton) {
            signOut();
        } else if (i == R.id.disconnectButton) {
            revokeAccess();
        }
    }

    private void signIn() {
        Intent signInIntent = authClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        firebase.signOut();
        authClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }

    private void revokeAccess() {
        firebase.signOut();
        authClient.revokeAccess().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }
}