package com.example.hometerminalgestion;

// Clase modelo: representa una fila de la tabla dispositivos
public class Dispositivo {

    private int id;
    private String nombre;
    private String tipo;
    private int puertoHdmi;      // 0 = no tiene puerto
    private int idHabitacion;    // 0 = sin habitacion
    private String habitacion;   // nombre de la habitacion (sale del LEFT JOIN), puede ser null
    private String ip;           // puede ser null
    private int puerto;          // puerto de red, 0 = no tiene
    private int enLinea;         // -1 = nunca verificado, 0 = sin respuesta, 1 = en linea
    private String ultimoChequeo; // fecha y hora del ultimo chequeo, puede ser null

    public Dispositivo(int id, String nombre, String tipo, int puertoHdmi, int idHabitacion,
                       String habitacion, String ip, int puerto, int enLinea, String ultimoChequeo) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.puertoHdmi = puertoHdmi;
        this.idHabitacion = idHabitacion;
        this.habitacion = habitacion;
        this.ip = ip;
        this.puerto = puerto;
        this.enLinea = enLinea;
        this.ultimoChequeo = ultimoChequeo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public int getPuertoHdmi() { return puertoHdmi; }
    public void setPuertoHdmi(int puertoHdmi) { this.puertoHdmi = puertoHdmi; }

    public int getIdHabitacion() { return idHabitacion; }
    public void setIdHabitacion(int idHabitacion) { this.idHabitacion = idHabitacion; }

    public String getHabitacion() { return habitacion; }
    public void setHabitacion(String habitacion) { this.habitacion = habitacion; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public int getPuerto() { return puerto; }
    public void setPuerto(int puerto) { this.puerto = puerto; }

    public int getEnLinea() { return enLinea; }
    public void setEnLinea(int enLinea) { this.enLinea = enLinea; }

    public String getUltimoChequeo() { return ultimoChequeo; }
    public void setUltimoChequeo(String ultimoChequeo) { this.ultimoChequeo = ultimoChequeo; }

    // true si el dispositivo tiene una IP cargada
    public boolean tieneIp() {
        return ip != null && !ip.isEmpty();
    }

    @Override
    public String toString() {
        return nombre + " - " + tipo;
    }
}
