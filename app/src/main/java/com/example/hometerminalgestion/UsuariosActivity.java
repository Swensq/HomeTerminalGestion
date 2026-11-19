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

public class UsuariosActivity extends AppCompatActivity {

    ListView lista;
    TextView tvVacio;
    Button btnAgregar, btnVolver;
    DbHelper db;
    int idUsuarioLogueado;
    String rolLogueado;
    ArrayList<Usuario> usuarios = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_usuarios);
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
        idUsuarioLogueado = getIntent().getIntExtra("idUsuario", -1);
        rolLogueado = getIntent().getStringExtra("rol");

        // al tocar un usuario abro el formulario para editarlo
        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(UsuariosActivity.this, UsuarioFormActivity.class);
                intent.putExtra("id", usuarios.get(position).getId());
                intent.putExtra("idUsuario", idUsuarioLogueado);
                intent.putExtra("rol", rolLogueado);
                startActivity(intent);
            }
        });

        btnAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UsuariosActivity.this, UsuarioFormActivity.class);
                intent.putExtra("id", -1);
                intent.putExtra("idUsuario", idUsuarioLogueado);
                intent.putExtra("rol", rolLogueado);
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

    // recargo la lista cada vez que vuelvo a esta pantalla
    @Override
    protected void onResume() {
        super.onResume();
        cargarLista();
    }

    private void cargarLista() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<Usuario> resultado = db.listarUsuarios();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        usuarios = resultado;
                        // armo el texto de cada fila con el formato de strings.xml
                        ArrayList<String> textos = new ArrayList<>();
                        for (Usuario u : usuarios) {
                            // texto del rol segun strings.xml
                            String rol = getString(R.string.rol_usuario);
                            if (Usuario.ROL_ADMIN.equals(u.getRol())) {
                                rol = getString(R.string.rol_admin);
                            }
                            textos.add(getString(R.string.item_usuario, u.getNombre(), u.getUsuario(), u.getEmail(), rol));
                        }
                        lista.setAdapter(new ArrayAdapter<String>(UsuariosActivity.this,
                                android.R.layout.simple_list_item_1, textos));
                        if (usuarios.isEmpty()) {
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
