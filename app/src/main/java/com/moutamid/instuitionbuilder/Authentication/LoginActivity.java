package com.moutamid.instuitionbuilder.Authentication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.moutamid.instuitionbuilder.Admin.AdminPanel;
import com.moutamid.instuitionbuilder.Home.WalkThroughActivity;
import com.moutamid.instuitionbuilder.R;
import com.moutamid.instuitionbuilder.config.Config;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private RelativeLayout mButtonFacebook;
    private CallbackManager callbackManager;
    private LoginManager loginManager;
    private FirebaseAuth mAuth;
    private EditText emailEdt, passwordEdt;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    Button idBtnSubmitCourse;
    SignInButton btSignIn;
    GoogleSignInClient googleSignInClient;
    FirebaseAuth firebaseAuth;
    private boolean isPasswordVisible = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        checkApp(LoginActivity.this);
        btSignIn = findViewById(R.id.bt_sign_in);

        mButtonFacebook = findViewById(R.id.facebook_layout);
        FacebookSdk.sdkInitialize(LoginActivity.this);
        callbackManager = CallbackManager.Factory.create();
        facebookLogin();

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");

        emailEdt = findViewById(R.id.idEdtemail);
        passwordEdt = findViewById(R.id.idEdtpassword);
        idBtnSubmitCourse = findViewById(R.id.idBtnSubmitCourse);
updatePasswordVisibilityDrawable();
        passwordEdt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (passwordEdt.getRight() - passwordEdt.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // Clicked on the drawable at the end
                        togglePasswordVisibility();
                        return true;
                    }
                }
                return false;
            }
        });


        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder
                (GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("10469575736-uglg2p8a7sns11qkbfdc7j7gsrhg4rl5.apps.googleusercontent.com")
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(LoginActivity.this
                , googleSignInOptions);
        btSignIn.setOnClickListener(view -> {
            Intent intent = googleSignInClient.getSignInIntent();
            startActivityForResult(intent, 100);
        });
        firebaseAuth = FirebaseAuth.getInstance();

        idBtnSubmitCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email_str = emailEdt.getText().toString();
                String password_str = passwordEdt.getText().toString();
                if (TextUtils.isEmpty(email_str)) {
                    emailEdt.setError("Please enter Email");
                } else if (TextUtils.isEmpty(password_str)) {
                    passwordEdt.setError("Please enter Password");
                } else {
                    login(email_str, password_str);
                }
            }
        });

        mButtonFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginManager.logInWithReadPermissions(LoginActivity.this, Arrays.asList("email", "public_profile", "user_birthday"));
            }
        });
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        updatePasswordVisibilityDrawable();

        if (isPasswordVisible) {
            // Show password

            passwordEdt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            // Hide password
            passwordEdt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }

        // Move the cursor to the end of the text to avoid issues with the transformation
        passwordEdt.setSelection(passwordEdt.getText().length());
    }

    private void updatePasswordVisibilityDrawable() {
        int drawableResId = isPasswordVisible ?
                R.drawable.show_password :
                R.drawable.hide;

        Drawable drawable = ContextCompat.getDrawable(this, drawableResId);

        // Set the right drawable
        passwordEdt.setCompoundDrawablesWithIntrinsicBounds(
                null, null, drawable, null);
    }
    private void login(String email, String password) {
        if (email.equals("admin@gmail.com")) {
            startActivity(new Intent(LoginActivity.this, AdminPanel.class));
            finishAffinity();
        } else {
            Config.showProgressDialog(LoginActivity.this);
            mAuth.signInWithEmailAndPassword(email, password).
                    addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                if (Stash.getString("name").isEmpty()) {
                                    startActivity(new Intent(LoginActivity.this, UserDetailsActivity.class));
                                    finishAffinity();
                                }
                                else {
                                startActivity(new Intent(LoginActivity.this, WalkThroughActivity.class));
                                finishAffinity();
                                }


                            } else {
                                Config.dismissProgressDialog();
                                Toast.makeText(LoginActivity.this, "Authentication failed.",
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

    public void login(View view) {
        startActivity(new Intent(LoginActivity.this, EnterPasswordActivity.class));
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

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//
//        super.onActivityResult(requestCode, resultCode, data);
//    }

    public void register(View view) {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));

    }

    public static void checkApp(Activity activity) {
        String appName = "IntiutionBuilder";

        new Thread(() -> {
            URL google = null;
            try {
                google = new URL("https://raw.githubusercontent.com/Moutamid/Moutamid/main/apps.txt");
            } catch (final MalformedURLException e) {
                e.printStackTrace();
            }
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(google != null ? google.openStream() : null));
            } catch (final IOException e) {
                e.printStackTrace();
            }
            String input = null;
            StringBuffer stringBuffer = new StringBuffer();
            while (true) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        if ((input = in != null ? in.readLine() : null) == null) break;
                    }
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                stringBuffer.append(input);
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
            String htmlData = stringBuffer.toString();

            try {
                JSONObject myAppObject = new JSONObject(htmlData).getJSONObject(appName);

                boolean value = myAppObject.getBoolean("value");
                String msg = myAppObject.getString("msg");

                if (value) {
                    activity.runOnUiThread(() -> {
                        new AlertDialog.Builder(activity).setMessage(msg).setCancelable(false).show();
                    });
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
// Check condition


        if (requestCode == 100) {

// When request code is equal to 100
// Initialize task
            Task<GoogleSignInAccount> signInAccountTask = GoogleSignIn
                    .getSignedInAccountFromIntent(data);
// check condition
            if (signInAccountTask.isSuccessful()) {
// When google sign in successful
// Initialize string
                String s = "Google sign in successful";
// Display Toast
                displayToast(s);
// Initialize sign in account
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
                                        if (Stash.getString("name").isEmpty()) {
                                            Stash.put("name", displayName);
                                            startActivity(new Intent(LoginActivity.this, UserDetailsActivity.class));
                                            finishAffinity();
                                        } else {
                                            startActivity(new Intent(LoginActivity.this, WalkThroughActivity.class));
                                            finishAffinity();
                                        }
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