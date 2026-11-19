package com.example.hometerminalgestion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HabitacionesActivity extends AppCompatActivity {

    ListView lista;
    TextView tvVacio;
    Button btnAgregar, btnVolver;
    DbHelper db;
    ArrayList<Habitacion> habitaciones = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_habitaciones);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        lista = findViewById(R.id.lista);
        tvVacio = findViewById(R.id.tvVacio);
        btnAgregar = findViewById(R.id.btnAgregar);
        btnVolver = findViewById(R.id.btnVolver);
        db = new DbHelper(this);

        // al tocar una habitacion abro el formulario para editarla
        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(HabitacionesActivity.this, HabitacionFormActivity.class);
                intent.putExtra("id", habitaciones.get(position).getId());
                startActivity(intent);
            }
        });

        btnAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HabitacionesActivity.this, HabitacionFormActivity.class);
                intent.putExtra("id", -1);
                startActivity(intent);
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

    private void cargarLista() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<Habitacion> resultado = db.listarHabitaciones();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        habitaciones = resultado;
                        ArrayList<String> textos = new ArrayList<>();
                        for (Habitacion h : habitaciones) {
                            String descripcion = h.getDescripcion();
                            if (descripcion == null || descripcion.isEmpty()) {
                                descripcion = getString(R.string.sin_descripcion);
                            }
                            textos.add(getString(R.string.item_habitacion, h.getNombre(), descripcion));
                        }
                        lista.setAdapter(new ArrayAdapter<String>(HabitacionesActivity.this,
                                android.R.layout.simple_list_item_1, textos));
                        if (habitaciones.isEmpty()) {
                            tvVacio.setVisibility(View.VISIBLE);
                        } else {
                            tvVacio.setVisibility(View.GONE);
                        }
                    }
                });
            }
        }).start();
    }
}
