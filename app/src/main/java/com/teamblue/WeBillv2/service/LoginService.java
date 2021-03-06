package com.teamblue.WeBillv2.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.teamblue.WeBillv2.model.api.LoginMethods;
import com.teamblue.WeBillv2.model.pojo.Constants;
import com.teamblue.WeBillv2.model.pojo.LoginModel;
import com.teamblue.WeBillv2.model.pojo.SignUpUser;
import com.teamblue.WeBillv2.model.pojo.User;
import com.teamblue.WeBillv2.view.MenuView;
import com.teamblue.WeBillv2.view.StripeAccountsActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginService extends AppCompatActivity {

    private String TAG = "Login";
    private String USERNAME="";

    /*
    making network call to our backend api to authorize login.
    parameters :
       context - to pass the activity where its being called, in this case its the MainActivity.class
       email - the edit text widget where the user enters their email
       password - the edit text widget where the user enters their password

       Return type:
       void - the function if successful will start an intent to the friends activity.
     */
    public void authorizeLogin(Context context,EditText username, EditText password){
        //1. create an instance of login methods interface defined in our LoginMethods class
        LoginMethods loginMethods = LoginRetrofitClient.getRetrofitInstance().create(LoginMethods.class);
        //2. create a call object which will make the REST API call to our backend by passing in email and password as paramaters
        Call<LoginModel> call = loginMethods.login(new User(username.getText().toString().trim(),password.getText().toString().trim()));
        Log.d(TAG,username.getText().toString().trim());
        Log.d(TAG,password.getText().toString().trim());

        /*3. create a callback for our call object, once its finished the network call, it will use this callback to further
           process whether the network call was successful or not.
         */
        call.enqueue(new Callback<LoginModel>() {
            @Override
            public void onResponse(Call<LoginModel> call, Response<LoginModel> response) {
                Log.d(TAG, String.valueOf(response.code()));
                Log.d(TAG,response.toString());

                //if network call was successful
                if(response.code()== Constants.RESPONSE_OK){
                    //create our defined object from the response body of the api call
                    LoginModel loginResponse = (LoginModel) response.body();
                    Log.d(TAG,"login successful");
                    Toast.makeText(context,loginResponse.getMessage(), Toast.LENGTH_LONG).show();
                    USERNAME = username.getText().toString().trim();

                    //set it to preferences
                    SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFERENCES_FILE_NAME,Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(Constants.USERNAME_KEY,USERNAME);
                    editor.putString(Constants.PASSWORD_KEY,password.getText().toString().trim());
                    editor.apply();
                    Log.d(TAG,sharedPreferences.getString(Constants.USERNAME_KEY,""));


                    //move to activity if successful login
                    if(loginResponse.getStatus()==Constants.RESPONSE_OK){
                        //create intent to move to friends activity on successful sign in
                        Intent intent = new Intent(context,MenuView.class);
                        //since calling intent from another activity have to set this flag to true
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        //start activity for intent created
                        context.startActivity(intent);
                    }

                    else{
                        //on login fail- clear out the text boxes, to allow user to re-enter details
                        username.setText("");
                        password.setText("");
                        Toast.makeText(context,"login unsuccessful",Toast.LENGTH_LONG).show();
                    }
                }else{
                    //network call was unsuccessful, allow users to re-enter their details
                    Log.d(TAG,"login unsuccessful");
                    username.setText("");
                    password.setText("");
                    Toast.makeText(context,"login unsuccessful",Toast.LENGTH_LONG).show();
                }
            }

            //when api call was unable to hit the backend api for some reason, then throw the message to the user.
            @Override
            public void onFailure(Call<LoginModel> call, Throwable t) {
                Log.d(TAG,"login unsuccessful");
                Toast.makeText(context,t.getMessage().toString(),Toast.LENGTH_LONG).show();
            }
        });
    }

    /*
    making network call to our backend api to register user.
    parameters :
       context - to pass the activity where its being called, in this case its the SignUpView.class
       email - the edit text widget where the user enters their email
       username - the edit text widget where the user enters their username
       password - the edit text widget where the user enters their password

       Return type:
       void - the function if successful will start an intent to the stripe accounts activity, else will empty the fields and allow user to re-enter the details.
     */
    public void registerUser(Context context,SignUpUser signUpUser){
        Log.d(TAG,signUpUser.getUsername().toString().trim());
        Toast.makeText(context,"Sign up attempt",Toast.LENGTH_LONG).show();

        //1. create an instance of login methods interface defined in our LoginMethods class
        LoginMethods loginMethods = LoginRetrofitClient.getRetrofitInstance().create(LoginMethods.class);
        //2. create a call object which will make the REST API call to our backend by passing in email,username and password as paramaters
        Call<LoginModel> call = loginMethods.signup(signUpUser);
        Log.d(TAG,signUpUser.getEmail().toString().trim());

        call.enqueue(new Callback<LoginModel>() {
            @Override
            public void onResponse(Call<LoginModel> call, Response<LoginModel> response) {
                Log.d(TAG, String.valueOf(response.code()));
                Log.d(TAG,response.toString());

                //if network call was successful
                if(response.code()== Constants.RESPONSE_OK){
                    //create our defined object from the response body of the api call
                    LoginModel signUpResponse = (LoginModel) response.body();
                    Log.d(TAG,"sign up successful");
                    Toast.makeText(context,signUpResponse.getMessage(), Toast.LENGTH_LONG).show();

                    //making network call to get user
                    //getUser(email.getText().toString().trim());
                    //set it to preferences
                    SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFERENCES_FILE_NAME,Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(Constants.USERNAME_KEY, signUpUser.getUsername());
                    editor.apply();

                    //move to activity if successful login
                    if(signUpResponse.getStatus()==Constants.RESPONSE_OK){
                        //create intent to move to stripe accounts activity on successful sign in
                        Intent intent = new Intent(context, StripeAccountsActivity.class);
                        intent.putExtra("username", signUpUser.getUsername().trim());
                        //since calling intent from another activity have to set this flag to true
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        //start activity for intent created
                        context.startActivity(intent);

                    }

                    else{
                        //on login fail- clear out the text boxes, to allow user to re-enter details
                        Toast.makeText(context,"sign up unsuccessful",Toast.LENGTH_LONG).show();
                    }
                }else{
                    //network call was unsuccessful, allow users to re-enter their details
                    Log.d(TAG,"sign up unsuccessful");
                    Toast.makeText(context,"sign up unsuccessful",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginModel> call, Throwable t) {
                Log.d(TAG,"sign up unsuccessful");
                Toast.makeText(context,t.getMessage().toString(),Toast.LENGTH_LONG).show();
            }
        });
    }

}
