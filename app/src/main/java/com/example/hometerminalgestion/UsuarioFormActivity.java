package com.example.hometerminalgestion;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class UsuarioFormActivity extends AppCompatActivity {

    TextView tvTitulo;
    EditText etNombre, etUsuario, etEmail, etPassword, etRepetir;
    LinearLayout layoutRol;
    RadioGroup rgRol;
    Button btnGuardar, btnEliminar, btnVolver;
    DbHelper db;
    int id;                 // -1 = usuario nuevo
    int idUsuarioLogueado;
    boolean esAdmin;        // el que esta logueado es admin?
    String rolCargado = Usuario.ROL_USUARIO; // rol que ya tenia el usuario que se edita

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_usuario_form);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvTitulo = findViewById(R.id.tvTitulo);
        etNombre = findViewById(R.id.etNombre);
        etUsuario = findViewById(R.id.etUsuario);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etRepetir = findViewById(R.id.etRepetir);
        layoutRol = findViewById(R.id.layoutRol);
        rgRol = findViewById(R.id.rgRol);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnVolver = findViewById(R.id.btnVolver);
        db = new DbHelper(this);

        id = getIntent().getIntExtra("id", -1);
        idUsuarioLogueado = getIntent().getIntExtra("idUsuario", -1);

        esAdmin = Usuario.ROL_ADMIN.equals(getIntent().getStringExtra("rol"));

        // el selector de rol y el boton eliminar son solo para el admin
        if (esAdmin) {
            layoutRol.setVisibility(View.VISIBLE);
        }

        // si estoy editando, cargo los datos
        if (id != -1) {
            if (esAdmin) {
                tvTitulo.setText(R.string.titulo_usuario_editar);
                btnEliminar.setVisibility(View.VISIBLE);
            } else {
                // usuario comun editando su propia cuenta
                tvTitulo.setText(R.string.titulo_mi_cuenta);
            }
            cargarUsuario();
        }

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardar();
            }
        });

        btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmarEliminar();
            }
        });

        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void cargarUsuario() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Usuario u = db.obtenerUsuario(id);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (u != null) {
                            etNombre.setText(u.getNombre());
                            etUsuario.setText(u.getUsuario());
                            etEmail.setText(u.getEmail());
                            etPassword.setText(u.getPassword());
                            etRepetir.setText(u.getPassword());
                            rolCargado = u.getRol();
                            if (Usuario.ROL_ADMIN.equals(u.getRol())) {
                                rgRol.check(R.id.rbAdmin);
                            } else {
                                rgRol.check(R.id.rbUsuario);
                            }
                        }
                    }
                });
            }
        }).start();
    }

    private void guardar() {
        String nombre = etNombre.getText().toString().trim();
        String usuario = etUsuario.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String repetir = etRepetir.getText().toString();

        // mismas validaciones que el registro
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

        // el rol: el admin lo elige, el usuario comun conserva el que tenia
        String rol = rolCargado;
        if (esAdmin) {
            if (rgRol.getCheckedRadioButtonId() == R.id.rbAdmin) {
                rol = Usuario.ROL_ADMIN;
            } else {
                rol = Usuario.ROL_USUARIO;
            }
            // un admin no puede sacarse el rol a si mismo
            if (id == idUsuarioLogueado && !rol.equals(Usuario.ROL_ADMIN)) {
                Toast.makeText(this, R.string.msg_no_quitar_admin, Toast.LENGTH_LONG).show();
                return;
            }
        }
        final String rolFinal = rol;

        new Thread(new Runnable() {
            @Override
            public void run() {
                // al comparar excluyo al propio usuario (si es nuevo id vale -1)
                final boolean usuarioExiste = db.existeUsuario(usuario, id);
                final boolean emailExiste = db.existeEmail(email, id);
                long resultado = -1;
                if (!usuarioExiste && !emailExiste) {
                    if (id == -1) {
                        resultado = db.insertarUsuario(new Usuario(0, usuario, nombre, email, password, rolFinal));
                    } else {
                        resultado = db.actualizarUsuario(new Usuario(id, usuario, nombre, email, password, rolFinal));
                    }
                }
                final long res = resultado;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (usuarioExiste) {
                            etUsuario.setError(getString(R.string.error_usuario_existe));
                            etUsuario.requestFocus();
                        } else if (emailExiste) {
                            etEmail.setError(getString(R.string.error_email_existe));
                            etEmail.requestFocus();
                        } else if (res <= 0) {
                            Toast.makeText(UsuarioFormActivity.this, R.string.msg_error_bd, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(UsuarioFormActivity.this, R.string.msg_usuario_guardado, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
            }
        }).start();
    }

    private void confirmarEliminar() {
        // no se puede borrar el usuario que tiene la sesion iniciada
        if (id == idUsuarioLogueado) {
            Toast.makeText(this, R.string.msg_no_eliminar_propio, Toast.LENGTH_LONG).show();
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialogo_titulo_eliminar)
                .setMessage(R.string.dialogo_msg_usuario)
                .setPositiveButton(R.string.dialogo_si, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        eliminar();
                    }
                })
                .setNegativeButton(R.string.dialogo_no, null)
                .show();
    }

    private void eliminar() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int filas = db.eliminarUsuario(id);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (filas > 0) {
                            Toast.makeText(UsuarioFormActivity.this, R.string.msg_usuario_eliminado, Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(UsuarioFormActivity.this, R.string.msg_error_bd, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }
}
