package com.facens.conectbus.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.facens.conectbus.R;
import com.facens.conectbus.config.ConfiguracaoFirebase;
import com.facens.conectbus.helper.UsuarioFirebase;
import com.facens.conectbus.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText campoEmail, camposSenha;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        campoEmail = findViewById(R.id.editLoginEmail);
        camposSenha = findViewById(R.id.editLoginSenha);
    }

    public void validarLoginUsuario(View view){

        String textoEmail = campoEmail.getText().toString();
        String textoSenha = camposSenha.getText().toString();

        if ( !textoEmail.isEmpty()){
            if ( !textoSenha.isEmpty()){
                Usuario usuario = new Usuario();
                usuario.setEmail( textoEmail );
                usuario.setSenha( textoSenha );

                logarUsuario( usuario );

            }else {
                Toast.makeText(LoginActivity.this,"Insira a senha!", Toast.LENGTH_LONG).show();
            }
        }else {
            Toast.makeText(LoginActivity.this,"Preencha o email!", Toast.LENGTH_LONG).show();
        }

    }

    public void logarUsuario(Usuario usuario) {

        auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
        auth.signInWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    UsuarioFirebase.redirecionaUsuarioLogado(LoginActivity.this);

                }else {
                    String excecao = "";
                    try {
                        throw task.getException();
                    }catch (FirebaseAuthInvalidUserException e){
                        excecao = "Usuario não cadastrado.";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        excecao = "E-mail e senha não correspondem!";
                    }catch (Exception e){
                        excecao = "Erro!";
                        e.printStackTrace();
                    }
                    Toast.makeText(LoginActivity.this, excecao, Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}
