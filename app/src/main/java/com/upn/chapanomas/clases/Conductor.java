package com.upn.chapanomas.clases;

public class Conductor {
    String id;
    String nombre;
    String email;
    String tipoMoto;
    String placa;

    public Conductor() {
    }

    public Conductor(String id, String nombre, String email, String tipoMoto, String placa) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.tipoMoto = tipoMoto;
        this.placa = placa;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTipoMoto() {
        return tipoMoto;
    }

    public void setTipoMoto(String tipoMoto) {
        this.tipoMoto = tipoMoto;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }
}
