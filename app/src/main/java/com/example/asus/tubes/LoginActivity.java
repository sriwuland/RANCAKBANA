package com.example.asus.tubes;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends AppCompatActivity {
    private int rcSignIn = 9001;
    private GoogleSignInOptions gso;
    private FacebookAuthCredential fac;
    private GoogleApiClient gac;
    private FirebaseAuth firebaseAuth;
    private CallbackManager callbackFacebook;
    private LoginButton lgn_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_login);
        lgn_button = findViewById(R.id.lgn_facebook);
        initAuth();
    }

    private void initAuth() {
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();

        gac = new GoogleApiClient.Builder(this).enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener()
        {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                Toast.makeText(LoginActivity.this, "Connection failed!", Toast.LENGTH_SHORT).show();
            }
        }).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();

        callbackFacebook = CallbackManager.Factory.create();

        lgn_button.setReadPermissions("email", "public_profile");
        lgn_button.registerCallback(callbackFacebook, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                facebookAuthentication(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "Canceling!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Log.w("Facebook", error.toString());
            }
        });

        firebaseAuth  = FirebaseAuth.getInstance();
    }

    private void facebookAuthentication(AccessToken accessToken) {
        firebaseAuth.signInWithCredential(FacebookAuthProvider.getCredential(accessToken.getToken())).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, "Login failed!!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackFacebook.onActivityResult(requestCode, resultCode, data);
        if (requestCode==rcSignIn){
            if (Auth.GoogleSignInApi.getSignInResultFromIntent(data).isSuccess()){
                gac.clearDefaultAccountAndReconnect();
                googleAuthentication(Auth.GoogleSignInApi.getSignInResultFromIntent(data).getSignInAccount());
            }
        }
    }

    private void googleAuthentication(GoogleSignInAccount signInAccount) {
        firebaseAuth.signInWithCredential(GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null)).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, "Login failed!!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void logGoogle(View view) {
        startActivityForResult(Auth.GoogleSignInApi.getSignInIntent(gac), rcSignIn);
    }


    public void logFacebook(View view) {
        lgn_button.performClick();
    }
}
