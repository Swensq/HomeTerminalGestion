package com.example.hometerminalgestion;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DispositivoFormActivity extends AppCompatActivity {

    TextView tvTitulo, tvEstado;
    EditText etNombre, etHdmi, etIp, etPuertoRed;
    RadioGroup rgTipo;
    Spinner spHabitacion;
    LinearLayout layoutRed;
    Button btnGuardar, btnEliminar, btnVolver, btnProbar, btnPanel;
    DbHelper db;
    int id; // -1 = dispositivo nuevo
    ArrayList<Habitacion> habitaciones = new ArrayList<>();
    Dispositivo actual; // el dispositivo que se esta editando (null si es nuevo)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dispositivo_form);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvTitulo = findViewById(R.id.tvTitulo);
        tvEstado = findViewById(R.id.tvEstado);
        etNombre = findViewById(R.id.etNombre);
        etHdmi = findViewById(R.id.etHdmi);
        etIp = findViewById(R.id.etIp);
        etPuertoRed = findViewById(R.id.etPuertoRed);
        rgTipo = findViewById(R.id.rgTipo);
        spHabitacion = findViewById(R.id.spHabitacion);
        layoutRed = findViewById(R.id.layoutRed);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnVolver = findViewById(R.id.btnVolver);
        btnProbar = findViewById(R.id.btnProbar);
        btnPanel = findViewById(R.id.btnPanel);
        db = new DbHelper(this);

        id = getIntent().getIntExtra("id", -1);

        if (id != -1) {
            tvTitulo.setText(R.string.titulo_dispositivo_editar);
            btnEliminar.setVisibility(View.VISIBLE);
        }
        cargarDatos();

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

        btnProbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                probarConexion();
            }
        });

        btnPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirPanel();
            }
        });

        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    // carga las habitaciones para el Spinner y, si estoy editando, el dispositivo
    private void cargarDatos() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<Habitacion> listaHab = db.listarHabitaciones();
                Dispositivo d = null;
                if (id != -1) {
                    d = db.obtenerDispositivo(id);
                }
                final Dispositivo dispositivo = d;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        habitaciones = listaHab;

                        // la primera opcion del Spinner es "Sin habitacion"
                        ArrayList<String> nombres = new ArrayList<>();
                        nombres.add(getString(R.string.opcion_sin_habitacion));
                        for (Habitacion h : habitaciones) {
                            nombres.add(h.getNombre());
                        }
                        ArrayAdapter<String> adaptador = new ArrayAdapter<String>(DispositivoFormActivity.this,
                                android.R.layout.simple_spinner_item, nombres);
                        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spHabitacion.setAdapter(adaptador);

                        if (dispositivo != null) {
                            mostrarDispositivo(dispositivo);
                        }
                    }
                });
            }
        }).start();
    }

    // pone los datos del dispositivo en los campos
    private void mostrarDispositivo(Dispositivo d) {
        actual = d;
        etNombre.setText(d.getNombre());
        if (d.getPuertoHdmi() > 0) {
            etHdmi.setText(String.valueOf(d.getPuertoHdmi()));
        }
        marcarTipo(d.getTipo());

        // busco la habitacion en la lista (la posicion 0 es "Sin habitacion")
        for (int i = 0; i < habitaciones.size(); i++) {
            if (habitaciones.get(i).getId() == d.getIdHabitacion()) {
                spHabitacion.setSelection(i + 1);
            }
        }

        if (d.tieneIp()) {
            etIp.setText(d.getIp());
            if (d.getPuerto() > 0) {
                etPuertoRed.setText(String.valueOf(d.getPuerto()));
            }
            // los botones de red solo aparecen si tiene IP
            layoutRed.setVisibility(View.VISIBLE);
            mostrarEstado();
        }
    }

    // marca el RadioButton que coincide con el tipo guardado
    private void marcarTipo(String tipo) {
        if (tipo.equals(getString(R.string.tipo_tv))) {
            rgTipo.check(R.id.rbTv);
        } else if (tipo.equals(getString(R.string.tipo_consola))) {
            rgTipo.check(R.id.rbConsola);
        } else if (tipo.equals(getString(R.string.tipo_audio))) {
            rgTipo.check(R.id.rbAudio);
        } else {
            rgTipo.check(R.id.rbOtro);
        }
    }

    // muestra si esta en linea o no y cuando se verifico
    private void mostrarEstado() {
        if (actual.getEnLinea() == 1) {
            tvEstado.setText(getString(R.string.estado_en_linea, actual.getUltimoChequeo()));
        } else if (actual.getEnLinea() == 0) {
            tvEstado.setText(getString(R.string.estado_sin_respuesta, actual.getUltimoChequeo()));
        } else {
            tvEstado.setText(R.string.estado_nunca);
        }
    }

    private void guardar() {
        String nombre = etNombre.getText().toString().trim();
        String hdmiTexto = etHdmi.getText().toString().trim();
        String ip = etIp.getText().toString().trim();
        String puertoTexto = etPuertoRed.getText().toString().trim();

        // nombre obligatorio
        if (nombre.isEmpty()) {
            etNombre.setError(getString(R.string.error_campo_vacio));
            etNombre.requestFocus();
            return;
        }

        // tiene que haber un tipo elegido
        int idElegido = rgTipo.getCheckedRadioButtonId();
        if (idElegido == -1) {
            Toast.makeText(this, R.string.error_tipo, Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton rbElegido = findViewById(idElegido);
        String tipo = rbElegido.getText().toString();

        // el puerto HDMI es opcional, pero si esta tiene que ser de 1 a 4
        int hdmi = 0;
        if (!hdmiTexto.isEmpty()) {
            try {
                hdmi = Integer.parseInt(hdmiTexto);
            } catch (NumberFormatException e) {
                hdmi = -1;
            }
            if (hdmi < 1 || hdmi > 4) {
                etHdmi.setError(getString(R.string.error_hdmi));
                etHdmi.requestFocus();
                return;
            }
        }

        // la IP es opcional, pero si esta tiene que ser valida
        int puertoRed = 0;
        if (!ip.isEmpty()) {
            if (!Patterns.IP_ADDRESS.matcher(ip).matches()) {
                etIp.setError(getString(R.string.error_ip));
                etIp.requestFocus();
                return;
            }
            // si hay IP y no hay puerto uso el 80
            if (puertoTexto.isEmpty()) {
                puertoRed = 80;
            } else {
                try {
                    puertoRed = Integer.parseInt(puertoTexto);
                } catch (NumberFormatException e) {
                    puertoRed = -1;
                }
                if (puertoRed < 1 || puertoRed > 65535) {
                    etPuertoRed.setError(getString(R.string.error_puerto_red));
                    etPuertoRed.requestFocus();
                    return;
                }
            }
        }

        // habitacion elegida (posicion 0 = sin habitacion)
        int idHabitacion = 0;
        int posicion = spHabitacion.getSelectedItemPosition();
        if (posicion > 0) {
            idHabitacion = habitaciones.get(posicion - 1).getId();
        }

        // si cambio la IP o el puerto, el estado anterior ya no vale
        int enLinea = -1;
        String ultimoChequeo = null;
        if (actual != null && ip.equals(actual.tieneIp() ? actual.getIp() : "") && puertoRed == actual.getPuerto()) {
            enLinea = actual.getEnLinea();
            ultimoChequeo = actual.getUltimoChequeo();
        }

        final Dispositivo nuevo = new Dispositivo(Math.max(id, 0), nombre, tipo, hdmi, idHabitacion,
                null, ip, puertoRed, enLinea, ultimoChequeo);

        new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean existe = db.existeDispositivo(nombre, id);
                long resultado = -1;
                if (!existe) {
                    if (id == -1) {
                        resultado = db.insertarDispositivo(nuevo);
                    } else {
                        resultado = db.actualizarDispositivo(nuevo);
                    }
                }
                final long res = resultado;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (existe) {
                            etNombre.setError(getString(R.string.error_dispositivo_existe));
                            etNombre.requestFocus();
                        } else if (res <= 0) {
                            Toast.makeText(DispositivoFormActivity.this, R.string.msg_error_bd, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(DispositivoFormActivity.this, R.string.msg_dispositivo_guardado, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
            }
        }).start();
    }

    // prueba la conexion al dispositivo (usa los datos guardados)
    private void probarConexion() {
        if (actual == null || !actual.tieneIp()) {
            return;
        }
        final String ip = actual.getIp();
        int puerto = actual.getPuerto();
        if (puerto <= 0) {
            puerto = 80;
        }
        final int puertoFinal = puerto;

        // deshabilito el boton mientras prueba
        btnProbar.setEnabled(false);
        btnProbar.setText(R.string.btn_probando);

        // la red NUNCA va en el hilo principal (daria NetworkOnMainThreadException)
        new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean responde = Red.probar(ip, puertoFinal);
                final String fecha = Red.ahora();
                db.guardarChequeo(actual.getId(), responde, fecha);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (responde) {
                            actual.setEnLinea(1);
                        } else {
                            actual.setEnLinea(0);
                        }
                        actual.setUltimoChequeo(fecha);
                        mostrarEstado();
                        btnProbar.setText(R.string.btn_probar);
                        btnProbar.setEnabled(true);
                    }
                });
            }
        }).start();
    }

    // abre http://ip:puerto en el navegador
    private void abrirPanel() {
        if (actual == null || !actual.tieneIp()) {
            return;
        }
        int puerto = actual.getPuerto();
        if (puerto <= 0) {
            puerto = 80;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + actual.getIp() + ":" + puerto));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.msg_sin_navegador, Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmarEliminar() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialogo_titulo_eliminar)
                .setMessage(R.string.dialogo_msg_dispositivo)
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
                final int filas = db.eliminarDispositivo(id);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (filas > 0) {
                            Toast.makeText(DispositivoFormActivity.this, R.string.msg_dispositivo_eliminado, Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(DispositivoFormActivity.this, R.string.msg_error_bd, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }
}
