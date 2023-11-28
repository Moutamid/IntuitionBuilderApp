package com.moutamid.instuitionbuilder.Home;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.fxn.stash.Stash;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.moutamid.instuitionbuilder.Admin.AdminPanel;
import com.moutamid.instuitionbuilder.Authentication.EnterPasswordActivity;
import com.moutamid.instuitionbuilder.Authentication.LoginActivity;
import com.moutamid.instuitionbuilder.Authentication.UserDetailsActivity;
import com.moutamid.instuitionbuilder.R;
import com.moutamid.instuitionbuilder.config.Config;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Objects;

public class IntroActivity extends AppCompatActivity {

    private BottomSheetBehavior<ConstraintLayout> bottomSheetBehavior;
    VideoView videoView;
    Button buttonNext;
    TextView register, login;
    EditText email, idEdtpassword, idEdtemail;
    private CallbackManager callbackManager;
    private LoginManager loginManager;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    SignInButton btSignIn;
    GoogleSignInClient googleSignInClient;
    FirebaseAuth firebaseAuth;
    FirebaseAuth mAuth= FirebaseAuth.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dummy);
        buttonNext = findViewById(R.id.buttonNext);
        videoView = findViewById(R.id.videoView);
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.trainging_video); // Replace with your video resource ID
        videoView.setVideoURI(videoUri);
        videoView.start();
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Stash.put("video_seen", "yes");
                startActivity(new Intent(IntroActivity.this, LoginActivity.class));
                finish();
            }
        });
        showBottomSheetDialog();
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheetDialog();
            }
        });
    }
    private void showBottomSheetDialog() {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet);
        LinearLayout login_layout = bottomSheetDialog.findViewById(R.id.login_layout);
        LinearLayout register_layout = bottomSheetDialog.findViewById(R.id.register_layout);
        register = bottomSheetDialog.findViewById(R.id.register);
        login = bottomSheetDialog.findViewById(R.id.login);
        email = bottomSheetDialog.findViewById(R.id.email);
        idEdtemail = bottomSheetDialog.findViewById(R.id.idEdtemail);
        idEdtpassword = bottomSheetDialog.findViewById(R.id.idEdtpassword);
        Button buttonOnBoardingAction = bottomSheetDialog.findViewById(R.id.buttonOnBoardingAction);
        Button idBtnSubmitCourse = bottomSheetDialog.findViewById(R.id.idBtnSubmitCourse);
        ///
        btSignIn = bottomSheetDialog.findViewById(R.id.bt_sign_in);

        ImageView mButtonFacebook = bottomSheetDialog.findViewById(R.id.buttonFacebook);
        FacebookSdk.sdkInitialize(IntroActivity.this);
        callbackManager = CallbackManager.Factory.create();
        facebookLogin();
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken("890062472575-05ue90j9694eg6eorj1lhjoknkj3oqja.apps.googleusercontent.com")
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(IntroActivity.this
                , googleSignInOptions);
        btSignIn.setOnClickListener(view -> {
            Intent intent = googleSignInClient.getSignInIntent();
            startActivityForResult(intent, 100);
        });
        firebaseAuth = FirebaseAuth.getInstance();
        mButtonFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginManager.logInWithReadPermissions(IntroActivity.this, Arrays.asList("email", "public_profile", "user_birthday"));
            }
        });




        //
        idBtnSubmitCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email_str = idEdtemail.getText().toString();
                String password_str = idEdtpassword.getText().toString();
                if (TextUtils.isEmpty(email_str)) {
                    idEdtemail.setError("Please enter Email");
                } else if (TextUtils.isEmpty(password_str)) {
                    idEdtpassword.setError("Please enter Password");
                } else {
                    login(email_str, password_str);
                }
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register_layout.setVisibility(View.VISIBLE);
                login_layout.setVisibility(View.GONE);
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register_layout.setVisibility(View.GONE);
                login_layout.setVisibility(View.VISIBLE);
            }
        });
        buttonOnBoardingAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                create_Account();
            }
        });

//        LinearLayout download = bottomSheetDialog.findViewById(R.id.download);
//        LinearLayout delete = bottomSheetDialog.findViewById(R.id.delete);

        bottomSheetDialog.show();
    }

    public void create_Account() {
        if (email.getText().toString().isEmpty()) {
            email.setError("Please Enter");
        } else {
            Intent intent = new Intent(IntroActivity.this, EnterPasswordActivity.class);
            intent.putExtra("email", email.getText().toString());
            startActivity(intent);
        }
    }
    private void login(String email, String password) {

        if (email.equals("admin@gmail.com")) {
            startActivity(new Intent(IntroActivity.this, AdminPanel.class));
            finishAffinity();
        } else {
            Config.showProgressDialog(IntroActivity.this);
            mAuth.signInWithEmailAndPassword(email, password).
                    addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                if (Stash.getString("name").isEmpty()) {
                                    startActivity(new Intent(IntroActivity.this, UserDetailsActivity.class));
                                    finishAffinity();
                                }
                                else {
                                    startActivity(new Intent(IntroActivity.this, WalkThroughActivity.class));
                                    finishAffinity();
                                }


                            } else {
                                Config.dismissProgressDialog();
                                Toast.makeText(IntroActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Config.dismissProgressDialog();

                        }
                    });
        }
    }

    public void facebookLogin() {

        loginManager = LoginManager.getInstance();
        callbackManager = CallbackManager.Factory.create();

        loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(

                        loginResult.getAccessToken(),

                        new GraphRequest.GraphJSONObjectCallback() {

                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {

                                if (object != null) {
                                    try {
                                        String name = object.getString("name");
                                        String email = object.getString("email");
                                        String fbUserID = object.getString("id");
                                        Log.d("detais", name + "  " + email + "  " + fbUserID);

                                    } catch (JSONException | NullPointerException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, name, email, gender, birthday");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.v("LoginScreen", "---onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                // here write code when get error
                Log.v("LoginScreen", "----onError: " + error.getMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
// Check condition


        if (requestCode == 100) {

            Task<GoogleSignInAccount> signInAccountTask = GoogleSignIn
                    .getSignedInAccountFromIntent(data);
            if (signInAccountTask.isSuccessful()) {
                String s = "Google sign in successful";
                displayToast(s);
                try {

                    // Initialize sign in account
                    GoogleSignInAccount googleSignInAccount = signInAccountTask
                            .getResult(ApiException.class);
// Check condition
                    if (googleSignInAccount != null) {
// When sign in account is not equal to null
// Initialize auth credential
                        AuthCredential authCredential = GoogleAuthProvider
                                .getCredential(googleSignInAccount.getIdToken()
                                        , null);
// Check credential
                        firebaseAuth.signInWithCredential(authCredential)
                                .addOnCompleteListener(this, task -> {
// Check condition
                                    if (task.isSuccessful()) {
                                        String displayName = signInAccountTask.getResult().getDisplayName();
                                        displayToast(displayName + " ");
                                        startActivity(new Intent(IntroActivity.this, UserDetailsActivity.class));

                                    } else {
// When task is unsuccessful
// Display Toast
                                        displayToast("Authentication Failed :" + Objects.requireNonNull(task.getException())
                                                .getMessage());
                                    }
                                });

                    }
                } catch (ApiException e) {
                    displayToast(e.getMessage() + "");
                    e.printStackTrace();
                }
            } else {
                displayToast("" + signInAccountTask.getException());
            }
        }
    }


    private void displayToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    public void google(View view) {
        Intent intent = googleSignInClient.getSignInIntent();
        startActivityForResult(intent, 100);
    }
}
