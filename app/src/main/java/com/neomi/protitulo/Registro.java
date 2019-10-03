package com.neomi.protitulo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Registro extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;


    private EditText etPassword;
    private EditText etEmail;
    private String eType;

    private Button btnCancelar;
    private Button btnRegistar;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Usuarios");

        etEmail = (EditText) findViewById(R.id.etRegEmail);
        etPassword = (EditText) findViewById(R.id.etRegPassword);
        eType = "alumno";
        btnRegistar = (Button) findViewById(R.id.btnRegistrar);
        btnCancelar = (Button) findViewById(R.id.btnCancelar);
        progressDialog = new ProgressDialog(this);

        btnRegistar.setOnClickListener(this);
        btnCancelar.setOnClickListener(this);

    }

    private void registrarUsuario(){

        final String email = etEmail.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();
        final String type = eType;

        if (TextUtils.isEmpty(email)){
            Toast.makeText(Registro.this,"Falta ingresar su email",Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(Registro.this,"Falta ingresar su contraseña",Toast.LENGTH_LONG).show();
            return;
        }

        progressDialog.setMessage("Registrando al usuario...");
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            final String UserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            Toast.makeText(Registro.this,"Registro exitoso",Toast.LENGTH_LONG).show();
                            Usuario usuario = new Usuario(UserId,email);
                            mDatabaseRef.child(UserId).setValue(usuario);
                            volver();
                        } else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException){
                                Toast.makeText(Registro.this,"Este usuario ya existe",Toast.LENGTH_LONG).show();
                            }
                            Toast.makeText(Registro.this,"Algo a fallado",Toast.LENGTH_LONG).show();
                        }
                        progressDialog.dismiss();
                    }
                });

    }

    private void volver(){
        Intent intent = new Intent(getApplication(),LoginActivity.class);
        startActivity(intent);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnCancelar:
                volver();
                break;

            case R.id.btnRegistrar:
                registrarUsuario();
                break;
        }
    }
}