package com.example.hometerminalgestion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    EditText etUsuario, etPassword;
    Button btnIngresar, btnRegistrarse;
    DbHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etUsuario = findViewById(R.id.etUsuario);
        etPassword = findViewById(R.id.etPassword);
        btnIngresar = findViewById(R.id.btnIngresar);
        btnRegistrarse = findViewById(R.id.btnRegistrarse);
        db = new DbHelper(this);

        btnIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ingresar();
            }
        });

        btnRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegistroActivity.class));
            }
        });
    }

    private void ingresar() {
        String usuario = etUsuario.getText().toString().trim();
        String password = etPassword.getText().toString();

        // primero reviso que no haya campos vacios
        if (usuario.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.msg_completa_usuario_password, Toast.LENGTH_SHORT).show();
            return;
        }

        // la consulta a la base va en un hilo aparte para no trabar la pantalla
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Usuario u = db.validarLogin(usuario, password);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (u == null) {
                            Toast.makeText(LoginActivity.this, R.string.msg_login_incorrecto, Toast.LENGTH_SHORT).show();
                        } else {
                            // login correcto: paso al menu con el id y el nombre
                            Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                            intent.putExtra("idUsuario", u.getId());
                            intent.putExtra("nombre", u.getNombre());
                            intent.putExtra("rol", u.getRol());
                            startActivity(intent);
                            finish();
                        }
                    }
                });
            }
        }).start();
    }
}
