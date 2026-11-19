package com.example.hometerminalgestion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MenuActivity extends AppCompatActivity {

    TextView tvSaludo, tvRol, tvResumen;
    Button btnUsuarios, btnMiCuenta, btnDispositivos, btnHabitaciones, btnCerrarSesion;
    DbHelper db;
    int idUsuario;
    String rol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvSaludo = findViewById(R.id.tvSaludo);
        tvRol = findViewById(R.id.tvRol);
        tvResumen = findViewById(R.id.tvResumen);
        btnUsuarios = findViewById(R.id.btnUsuarios);
        btnMiCuenta = findViewById(R.id.btnMiCuenta);
        btnDispositivos = findViewById(R.id.btnDispositivos);
        btnHabitaciones = findViewById(R.id.btnHabitaciones);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        db = new DbHelper(this);

        // datos que mando el login
        idUsuario = getIntent().getIntExtra("idUsuario", -1);
        rol = getIntent().getStringExtra("rol");
        String nombre = getIntent().getStringExtra("nombre");
        tvSaludo.setText(getString(R.string.saludo, nombre));

        // segun el rol muestro "Usuarios" (admin) o "Mi cuenta" (usuario comun)
        boolean esAdmin = Usuario.ROL_ADMIN.equals(rol);
        if (esAdmin) {
            tvRol.setText(getString(R.string.rol_menu, getString(R.string.rol_admin)));
            btnUsuarios.setVisibility(View.VISIBLE);
            btnMiCuenta.setVisibility(View.GONE);
        } else {
            tvRol.setText(getString(R.string.rol_menu, getString(R.string.rol_usuario)));
            btnUsuarios.setVisibility(View.GONE);
            btnMiCuenta.setVisibility(View.VISIBLE);
        }

        btnUsuarios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, UsuariosActivity.class);
                intent.putExtra("idUsuario", idUsuario);
                intent.putExtra("rol", rol);
                startActivity(intent);
            }
        });

        // "Mi cuenta" abre el formulario con mi propio usuario
        btnMiCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, UsuarioFormActivity.class);
                intent.putExtra("id", idUsuario);
                intent.putExtra("idUsuario", idUsuario);
                intent.putExtra("rol", rol);
                startActivity(intent);
            }
        });

        btnDispositivos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, DispositivosActivity.class));
            }
        });

        btnHabitaciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, HabitacionesActivity.class));
            }
        });

        btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // vuelvo al login y cierro el menu
                startActivity(new Intent(MenuActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    // el resumen se actualiza cada vez que vuelvo al menu
    @Override
    protected void onResume() {
        super.onResume();
        cargarResumen();
    }

    private void cargarResumen() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int total = db.contarDispositivos();
                final int enLinea = db.contarEnLinea();
                final int verificados = db.contarVerificados();
                final int sinHabitacion = db.contarSinHabitacion();
                final ArrayList<String[]> habitaciones = db.resumenPorHabitacion();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // armo el texto del resumen, una linea por dato
                        StringBuilder texto = new StringBuilder();
                        texto.append(getString(R.string.resumen_total, total)).append("\n");
                        texto.append(getString(R.string.resumen_en_linea, enLinea, verificados));
                        for (String[] fila : habitaciones) {
                            texto.append("\n").append(getString(R.string.resumen_habitacion,
                                    fila[0], Integer.parseInt(fila[1])));
                        }
                        if (sinHabitacion > 0) {
                            texto.append("\n").append(getString(R.string.resumen_habitacion,
                                    getString(R.string.opcion_sin_habitacion), sinHabitacion));
                        }
                        tvResumen.setText(texto.toString());
                    }
                });
            }
        }).start();
    }
}
