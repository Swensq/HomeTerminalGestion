package com.example.hometerminalgestion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DispositivosActivity extends AppCompatActivity {

    ListView lista;
    TextView tvVacio, tvProgreso;
    ProgressBar progreso;
    Button btnAgregar, btnVerificar, btnVolver;
    DbHelper db;
    ArrayList<Dispositivo> dispositivos = new ArrayList<>();
    boolean cerrada = false; // pasa a true cuando se cierra la pantalla, para que el hilo pare

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dispositivos);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        lista = findViewById(R.id.lista);
        tvVacio = findViewById(R.id.tvVacio);
        tvProgreso = findViewById(R.id.tvProgreso);
        progreso = findViewById(R.id.progreso);
        btnAgregar = findViewById(R.id.btnAgregar);
        btnVerificar = findViewById(R.id.btnVerificar);
        btnVolver = findViewById(R.id.btnVolver);
        db = new DbHelper(this);

        // al tocar un dispositivo abro el formulario para editarlo
        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(DispositivosActivity.this, DispositivoFormActivity.class);
                intent.putExtra("id", dispositivos.get(position).getId());
                startActivity(intent);
            }
        });

        btnAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DispositivosActivity.this, DispositivoFormActivity.class);
                intent.putExtra("id", -1);
                startActivity(intent);
            }
        });

        btnVerificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verificarTodos();
            }
        });

        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarLista();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cerrada = true; // aviso al hilo de verificacion que pare
    }

    private void cargarLista() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<Dispositivo> resultado = db.listarDispositivos();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dispositivos = resultado;
                        lista.setAdapter(new DispositivoAdapter(DispositivosActivity.this, dispositivos));
                        if (dispositivos.isEmpty()) {
                            tvVacio.setVisibility(View.VISIBLE);
                        } else {
                            tvVacio.setVisibility(View.GONE);
                        }
                    }
                });
            }
        }).start();
    }

    // verifica uno por uno los dispositivos que tienen IP
    private void verificarTodos() {
        // armo la lista de los que tienen IP (esto es rapido, no usa la red)
        final ArrayList<Dispositivo> aVerificar = new ArrayList<>();
        for (Dispositivo d : dispositivos) {
            if (d.tieneIp()) {
                aVerificar.add(d);
            }
        }
        if (aVerificar.isEmpty()) {
            Toast.makeText(this, R.string.msg_sin_ips, Toast.LENGTH_SHORT).show();
            return;
        }

        // deshabilito los botones y muestro la barra de progreso
        final int total = aVerificar.size();
        btnVerificar.setEnabled(false);
        btnAgregar.setEnabled(false);
        progreso.setMax(total);
        progreso.setProgress(0);
        progreso.setVisibility(View.VISIBLE);
        tvProgreso.setVisibility(View.VISIBLE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                int enLinea = 0;
                for (int i = 0; i < total; i++) {
                    // si cerraron la pantalla, corto el trabajo
                    if (cerrada) {
                        return;
                    }
                    Dispositivo d = aVerificar.get(i);

                    // actualizo el texto y la barra
                    final int actual = i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progreso.setProgress(actual);
                            tvProgreso.setText(getString(R.string.verificando, actual + 1, total));
                        }
                    });

                    // pruebo la conexion (si no tiene puerto uso el 80) y guardo el resultado
                    int puerto = d.getPuerto();
                    if (puerto <= 0) {
                        puerto = 80;
                    }
                    boolean responde = Red.probar(d.getIp(), puerto);
                    db.guardarChequeo(d.getId(), responde, Red.ahora());
                    if (responde) {
                        enLinea++;
                    }
                }

                final int cantidadEnLinea = enLinea;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (cerrada) {
                            return;
                        }
                        // termino: vuelvo a habilitar todo y recargo la lista
                        progreso.setProgress(total);
                        progreso.setVisibility(View.GONE);
                        tvProgreso.setVisibility(View.GONE);
                        btnVerificar.setEnabled(true);
                        btnAgregar.setEnabled(true);
                        cargarLista();
                        Toast.makeText(DispositivosActivity.this,
                                getString(R.string.msg_resultado_verificacion, cantidadEnLinea, total),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }
}
