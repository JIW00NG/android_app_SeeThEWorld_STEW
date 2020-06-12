package jbnu.moblie.app.one.team.STEW;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private String autoLoginId;
    private String autoLoginPassword;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogIn;
    private Button buttonSignUp;
    private CheckBox checkBoxAutoLogin;

    public boolean AUTOLOGINCHECK = false;
    public boolean AUTOLOGIN = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseAuth = FirebaseAuth.getInstance();

        SharedPreferences pref = getApplicationContext().getSharedPreferences("autologin",MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();

        checkBoxAutoLogin = findViewById(R.id.checkBox_auto_login);
        checkBoxAutoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(AUTOLOGINCHECK ==false){
                    AUTOLOGINCHECK =true;
                    AUTOLOGIN=true;
                }else if(AUTOLOGINCHECK ==true){
                    AUTOLOGINCHECK =false;
                    AUTOLOGIN=false;
                }
            }
        });

        editTextEmail = (EditText) findViewById(R.id.edittext_email);
        editTextPassword = (EditText) findViewById(R.id.edittext_password);

        AUTOLOGINCHECK =pref.getBoolean("isautologin",false);

        if(AUTOLOGINCHECK){
            autoLoginId=pref.getString("autoid","");
            editTextEmail.setText(autoLoginId);
            autoLoginPassword=pref.getString("autopassword","");
            editTextPassword.setText(autoLoginPassword);
            checkBoxAutoLogin.setChecked(true);
            if(pref.getBoolean("autologin",false)){
                loginUser(editTextEmail.getText().toString(), editTextPassword.getText().toString(),editor);

            }
        }

        buttonSignUp = (Button) findViewById(R.id.button_sign_up);
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // SignUpActivity 연결
                Intent intent = new Intent(Login.this, SignUp.class);
                startActivity(intent);
            }
        });

        buttonLogIn = (Button) findViewById(R.id.button_login);
        buttonLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!("").equals(editTextEmail.getText().toString()) && !editTextPassword.getText().toString().equals("")) {
                    loginUser(editTextEmail.getText().toString(), editTextPassword.getText().toString(),editor);
                } else {
                    Toast.makeText(Login.this, "Enter the ID and Password.", Toast.LENGTH_LONG).show();
                }
            }
        });

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Intent intent = new Intent(Login.this, TextRecogniserActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                }
            }
        };
    }

    public void loginUser(String email, final String password, final SharedPreferences.Editor editor) {
        final String finalEmail = email;
        final String finalPassword = password;

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // 로그인 성공
                            if(AUTOLOGINCHECK){
                                editor.putString("autoid",finalEmail);
                                editor.putString("autopassword",finalPassword);
                                editor.putBoolean("isautologin", AUTOLOGINCHECK);
                                editor.commit();
                            }else if(!AUTOLOGINCHECK){
                                editor.clear();
                                editor.commit();
                            }
                            Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            firebaseAuth.addAuthStateListener(firebaseAuthListener);
                        } else {
                            // 로그인 실패
                            Toast.makeText(Login.this, "ID or Password does not match.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        editor.putBoolean("autologin",true);
        editor.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuthListener != null) {
            firebaseAuth.removeAuthStateListener(firebaseAuthListener);
        }
    }
}
