package com.example.hp.thetacab.Activities;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A placeholder fragment containing a simple view.
 */
import com.example.hp.thetacab.FireBaseEntry;
import com.example.hp.thetacab.R;
import com.example.hp.thetacab.tApplication;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
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

import java.util.ArrayList;

public class SignInActivityFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener
{
    final String TAG="SignIn";

    private FirebaseAuth.AuthStateListener mAuthListener;
    EditText username;
    EditText password;
    Button signIn;
    TextView signUp;
    String Email;
    String Birthday;
    String Name;
    String Gender;
    private DatabaseReference mDatabase;
    public static Intent intent;


    //Facebook Variables
    CallbackManager mCallbackManager;
    private FirebaseAuth mAuth;
    //Google Variables
    private static final int RC_SIGN_IN = 9001;
    private GoogleApiClient mGoogleApiClient;


    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    public SignInActivityFragment() {
    }
    tApplication app;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        //AppEventsLogger.activateApp(getActivity());
        View rootView= inflater.inflate(R.layout.fragment_sign_in, container, false);
        bindViews(rootView);
        app=(tApplication) getActivity().getApplication();
        mAuth=FirebaseAuth.getInstance();
        rootView.findViewById(R.id.signout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                if(status.isSuccess()){
                                    // Toast.makeText(LogOutActivity.this,"Success",Toast.LENGTH_LONG).show();


                                }
                            }
                        });

                Intent intent=new Intent(getActivity(),SignInActivity.class);
                startActivity(intent);
            }
        });

        mDatabase= FirebaseDatabase.getInstance().getReference();
        //db.child("user").setValue("hello");
       // mDatabase.child("user1").setValue("muddassir");


        rootView.findViewById(R.id.google_login_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user!=null){
                    app.putUid(user.getUid());
                }
                if (user != null && !app.authStateListenerNotActive) {

                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    app.putUid(user.getUid());
                    String newMail=parseEmail(user.getEmail());
                    mDatabase.child("users").child(newMail).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                Intent i = new Intent(getActivity(),MapsActivity.class);
                                startActivity(i);

                            }
                            else{
                                intent = new Intent(getActivity(),FireBaseEntry.class);
                                startActivity(intent);

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };


                        //Facebook Signin///////////
        //////////////////////////////////////////////////////////////
                    ////////////////////////////////////
                            ///////////////





        mCallbackManager = CallbackManager.Factory.create();
        final LoginButton loginButton = (LoginButton) rootView.findViewById(R.id.login_button);
        loginButton.setBackgroundResource(R.drawable.sign_in_button_background);
        loginButton.setFragment(this);
        loginButton.setReadPermissions("email", "public_profile","user_birthday");

        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                //Getting birthday and email
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.v("LoginActivity", response.toString());

                                // Application code
                                try {
                                    app.authStateListenerNotActive = true;
                                    String email = object.getString("email");
                                    String birthday = object.getString("birthday"); // 01/31/1980 format
                                    String gender=object.getString("gender");
                                    String name=object.getString("name");
                                    String id=object.getString("id");
                                    Log.e("All Data",birthday+" "+id+" "+name+" "+gender+" "+email);
                                    Email=email;
                                    Birthday=birthday;
                                    Name=name;
                                    Gender=gender;


                                }catch(JSONException e){
                                    Log.e("error in json",e.toString());
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday");
                request.setParameters(parameters);
                request.executeAsync();

                handleFacebookAccessToken(loginResult.getAccessToken());

            }

            @Override
            public void onCancel() {
                Log.e(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(TAG, "facebook:onError", error);
                // ...
            }
        });

        rootView.findViewById(R.id.facebook_login_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButton.performClick();
            }
        });

        //////////////////////////////////////////////////////////////////////
                //////////////////////////////////////////////////////////
                        //////////////////////////
                            /////////////

        //Google Signin////////////



        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage(getActivity() /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //////////////////////////


        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signInWithEmailAndPassword(username.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "signInWithEmail", task.getException());
                                    Toast.makeText(getActivity(), "Username or Password was Incorrect",
                                            Toast.LENGTH_SHORT).show();
                                }else {
                                    Log.w(TAG, "signInWithEmail", task.getException());
                                    Toast.makeText(getActivity(), "Sign In Succesfull",
                                            Toast.LENGTH_SHORT).show();
                                    Intent intent=new Intent(getActivity(),MapsActivity.class);
                                    startActivity(intent);
                                }

                                // ...
                            }
                        });
            }
        });
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(),SignUpActivity.class);
                startActivity(intent);
            }
        });
        return rootView;
    }

    void bindViews(View rootView){
        username=(EditText) rootView.findViewById(R.id.username_edit_text);
        password=(EditText) rootView.findViewById(R.id.password_edit_text);
        signIn=(Button) rootView.findViewById(R.id.sign_in);
        signUp=(TextView) rootView.findViewById(R.id.sign_up);
    }
    ////////////////////////////////////Facebook Methods///////////////////////////
            ///////////////////////////////////////////////////////
                    ////////////////////////////////////
                        ///////////////////////////

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        // [START_EXCLUDE silent]

        // [END_EXCLUDE]

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(getActivity(), "Authentication failed.",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            FirebaseCheck("facebook");
                        }

                        // [START_EXCLUDE]

                        // [END_EXCLUDE]
                    }
                });
    }



    //////////////////////////////////////////////////////////////////////////////
            //////////////////////////////////////////////////////
                    ///////////////////////////////////
                            //////////////////



    ////////////////////////////////////Google Methods///////////////////////////
    ///////////////////////////////////////////////////////
    ////////////////////////////////////
    ///////////////////////////

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]

        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(getActivity(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Log.e("GOogle SignIN"," : Success");
                            FirebaseCheck("google");

                        }
                        // [START_EXCLUDE]

                        // [END_EXCLUDE]
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
        app.authStateListenerNotActive = true;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(getActivity(), "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    //////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////
    ///////////////////////////////////
    //////////////////

    /////////////////////////////////////////////UNiversal Methods///////////////////////
            ///////////////////////////////////////////////////////////////

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                GoogleSignInResult result1 = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                GoogleSignInAccount acct = result1.getSignInAccount();
                String personName = acct.getDisplayName();
                String personEmail = acct.getEmail();
                String personId = acct.getId();
                Uri personPhoto = acct.getPhotoUrl();
                Log.e("All Data of google : ",personName+" "+personEmail);

                Name=personName;
                Email=personEmail;
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // [START_EXCLUDE]

                // [END_EXCLUDE]
                Log.v("GoogleSignIn", " google sign in unsuccesful");
                Toast.makeText(getActivity(),"Google Sign In unsuccesfull",Toast.LENGTH_SHORT).show();
            }
        } else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);

        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////
            //////////////////////////////////////////////////////////////
    public void FirebaseCheck(String name) {
        app.authStateListenerNotActive = true;

        if (name.compareTo("facebook") == 0) {
            Intent i=new Intent(getActivity(),TransitionActivity.class);
            ArrayList<String> variables = new ArrayList<String>();
            Log.e("SigninFacebook",Email+" "+Birthday+" "+" "+Name+" "+Gender);
            variables.add(Email);
            /*
            Email=email;
                                    Birthday=birthday;
                                    Name=name;
                                    Gender=gender;
            */

            variables.add(Birthday);
            variables.add(Name);
            variables.add(Gender);
            i.putStringArrayListExtra("variables",variables);
            i.putExtra("loginActivity","facebook");
            startActivity(i);
            getActivity().finish();

        } else if (name.compareTo("google") == 0) {
            Intent i=new Intent(getActivity(),TransitionActivity.class);
            ArrayList<String> variables = new ArrayList<String>();
            variables.add(Email);
            variables.add(Name);
            i.putStringArrayListExtra("variables",variables);
            i.putExtra("loginActivity","google");
            startActivity(i);
            getActivity().finish();


        }
    }
    public String parseEmail(String s ){
        String newEmail="";
        for(int i = 0 ; i < s.length();i++){
            if(s.charAt(i)!='.'){
                newEmail=newEmail+String.valueOf(s.charAt(i));
            }
            else{
                newEmail=newEmail+"!";
            }
        }
        return newEmail;

    }

}
