package com.example.hometerminalgestion;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegistroActivity extends AppCompatActivity {

    EditText etNombre, etUsuario, etEmail, etPassword, etRepetir;
    Button btnCrear, btnVolver;
    DbHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etNombre = findViewById(R.id.etNombre);
        etUsuario = findViewById(R.id.etUsuario);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etRepetir = findViewById(R.id.etRepetir);
        btnCrear = findViewById(R.id.btnCrear);
        btnVolver = findViewById(R.id.btnVolver);
        db = new DbHelper(this);

        btnCrear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrar();
            }
        });

        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void registrar() {
        String nombre = etNombre.getText().toString().trim();
        String usuario = etUsuario.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String repetir = etRepetir.getText().toString();

        // validaciones que no necesitan la base de datos
        if (nombre.isEmpty() || usuario.isEmpty() || email.isEmpty() || password.isEmpty() || repetir.isEmpty()) {
            Toast.makeText(this, R.string.msg_completa_campos, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.error_email_invalido));
            etEmail.requestFocus();
            return;
        }
        if (password.length() < 4) {
            etPassword.setError(getString(R.string.error_password_corta));
            etPassword.requestFocus();
            return;
        }
        if (!password.equals(repetir)) {
            etRepetir.setError(getString(R.string.error_password_distinta));
            etRepetir.requestFocus();
            return;
        }

        // validaciones con la base y el guardado, en un hilo aparte
        new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean usuarioExiste = db.existeUsuario(usuario);
                final boolean emailExiste = db.existeEmail(email);
                long resultado = -1;
                if (!usuarioExiste && !emailExiste) {
                    // el primer usuario que se registra es admin, los demas son usuario comun
                    String rol = Usuario.ROL_USUARIO;
                    if (db.contarUsuarios() == 0) {
                        rol = Usuario.ROL_ADMIN;
                    }
                    resultado = db.insertarUsuario(new Usuario(0, usuario, nombre, email, password, rol));
                }
                final long idNuevo = resultado;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (usuarioExiste) {
                            etUsuario.setError(getString(R.string.error_usuario_existe));
                            etUsuario.requestFocus();
                        } else if (emailExiste) {
                            etEmail.setError(getString(R.string.error_email_existe));
                            etEmail.requestFocus();
                        } else if (idNuevo == -1) {
                            Toast.makeText(RegistroActivity.this, R.string.msg_error_bd, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegistroActivity.this, R.string.msg_registro_ok, Toast.LENGTH_SHORT).show();
                            finish(); // vuelve al login
                        }
                    }
                });
            }
        }).start();
    }
}
