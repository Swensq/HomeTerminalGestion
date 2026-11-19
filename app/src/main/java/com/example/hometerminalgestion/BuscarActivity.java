package com.example.hometerminalgestion;

import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class BuscarActivity extends AppCompatActivity {

    // cantidad de hilos que buscan al mismo tiempo
    private static final int HILOS = 20;

    EditText etRed;
    TextView tvProgreso, tvResultado;
    ProgressBar progreso;
    ListView lista;
    Button btnBuscar, btnAgregar, btnVolver;
    DbHelper db;

    // IPs que se muestran en la lista (las que respondieron y todavia no estan guardadas)
    ArrayList<String> ips = new ArrayList<>();

    boolean cerrada = false; // pasa a true cuando se cierra la pantalla, para que los hilos paren

    // datos que comparten los 20 hilos (siempre se tocan dentro de synchronized (candado))
    final Object candado = new Object();
    ArrayList<Integer> encontradas = new ArrayList<>(); // ultimos numeros de las IP que respondieron
    int revisadas = 0;  // cuantas direcciones ya se revisaron
    int terminados = 0; // cuantos hilos ya terminaron

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_buscar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etRed = findViewById(R.id.etRed);
        tvProgreso = findViewById(R.id.tvProgreso);
        tvResultado = findViewById(R.id.tvResultado);
        progreso = findViewById(R.id.progreso);
        lista = findViewById(R.id.lista);
        btnBuscar = findViewById(R.id.btnBuscar);
        btnAgregar = findViewById(R.id.btnAgregar);
        btnVolver = findViewById(R.id.btnVolver);
        db = new DbHelper(this);

        // la lista permite marcar varios elementos con checkbox
        lista.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // pongo la red de este telefono como valor inicial (en un hilo por las dudas)
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String prefijo = Red.prefijoLocal();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!cerrada) {
                            etRed.setText(prefijo);
                        }
                    }
                });
            }
        }).start();

        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buscar();
            }
        });

        btnAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarSeleccionados();
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
    protected void onDestroy() {
        super.onDestroy();
        cerrada = true; // aviso a los hilos que paren
    }

    // revisa que sean tres numeros del 0 al 255 separados por puntos
    private boolean prefijoValido(String prefijo) {
        if (prefijo.endsWith(".")) {
            return false;
        }
        String[] partes = prefijo.split("\\.");
        if (partes.length != 3) {
            return false;
        }
        for (String parte : partes) {
            try {
                int numero = Integer.parseInt(parte);
                if (numero < 0 || numero > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    private void buscar() {
        final String prefijo = etRed.getText().toString().trim();
        if (!prefijoValido(prefijo)) {
            etRed.setError(getString(R.string.error_red));
            etRed.requestFocus();
            return;
        }

        // limpio la pantalla y deshabilito los botones mientras busca
        ips = new ArrayList<>();
        lista.setAdapter(null);
        tvResultado.setVisibility(View.GONE);
        btnBuscar.setEnabled(false);
        btnAgregar.setEnabled(false);
        progreso.setProgress(0);
        progreso.setVisibility(View.VISIBLE);
        tvProgreso.setText(getString(R.string.buscando, 0, 254));
        tvProgreso.setVisibility(View.VISIBLE);

        synchronized (candado) {
            encontradas = new ArrayList<>();
            revisadas = 0;
            terminados = 0;
        }

        // Como funciona: hay 20 hilos, numerados de 0 a 19.
        // El hilo k revisa las direcciones i (de 1 a 254) donde i % 20 == k,
        // asi las 254 direcciones se reparten entre todos y se revisan al mismo tiempo.
        for (int k = 0; k < HILOS; k++) {
            final int numeroHilo = k;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    revisarDirecciones(prefijo, numeroHilo);
                }
            }).start();
        }
    }

    // trabajo de un hilo: revisa sus direcciones y avisa el progreso
    private void revisarDirecciones(String prefijo, int numeroHilo) {
        for (int i = 1; i <= 254; i++) {
            if (i % HILOS != numeroHilo) {
                continue; // esta direccion le toca a otro hilo
            }
            if (cerrada) {
                break;
            }

            // pruebo el puerto 80 con poco tiempo de espera (400 ms)
            boolean responde = Red.probar(prefijo + "." + i, 80, 400);

            // las variables compartidas se tocan de a un hilo por vez
            int hechas;
            synchronized (candado) {
                if (responde) {
                    encontradas.add(i);
                }
                revisadas++;
                hechas = revisadas;
            }

            final int hechasFinal = hechas;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!cerrada) {
                        progreso.setProgress(hechasFinal);
                        tvProgreso.setText(getString(R.string.buscando, hechasFinal, 254));
                    }
                }
            });
        }

        // aviso que este hilo termino; el ultimo en terminar muestra el resultado
        boolean esElUltimo;
        synchronized (candado) {
            terminados++;
            esElUltimo = (terminados == HILOS);
        }
        if (esElUltimo && !cerrada) {
            armarResultado(prefijo);
        }
    }

    // lo ejecuta el ultimo hilo: ordena las IP, saca las que ya estan guardadas y las muestra
    private void armarResultado(String prefijo) {
        ArrayList<Integer> copia;
        synchronized (candado) {
            copia = new ArrayList<>(encontradas);
        }
        Collections.sort(copia);

        ArrayList<String> guardadas = db.listarIps();
        final ArrayList<String> nuevas = new ArrayList<>();
        for (int numero : copia) {
            String ip = prefijo + "." + numero;
            if (!guardadas.contains(ip)) {
                nuevas.add(ip);
            }
        }
        final int total = copia.size();
        final int yaAgregadas = total - nuevas.size();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (cerrada) {
                    return;
                }
                ips = nuevas;
                lista.setAdapter(new ArrayAdapter<String>(BuscarActivity.this,
                        android.R.layout.simple_list_item_multiple_choice, ips));
                progreso.setVisibility(View.GONE);
                tvProgreso.setVisibility(View.GONE);
                btnBuscar.setEnabled(true);
                btnAgregar.setEnabled(true);

                if (total == 0) {
                    tvResultado.setText(R.string.msg_nada_encontrado);
                } else {
                    tvResultado.setText(getString(R.string.encontrados, total, yaAgregadas));
                }
                tvResultado.setVisibility(View.VISIBLE);
            }
        });
    }

    private void agregarSeleccionados() {
        // junto las IP que estan marcadas en la lista
        final ArrayList<String> elegidas = new ArrayList<>();
        SparseBooleanArray marcadas = lista.getCheckedItemPositions();
        for (int i = 0; i < ips.size(); i++) {
            if (marcadas.get(i)) {
                elegidas.add(ips.get(i));
            }
        }
        if (elegidas.isEmpty()) {
            Toast.makeText(this, R.string.msg_nada_seleccionado, Toast.LENGTH_SHORT).show();
            return;
        }

        final String tipo = getString(R.string.tipo_otro);
        btnAgregar.setEnabled(false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                int agregados = 0;
                for (String ip : elegidas) {
                    String nombre = getString(R.string.nombre_dispositivo_red, ip);
                    // si ya existe ese nombre, lo salteo
                    if (!db.existeDispositivo(nombre, -1)) {
                        Dispositivo d = new Dispositivo(0, nombre, tipo, 0, 0, null, ip, 80, 1, Red.ahora());
                        if (db.insertarDispositivo(d) > 0) {
                            agregados++;
                        }
                    }
                }
                final int cantidad = agregados;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (cerrada) {
                            return;
                        }
                        Toast.makeText(BuscarActivity.this,
                                getString(R.string.msg_dispositivos_agregados, cantidad),
                                Toast.LENGTH_SHORT).show();
                        finish(); // la lista de dispositivos se recarga en onResume
                    }
                });
            }
        }).start();
    }
}
