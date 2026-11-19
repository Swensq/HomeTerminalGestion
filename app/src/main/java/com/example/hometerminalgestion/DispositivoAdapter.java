package com.example.hometerminalgestion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

// Adaptador propio: dibuja cada fila con un circulo de color y dos lineas de texto
public class DispositivoAdapter extends ArrayAdapter<Dispositivo> {

    public DispositivoAdapter(Context context, ArrayList<Dispositivo> dispositivos) {
        super(context, 0, dispositivos);
    }

    // se llama una vez por cada fila que se muestra
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // si no hay una fila para reusar, inflo una nueva
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_dispositivo, parent, false);
        }

        Dispositivo d = getItem(position);
        View vEstado = convertView.findViewById(R.id.vEstado);
        TextView tvLinea1 = convertView.findViewById(R.id.tvLinea1);
        TextView tvLinea2 = convertView.findViewById(R.id.tvLinea2);

        // color del circulo: verde = en linea, rojo = sin respuesta, gris = sin IP o sin verificar
        if (d.tieneIp() && d.getEnLinea() == 1) {
            vEstado.setBackgroundResource(R.drawable.circulo_verde);
        } else if (d.tieneIp() && d.getEnLinea() == 0) {
            vEstado.setBackgroundResource(R.drawable.circulo_rojo);
        } else {
            vEstado.setBackgroundResource(R.drawable.circulo_gris);
        }

        // primera linea: nombre y tipo
        tvLinea1.setText(getContext().getString(R.string.item_linea1, d.getNombre(), d.getTipo()));

        // segunda linea: habitacion, IP y ultimo chequeo
        String habitacion = d.getHabitacion();
        if (habitacion == null) {
            habitacion = getContext().getString(R.string.sin_habitacion);
        }
        if (d.tieneIp()) {
            String chequeo = d.getUltimoChequeo();
            if (chequeo == null) {
                chequeo = getContext().getString(R.string.sin_verificar);
            }
            tvLinea2.setText(getContext().getString(R.string.item_linea2_ip, habitacion, d.getIp(), chequeo));
        } else {
            tvLinea2.setText(getContext().getString(R.string.item_linea2_sin_ip, habitacion));
        }

        return convertView;
    }
}
