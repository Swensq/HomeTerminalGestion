package com.example.hometerminalgestion;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HabitacionFormActivity extends AppCompatActivity {

    TextView tvTitulo;
    EditText etNombre, etDescripcion;
    Button btnGuardar, btnEliminar, btnVolver;
    DbHelper db;
    int id; // -1 = habitacion nueva

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_habitacion_form);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvTitulo = findViewById(R.id.tvTitulo);
        etNombre = findViewById(R.id.etNombre);
        etDescripcion = findViewById(R.id.etDescripcion);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnVolver = findViewById(R.id.btnVolver);
        db = new DbHelper(this);

        id = getIntent().getIntExtra("id", -1);

        if (id != -1) {
            tvTitulo.setText(R.string.titulo_habitacion_editar);
            btnEliminar.setVisibility(View.VISIBLE);
            cargarHabitacion();
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
                revisarYConfirmar();
            }
        });

        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void cargarHabitacion() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Habitacion h = db.obtenerHabitacion(id);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (h != null) {
                            etNombre.setText(h.getNombre());
                            if (h.getDescripcion() != null) {
                                etDescripcion.setText(h.getDescripcion());
                            }
                        }
                    }
                });
            }
        }).start();
    }

    private void guardar() {
        String nombre = etNombre.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();

        if (nombre.isEmpty()) {
            etNombre.setError(getString(R.string.error_campo_vacio));
            etNombre.requestFocus();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean existe = db.existeHabitacion(nombre, id);
                long resultado = -1;
                if (!existe) {
                    if (id == -1) {
                        resultado = db.insertarHabitacion(new Habitacion(0, nombre, descripcion));
                    } else {
                        resultado = db.actualizarHabitacion(new Habitacion(id, nombre, descripcion));
                    }
                }
                final long res = resultado;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (existe) {
                            etNombre.setError(getString(R.string.error_habitacion_existe));
                            etNombre.requestFocus();
                        } else if (res <= 0) {
                            Toast.makeText(HabitacionFormActivity.this, R.string.msg_error_bd, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(HabitacionFormActivity.this, R.string.msg_habitacion_guardada, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
            }
        }).start();
    }

    // primero reviso que la habitacion no tenga dispositivos; si no tiene, pido confirmacion
    private void revisarYConfirmar() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int cantidad = db.contarDispositivosDeHabitacion(id);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (cantidad > 0) {
                            Toast.makeText(HabitacionFormActivity.this, R.string.msg_habitacion_con_dispositivos, Toast.LENGTH_LONG).show();
                        } else {
                            confirmarEliminar();
                        }
                    }
                });
            }
        }).start();
    }

    private void confirmarEliminar() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialogo_titulo_eliminar)
                .setMessage(R.string.dialogo_msg_habitacion)
                .setPositiveButton(R.string.dialogo_si, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
                final int filas = db.eliminarHabitacion(id);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (filas > 0) {
                            Toast.makeText(HabitacionFormActivity.this, R.string.msg_habitacion_eliminada, Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(HabitacionFormActivity.this, R.string.msg_error_bd, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }
}
