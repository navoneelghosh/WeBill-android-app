package com.teamblue.WeBillv2.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.teamblue.WeBillv2.R;
import com.teamblue.WeBillv2.controller.LoginController;
import com.teamblue.WeBillv2.model.pojo.Constants;

import java.time.Year;

public class MainActivity extends AppCompatActivity {
    private Button loginButton;
    private Button signUpButton;
    private EditText usernameEditText;
    private EditText passwordEditText;

    private Button btnSkip; // Skip login for developer testing (Delete after finished)
//create an instance of login controller
    private LoginController loginController = new LoginController();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginButton = (Button) findViewById(R.id.registerButtonSignUp);
        signUpButton = (Button) findViewById(R.id.btnSignUp);
        usernameEditText = (EditText) findViewById(R.id.emailEditTextSignUp);
        passwordEditText = (EditText) findViewById(R.id.usernameEditTextSignUp);

//        btnSkip = (Button) findViewById(R.id.btnSkip); // Skip login for developer testing (Delete after finished)

        //set on click listener for login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //call login controller's login method
                setDefaultAppYear();
                loginController.login(getApplicationContext(),usernameEditText,passwordEditText);
            }
        });



        //set on click listener for sign up button
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //call login controllers sign up method
                Toast.makeText(getApplicationContext(),"Sign up",Toast.LENGTH_LONG).show();
                setDefaultAppYear();
                //move to sign up page
                segueToSignUpActivity();
            }
        });

//        // Skip login for developer testing (Delete after finished)
//        btnSkip.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent goFriendPage = new Intent(MainActivity.this,MenuView.class);
//                startActivity(goFriendPage);
//            }
//        });
    }
//intent to go to the sign up page
    public void segueToSignUpActivity(){
        Intent intent = new Intent(this,SignUpView.class);
        startActivity(intent);
    }
//function to set the default year
    public void setDefaultAppYear(){
        String currentYear = Integer.toString(Year.now().getValue());
        //set it to preferences
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(Constants.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.FILTER_YEAR,currentYear);
        editor.apply();
        Log.d("=======TAG======",(sharedPreferences.getString(Constants.FILTER_YEAR,Integer.toString(Year.now().getValue()))));
    }

}
