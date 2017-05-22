package com.example.amrut.easychatapplication;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,GoogleApiClient.OnConnectionFailedListener {

    private SignInButton googleSignInBtn;
    private Button emailSignIn;
    private EditText userEmailText,userPasswordText;
    private TextView signUpLink;
    private LoginButton fbLoginButton;
    CallbackManager mCallbackManager;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    GoogleApiClient mGoogleApiClient;
    private DatabaseReference mDatabase;
    final static private int RC_SIGN_IN=9001;
    FirebaseUser user;
    String first_name,last_name,gender,profilePicUrl,fb_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        googleSignInBtn= (SignInButton) findViewById(R.id.sign_in_button);
        emailSignIn= (Button) findViewById(R.id.buttonSignIn);
        userEmailText= (EditText) findViewById(R.id.editTextEmail);
        userPasswordText= (EditText) findViewById(R.id.editTextPassword);
        signUpLink= (TextView) findViewById(R.id.textViewSignUpLink);
        fbLoginButton = (LoginButton) findViewById(R.id.login_button);
        mDatabase= FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("demo", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("demo", "onAuthStateChanged:signed_out");
                }
            }
        };

        if (mAuth.getCurrentUser()!=null){
            Intent intent=new Intent(MainActivity.this,UserPageActivity.class);
            startActivity(intent);
            finish();
        }

        mCallbackManager = CallbackManager.Factory.create();
        fbLoginButton.setReadPermissions("email", "public_profile");
        fbLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("demo", "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d("demo", "facebook:onCancel");
                Toast.makeText(getApplicationContext(),"Cancelled",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("demo", "facebook:onError", error);
                Toast.makeText(getApplicationContext(),"facebook login:error occrued",Toast.LENGTH_SHORT).show();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("827209507255-thhd3ke4u97orb60ngnqqt7u5n9377na.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* MainActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        googleSignInBtn.setOnClickListener(this);
        emailSignIn.setOnClickListener(this);
        signUpLink.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.sign_in_button){
            googleSignIn();
        }else if (v.getId()==R.id.buttonSignIn){
            signIn(userEmailText.getText().toString(),userPasswordText.getText().toString());
        }else if (v.getId()==R.id.textViewSignUpLink){
            Intent intent=new Intent(MainActivity.this,SignUpActivity.class);
            startActivity(intent);
            finish();
        }

    }

    public void signIn(String email,String password){
        if (!validateForm()){
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("demo", "signInWithEmail:onComplete:" + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            Log.w("demo", "signInWithEmail:failed", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication Failed",
                                    Toast.LENGTH_SHORT).show();
                        }else {
                            //go to UserPage activity
                            Intent intent=new Intent(MainActivity.this,UserPageActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });

    }

    private void googleSignIn(){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public boolean validateForm(){
        boolean valid=true;
        if (userEmailText.getText().toString().isEmpty()){
            userEmailText.setError("Required");
            valid=false;
        } else{
            userEmailText.setError(null);
        }

        if (userPasswordText.getText().toString().isEmpty()){
            userPasswordText.setError("Required");
            valid=false;
        } else{
            userPasswordText.setError(null);
        }
        return valid;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleSignInResult(GoogleSignInResult result){
        Log.d("demo","hadnleSignInResult:"+ result.isSuccess());
        if (result.isSuccess()){
            GoogleSignInAccount acct= result.getSignInAccount();
            firebaseAuthWithGoogle(acct);
        }else {

        }
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("demo", "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("demo", "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w("demo", "signInWithCredential", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(MainActivity.this, "Authentication success.",
                                    Toast.LENGTH_SHORT).show();
                            GraphRequest request = GraphRequest.newMeRequest(
                                    AccessToken.getCurrentAccessToken() ,
                                    new GraphRequest.GraphJSONObjectCallback() {
                                        @Override
                                        public void onCompleted(
                                                JSONObject object,
                                                GraphResponse response) {
                                            try {
                                                Log.d("demo","fbjsonObj"+object.toString());
                                                fb_id=object.getString("id");
                                                first_name= (String) object.getString("first_name");
                                                last_name=object.getString("last_name");
                                                gender=object.getString("gender");
                                                profilePicUrl=object.getJSONObject("picture").getJSONObject("data").getString("url");
                                                Log.d("demo","fb user details"+first_name+last_name+gender+profilePicUrl);
                                                //create new User and save in firebase DB
                                                User newUser=new User();
                                                newUser.setUid(user.getUid());
                                                newUser.setFirstName(first_name);
                                                newUser.setLastName(last_name);
                                                newUser.setGender(gender);
                                                newUser.setUserPicUrl(profilePicUrl);
                                                mDatabase.child("users").child(user.getUid()).child("profile").setValue(newUser);
                                                first_name="";
                                                last_name="";
                                                gender="";
                                                profilePicUrl="";
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                            Bundle parameters = new Bundle();
                            parameters.putString("fields","id,name,email,about,cover,birthday,first_name,gender,last_name,picture");
                            request.setParameters(parameters);
                            request.executeAsync();

                            Intent intent=new Intent(MainActivity.this,UserPageActivity.class);
                            startActivity(intent);
                            finish();
                        }

                        // ...
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("demo","ONConnectionFAiled:"+connectionResult);
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d("demo", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("demo", "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w("demo", "signInWithCredential", task.getException());
                            Toast.makeText(MainActivity.this, "Google Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }else {
                            mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(acct.getId())){
                                        Log.d("demo","user exits");
                                        //go to User Page activity
                                        Intent intent=new Intent(MainActivity.this,UserPageActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }else {

                                        User newUser= new User();
                                        Log.d("demo","FLname"+acct.getFamilyName());
                                        newUser.setUid(user.getUid());
                                        newUser.setFirstName(acct.getGivenName());
                                        newUser.setLastName(acct.getFamilyName());
                                        newUser.setGender(null);
                                        newUser.setUserPicUrl(acct.getPhotoUrl().toString());
                                        mDatabase.child("users").child(user.getUid()).child("profile").setValue(newUser);
                                        Toast.makeText(getApplicationContext(),"Google User Signed In",Toast.LENGTH_SHORT).show();
                                        Intent intent=new Intent(MainActivity.this,UserPageActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                });
    }
}
