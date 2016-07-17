package com.example.hp.thetacab.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.hp.thetacab.Activities.SignInActivity;
import com.example.hp.thetacab.FinalUserObject;
import com.example.hp.thetacab.R;
import com.example.hp.thetacab.tApplication;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * A placeholder fragment containing a simple view.
 */
public class SignUpActivityFragment extends Fragment {
    final String TAG="SignUp";
    FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    EditText username;
    EditText password;
    EditText CellPhone;
    EditText CNIC;
    EditText Gender;
    EditText Birthday;
    EditText Name;
    RadioGroup customerid;
    String customerId;
    Button signUp;
    private DatabaseReference mDatabase;

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
    public SignUpActivityFragment() {
    }
    String UID;
    tApplication app;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView= inflater.inflate(R.layout.fragment_sign_up, container, false);
        mAuth=FirebaseAuth.getInstance();
        bindViews(rootView);
        mDatabase= FirebaseDatabase.getInstance().getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    UID=user.getUid();
                    app=(tApplication)getActivity().getApplication();
                    app.putUid(UID);
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id=customerid.getCheckedRadioButtonId();
                customerId=((RadioButton)rootView.findViewById(id)).getText().toString();
                mAuth.createUserWithEmailAndPassword(username.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Toast.makeText(getActivity(), "Sign Up failed.",
                                            Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(getActivity(), "Sign Up Successfull.",
                                            Toast.LENGTH_SHORT).show();
                                    FinalUserObject user= new FinalUserObject(username.getText().toString(),Name.getText().toString(),Birthday.getText().toString(),Gender.getText().toString(),CNIC.getText().toString(),CellPhone.getText().toString(),customerId);
                                    mDatabase.child("users").child(parseEmail(username.getText().toString())).setValue("1");
                                    mDatabase.child("Customers").child(UID).setValue(user);
                                    Intent intent=new Intent(getActivity(),SignInActivity.class);
                                    startActivity(intent);
                                    getActivity().finish();
                                }

                                // ...
                            }
                        });
            }
        });
        return rootView;
    }
    void bindViews(View rootView){
        username=(EditText) rootView.findViewById(R.id.username);
        password=(EditText) rootView.findViewById(R.id.password);
        signUp=(Button) rootView.findViewById(R.id.sign_up);
        CellPhone=(EditText)rootView.findViewById(R.id.cell_phone_edit_signup);
        CNIC=(EditText)rootView.findViewById(R.id.cnic_edit_signup);
        Gender=(EditText)rootView.findViewById(R.id.gender_edit_signup);
        Birthday=(EditText)rootView.findViewById(R.id.birthday_edit_signup);
        customerid=(RadioGroup) rootView.findViewById(R.id.customerid_signup);
        Name=(EditText)rootView.findViewById(R.id.name_edit);
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
