package com.vanard.tulis;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private Button btnRegister, btnLogin;
    private EditText etEmail, etPassword, etConf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.et_mail_register);
        etPassword = findViewById(R.id.et_pass_register);
        etConf = findViewById(R.id.et_conf_register);
        btnLogin = findViewById(R.id.btn_register_login);
        btnRegister = findViewById(R.id.btn_register);

        buttonOnClick();
    }

    private void buttonOnClick() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String regMail = etEmail.getText().toString();
                String regPass = etPassword.getText().toString();
                String confPass = etConf.getText().toString();

                if (!regMail.isEmpty() && !regPass.isEmpty() && !confPass.isEmpty()){
                    if (regPass.equals(confPass)){
                        mAuth.createUserWithEmailAndPassword(regMail, regPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    sendToMain();
                                }else{
                                    String e = task.getException().getMessage();
                                    toast("Error : " + e);
                                }
                            }
                        });
                    }else{
                        toast("Password doens't match");
                    }
                }else{
                    toast("Fill the form first");
                }
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null){
            sendToMain();
        }
    }

    private void sendToMain(){
        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
        finish();
    }

    private void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
