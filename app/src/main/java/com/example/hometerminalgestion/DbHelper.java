package com.example.hometerminalgestion;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NOMBRE = "hometerminal.db";
    private static final int DB_VERSION = 3;

    // consulta base de dispositivos: LEFT JOIN para traer el nombre de la habitacion
    // (si el dispositivo no tiene habitacion, el nombre viene null)
    private static final String SELECT_DISPOSITIVOS =
            "SELECT d.id, d.nombre, d.tipo, d.puerto_hdmi, d.id_habitacion, h.nombre, " +
            "d.ip, d.puerto, d.en_linea, d.ultimo_chequeo " +
            "FROM dispositivos d LEFT JOIN habitaciones h ON d.id_habitacion = h.id ";

    public DbHelper(Context context) {
        super(context, DB_NOMBRE, null, DB_VERSION);
    }

    // activo las claves foraneas (por defecto SQLite las ignora)
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // se ejecuta una sola vez, cuando se crea la base
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE usuarios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "usuario TEXT UNIQUE NOT NULL, " +
                "nombre TEXT NOT NULL, " +
                "email TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "rol TEXT NOT NULL)");

        db.execSQL("CREATE TABLE habitaciones (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT UNIQUE NOT NULL, " +
                "descripcion TEXT)");

        db.execSQL("CREATE TABLE dispositivos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT UNIQUE NOT NULL, " +
                "tipo TEXT NOT NULL, " +
                "puerto_hdmi INTEGER, " +
                "id_habitacion INTEGER REFERENCES habitaciones(id), " +
                "ip TEXT, " +
                "puerto INTEGER, " +
                "en_linea INTEGER, " +
                "ultimo_chequeo TEXT)");

        // usuario administrador de ejemplo para poder entrar la primera vez
        db.execSQL("INSERT INTO usuarios (usuario, nombre, email, password, rol) " +
                "VALUES ('admin', 'Alex', 'admin@hometerminal.com', 'admin123', '" + Usuario.ROL_ADMIN + "')");

        // habitaciones de ejemplo (como la tabla es nueva, los ids son 1, 2 y 3)
        db.execSQL("INSERT INTO habitaciones (nombre, descripcion) VALUES ('Living', 'Sala principal')");
        db.execSQL("INSERT INTO habitaciones (nombre, descripcion) VALUES ('Cocina', NULL)");
        db.execSQL("INSERT INTO habitaciones (nombre, descripcion) VALUES ('Dormitorio', NULL)");

        // dispositivos de ejemplo para que la lista no arranque vacia
        db.execSQL("INSERT INTO dispositivos (nombre, tipo, puerto_hdmi, id_habitacion) " +
                "VALUES ('TV Living', 'TV', NULL, 1)");
        db.execSQL("INSERT INTO dispositivos (nombre, tipo, puerto_hdmi, id_habitacion) " +
                "VALUES ('PlayStation 5', 'Consola', 1, 1)");
        db.execSQL("INSERT INTO dispositivos (nombre, tipo, puerto_hdmi, id_habitacion) " +
                "VALUES ('WiiM Mini', 'Audio', NULL, 2)");
        // este funciona en cualquier lugar con internet, sirve para la demo
        db.execSQL("INSERT INTO dispositivos (nombre, tipo, ip, puerto) " +
                "VALUES ('DNS de Google (prueba)', 'Otro', '8.8.8.8', 53)");
    }

    // al cambiar la version borro todo y vuelvo a crear (alcanza para el TP)
    @Override
    public void onUpgrade(SQLiteDatabase db, int versionVieja, int versionNueva) {
        db.execSQL("DROP TABLE IF EXISTS dispositivos");
        db.execSQL("DROP TABLE IF EXISTS habitaciones");
        db.execSQL("DROP TABLE IF EXISTS usuarios");
        onCreate(db);
    }

    // usuarios

    private Usuario leerUsuario(Cursor c) {
        return new Usuario(c.getInt(0), c.getString(1), c.getString(2), c.getString(3),
                c.getString(4), c.getString(5));
    }

    // devuelve el id nuevo, o -1 si fallo
    public long insertarUsuario(Usuario u) {
        SQLiteDatabase db = getWritableDatabase();
        return db.insert("usuarios", null, valoresDeUsuario(u));
    }

    // devuelve la cantidad de filas modificadas
    public int actualizarUsuario(Usuario u) {
        SQLiteDatabase db = getWritableDatabase();
        return db.update("usuarios", valoresDeUsuario(u), "id = ?", new String[]{String.valueOf(u.getId())});
    }

    private ContentValues valoresDeUsuario(Usuario u) {
        ContentValues valores = new ContentValues();
        valores.put("usuario", u.getUsuario());
        valores.put("nombre", u.getNombre());
        valores.put("email", u.getEmail());
        valores.put("password", u.getPassword());
        valores.put("rol", u.getRol());
        return valores;
    }

    public int eliminarUsuario(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete("usuarios", "id = ?", new String[]{String.valueOf(id)});
    }

    public int contarUsuarios() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM usuarios", null);
        c.moveToFirst();
        int cantidad = c.getInt(0);
        c.close();
        return cantidad;
    }

    public ArrayList<Usuario> listarUsuarios() {
        ArrayList<Usuario> lista = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, usuario, nombre, email, password, rol FROM usuarios ORDER BY nombre", null);
        while (c.moveToNext()) {
            lista.add(leerUsuario(c));
        }
        c.close();
        return lista;
    }

    // devuelve null si no existe
    public Usuario obtenerUsuario(int id) {
        Usuario u = null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, usuario, nombre, email, password, rol FROM usuarios WHERE id = ?",
                new String[]{String.valueOf(id)});
        if (c.moveToFirst()) {
            u = leerUsuario(c);
        }
        c.close();
        return u;
    }

    // excluirId sirve al editar: el usuario no se compara consigo mismo (-1 = no excluir a nadie)
    public boolean existeUsuario(String usuario, int excluirId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id FROM usuarios WHERE usuario = ? AND id <> ?",
                new String[]{usuario, String.valueOf(excluirId)});
        boolean existe = c.getCount() > 0;
        c.close();
        return existe;
    }

    public boolean existeUsuario(String usuario) {
        return existeUsuario(usuario, -1);
    }

    public boolean existeEmail(String email, int excluirId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id FROM usuarios WHERE email = ? AND id <> ?",
                new String[]{email, String.valueOf(excluirId)});
        boolean existe = c.getCount() > 0;
        c.close();
        return existe;
    }

    public boolean existeEmail(String email) {
        return existeEmail(email, -1);
    }

    // devuelve el usuario si los datos son correctos, o null si no
    public Usuario validarLogin(String usuario, String password) {
        Usuario u = null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, usuario, nombre, email, password, rol FROM usuarios " +
                "WHERE usuario = ? AND password = ?", new String[]{usuario, password});
        if (c.moveToFirst()) {
            u = leerUsuario(c);
        }
        c.close();
        return u;
    }

    // habitaciones

    public long insertarHabitacion(Habitacion h) {
        SQLiteDatabase db = getWritableDatabase();
        return db.insert("habitaciones", null, valoresDeHabitacion(h));
    }

    public int actualizarHabitacion(Habitacion h) {
        SQLiteDatabase db = getWritableDatabase();
        return db.update("habitaciones", valoresDeHabitacion(h), "id = ?",
                new String[]{String.valueOf(h.getId())});
    }

    public int eliminarHabitacion(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete("habitaciones", "id = ?", new String[]{String.valueOf(id)});
    }

    private ContentValues valoresDeHabitacion(Habitacion h) {
        ContentValues valores = new ContentValues();
        valores.put("nombre", h.getNombre());
        valores.put("descripcion", h.getDescripcion());
        return valores;
    }

    public ArrayList<Habitacion> listarHabitaciones() {
        ArrayList<Habitacion> lista = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, nombre, descripcion FROM habitaciones ORDER BY nombre", null);
        while (c.moveToNext()) {
            lista.add(new Habitacion(c.getInt(0), c.getString(1), c.getString(2)));
        }
        c.close();
        return lista;
    }

    public Habitacion obtenerHabitacion(int id) {
        Habitacion h = null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, nombre, descripcion FROM habitaciones WHERE id = ?",
                new String[]{String.valueOf(id)});
        if (c.moveToFirst()) {
            h = new Habitacion(c.getInt(0), c.getString(1), c.getString(2));
        }
        c.close();
        return h;
    }

    public boolean existeHabitacion(String nombre, int excluirId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id FROM habitaciones WHERE nombre = ? AND id <> ?",
                new String[]{nombre, String.valueOf(excluirId)});
        boolean existe = c.getCount() > 0;
        c.close();
        return existe;
    }

    // cuantos dispositivos tiene una habitacion (para no borrarla si tiene)
    public int contarDispositivosDeHabitacion(int idHabitacion) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM dispositivos WHERE id_habitacion = ?",
                new String[]{String.valueOf(idHabitacion)});
        c.moveToFirst();
        int cantidad = c.getInt(0);
        c.close();
        return cantidad;
    }

    // dispositivos

    private Dispositivo leerDispositivo(Cursor c) {
        // getInt devuelve 0 cuando el valor es NULL
        int enLinea = -1; // -1 = nunca verificado
        if (!c.isNull(8)) {
            enLinea = c.getInt(8);
        }
        return new Dispositivo(c.getInt(0), c.getString(1), c.getString(2), c.getInt(3),
                c.getInt(4), c.getString(5), c.getString(6), c.getInt(7), enLinea, c.getString(9));
    }

    public long insertarDispositivo(Dispositivo d) {
        SQLiteDatabase db = getWritableDatabase();
        return db.insert("dispositivos", null, valoresDeDispositivo(d));
    }

    public int actualizarDispositivo(Dispositivo d) {
        SQLiteDatabase db = getWritableDatabase();
        return db.update("dispositivos", valoresDeDispositivo(d), "id = ?",
                new String[]{String.valueOf(d.getId())});
    }

    public int eliminarDispositivo(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete("dispositivos", "id = ?", new String[]{String.valueOf(id)});
    }

    // arma los valores a guardar (los datos vacios se guardan como NULL)
    private ContentValues valoresDeDispositivo(Dispositivo d) {
        ContentValues valores = new ContentValues();
        valores.put("nombre", d.getNombre());
        valores.put("tipo", d.getTipo());
        if (d.getPuertoHdmi() > 0) {
            valores.put("puerto_hdmi", d.getPuertoHdmi());
        } else {
            valores.putNull("puerto_hdmi");
        }
        if (d.getIdHabitacion() > 0) {
            valores.put("id_habitacion", d.getIdHabitacion());
        } else {
            valores.putNull("id_habitacion");
        }
        if (d.tieneIp()) {
            valores.put("ip", d.getIp());
            valores.put("puerto", d.getPuerto());
        } else {
            valores.putNull("ip");
            valores.putNull("puerto");
        }
        if (d.getEnLinea() >= 0) {
            valores.put("en_linea", d.getEnLinea());
        } else {
            valores.putNull("en_linea");
        }
        valores.put("ultimo_chequeo", d.getUltimoChequeo());
        return valores;
    }

    // guarda solo el resultado de un chequeo de red
    public int guardarChequeo(int id, boolean enLinea, String fecha) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        if (enLinea) {
            valores.put("en_linea", 1);
        } else {
            valores.put("en_linea", 0);
        }
        valores.put("ultimo_chequeo", fecha);
        return db.update("dispositivos", valores, "id = ?", new String[]{String.valueOf(id)});
    }

    public ArrayList<Dispositivo> listarDispositivos() {
        ArrayList<Dispositivo> lista = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(SELECT_DISPOSITIVOS + "ORDER BY d.nombre", null);
        while (c.moveToNext()) {
            lista.add(leerDispositivo(c));
        }
        c.close();
        return lista;
    }

    public Dispositivo obtenerDispositivo(int id) {
        Dispositivo d = null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(SELECT_DISPOSITIVOS + "WHERE d.id = ?", new String[]{String.valueOf(id)});
        if (c.moveToFirst()) {
            d = leerDispositivo(c);
        }
        c.close();
        return d;
    }

    public boolean existeDispositivo(String nombre, int excluirId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id FROM dispositivos WHERE nombre = ? AND id <> ?",
                new String[]{nombre, String.valueOf(excluirId)});
        boolean existe = c.getCount() > 0;
        c.close();
        return existe;
    }

    // resumen para el menu

    public int contarDispositivos() {
        return contarNumero("SELECT COUNT(*) FROM dispositivos");
    }

    // dispositivos que respondieron en el ultimo chequeo
    public int contarEnLinea() {
        return contarNumero("SELECT COUNT(*) FROM dispositivos WHERE en_linea = 1");
    }

    // dispositivos que ya se verificaron alguna vez
    public int contarVerificados() {
        return contarNumero("SELECT COUNT(*) FROM dispositivos WHERE en_linea IS NOT NULL");
    }

    public int contarSinHabitacion() {
        return contarNumero("SELECT COUNT(*) FROM dispositivos WHERE id_habitacion IS NULL");
    }

    private int contarNumero(String sql) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(sql, null);
        c.moveToFirst();
        int cantidad = c.getInt(0);
        c.close();
        return cantidad;
    }

    // una fila por habitacion: {nombre, cantidad de dispositivos} (GROUP BY)
    public ArrayList<String[]> resumenPorHabitacion() {
        ArrayList<String[]> lista = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT h.nombre, COUNT(d.id) FROM habitaciones h " +
                "LEFT JOIN dispositivos d ON d.id_habitacion = h.id " +
                "GROUP BY h.id ORDER BY h.nombre", null);
        while (c.moveToNext()) {
            lista.add(new String[]{c.getString(0), String.valueOf(c.getInt(1))});
        }
        c.close();
        return lista;
    }
}
