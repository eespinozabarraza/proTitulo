package com.neomi.protitulo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "FirebaseEmailPassword";

    private FirebaseAuth mAuth;


    private EditText etEmail, etPassword;
    private Button btnIngresar;
    private TextView tvRegistrarse, txtStatus, txtDetail;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        txtStatus = (TextView) findViewById(R.id.status);
        txtDetail = (TextView) findViewById(R.id.detail);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        btnIngresar = (Button) findViewById(R.id.btnIngresar);
        tvRegistrarse = (TextView) findViewById(R.id.btnRegistrarse);
        progressDialog = new ProgressDialog(this);

        btnIngresar.setOnClickListener(this);
        tvRegistrarse.setOnClickListener(this);

    }
    @Override
    public void onStart(){
        super.onStart();
        FirebaseUser currentUser =mAuth.getCurrentUser();
        updateUI(currentUser);


    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(getApplication(),MainActivity.class);
            startActivity(intent);

        } else {
            txtStatus.setText("Signed Out");
            txtDetail.setText(null);

            findViewById(R.id.etEmail).setVisibility(View.VISIBLE);
            findViewById(R.id.etPassword).setVisibility(View.VISIBLE);
            findViewById(R.id.btnIngresar).setVisibility(View.VISIBLE);
            findViewById(R.id.btnRegistrarse).setVisibility(View.VISIBLE);
        }
    }


    private void logearUsuario(){

        final String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!TextUtils.isEmpty(email)){
            if (!TextUtils.isEmpty(password)){

                progressDialog.setMessage("Ingresando...");
                progressDialog.show();

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.e(TAG, "Ingreso exitoso");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    updateUI(user);
                                } else {
                                    Log.e(TAG, "signIn: Fail!", task.getException());
                                    Toast.makeText(getApplicationContext(), "Authentication failed!", Toast.LENGTH_SHORT).show();
                                    updateUI(null);
                                }

                                if (!task.isSuccessful()) {
                                    txtStatus.setText("Authentication failed!");
                                }
                            }
                        });
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnRegistrarse:
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
                break;

            case R.id.btnIngresar:
                logearUsuario();
                break;
        }
    }
}
