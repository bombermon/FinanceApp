package com.veselovski.financeapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;
    private FirebaseAuth mAuth;
    private Button LoginWithEmailBtn;
    private DatabaseReference usersRef;
    private ProgressDialog loader;
    private FirebaseAuth.AuthStateListener authStateListener;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user!= null){
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        loader = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        LoginWithEmailBtn = findViewById(R.id.LoginWithEmail);

        createRequest();
        LoginWithEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void startLoader(){
        loader.setMessage("????????????????????, ??????????????????...");
        loader.setCanceledOnTouchOutside(false);
        loader.show();
    }



    private void createRequest() {

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1077147152829-o1tianc6f4m02lfmau4cmbmopcgkedud.apps.googleusercontent.com")
                .requestEmail()
                .build();


        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }



    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
        startLoader();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Failure"+e.getMessage(), Toast.LENGTH_SHORT).show();
                loader.dismiss();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                            loader.dismiss();
                            updateUI(user);
                        } else {
                            Toast.makeText(LoginActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                            loader.dismiss();
                        }
                    }
                });
    }

    private void updateUI(Object o) {

        GoogleSignInAccount googleSignInAccount  = GoogleSignIn.getLastSignedInAccount(LoginActivity.this);
        if (googleSignInAccount!=null){

            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            Calendar cal = Calendar.getInstance();
            String date = dateFormat.format(cal.getTime());

            Uri picUri = googleSignInAccount.getPhotoUrl();
            String profilepictureurl = picUri.toString();
            String name = googleSignInAccount.getDisplayName();
            String email = googleSignInAccount.getEmail();
            String id = mAuth.getCurrentUser().getUid();

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("name", name);
            hashMap.put("profilepictureurl", profilepictureurl);
            hashMap.put("email", email);
            hashMap.put("id", id);
            hashMap.put("logedinon", date);

            usersRef.child(id).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(LoginActivity.this, "???????????????? ????????", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }else {
                        Toast.makeText(LoginActivity.this, "Failed to save data "+ task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(authStateListener);
    }

}