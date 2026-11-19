package com.example.hometerminalgestion;

import java.net.ConnectException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;

// Funciones de red. OJO: probar() NO se puede llamar desde el hilo principal.
public class Red {

    // intenta abrir una conexion TCP a ip:puerto (espera maximo 2 segundos)
    public static boolean probar(String ip, int puerto) {
        return probar(ip, puerto, 2000);
    }

    // igual que la anterior, pero con el tiempo de espera elegido (en milisegundos)
    public static boolean probar(String ip, int puerto, int timeoutMs) {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(ip, puerto), timeoutMs);
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

    // devuelve los tres primeros numeros de la IP de este telefono (ej: "192.168.0")
    // o "" si no se pudo saber. Tampoco va en el hilo principal por las dudas.
    public static String prefijoLocal() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface interfaz = interfaces.nextElement();
                Enumeration<InetAddress> direcciones = interfaz.getInetAddresses();
                while (direcciones.hasMoreElements()) {
                    InetAddress direccion = direcciones.nextElement();
                    // me quedo con la primera IPv4 que no sea la de loopback (127.0.0.1)
                    if (direccion instanceof Inet4Address && !direccion.isLoopbackAddress()) {
                        String ip = direccion.getHostAddress();
                        return ip.substring(0, ip.lastIndexOf('.'));
                    }
                }
            }
        } catch (Exception e) {
            return "";
        }
        return "";
    }
}
