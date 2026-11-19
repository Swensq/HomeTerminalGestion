package com.example.hometerminalgestion;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// Funciones de red. OJO: probar() NO se puede llamar desde el hilo principal.
public class Red {

    // intenta abrir una conexion TCP a ip:puerto (espera maximo 2 segundos)
    public static boolean probar(String ip, int puerto) {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(ip, puerto), 2000);
            return true;   // se pudo conectar: el dispositivo responde
        } catch (ConnectException e) {
            // "Connection refused" quiere decir que el dispositivo esta prendido y contesto,
            // solo que ese puerto esta cerrado. Lo cuento como en linea.
            // Otros ConnectException (EHOSTUNREACH, ENETUNREACH) son que no hay camino al equipo.
            String mensaje = String.valueOf(e.getMessage());
            return mensaje.contains("ECONNREFUSED") || mensaje.contains("refused");
        } catch (Exception e) {
            return false;  // timeout u otro error: sin respuesta
        } finally {
            // siempre cierro el socket
            try {
                socket.close();
            } catch (Exception e) {
                // no hago nada
            }
        }
    }

    // fecha y hora actual como texto, para guardar en la base
    public static String ahora() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
    }
}
