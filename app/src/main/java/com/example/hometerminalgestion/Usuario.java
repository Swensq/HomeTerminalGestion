package com.example.hometerminalgestion;

// Clase modelo: representa una fila de la tabla usuarios
public class Usuario {

    // los dos roles posibles
    public static final String ROL_ADMIN = "admin";
    public static final String ROL_USUARIO = "usuario";

    private int id;
    private String usuario;
    private String nombre;
    private String email;
    private String password;
    private String rol;

    public Usuario(int id, String usuario, String nombre, String email, String password, String rol) {
        this.id = id;
        this.usuario = usuario;
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.rol = rol;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    @Override
    public String toString() {
        return nombre + " (" + usuario + ")";
    }
}
