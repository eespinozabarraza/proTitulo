package com.example.neomi.protitulo;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Ubicacion {
    private String uid;
    private double latitud;
    private double longitud;
    private double altura;
    private float velocidad;
    private String actividad;
    private String confianza;
    public int starCount = 0;
    public Map<String, Boolean> stars = new HashMap<>();


    public Ubicacion(String uid, double latitud, double longitud, double altura, float velocidad, String actividad,
                     String confianza ) {
        this.uid = uid;
        this.latitud = latitud;
        this.longitud = longitud;
        this.altura = altura;
        this.velocidad = velocidad;
        this.actividad = actividad;
        this.confianza = confianza;

    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("latitud", latitud);
        result.put("longitud", longitud);
        result.put("altura", altura);
        result.put("velocidad", velocidad);
        result.put("actividad",actividad);
        result.put("confianza",confianza);
        result.put("starCount", starCount);
        result.put("stars", stars);

        return result;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public double getAltura() {
        return altura;
    }

    public void setAltura(double altura) {
        this.altura = altura;
    }

    public float getVelocidad() {
        return velocidad;
    }

    public void setVelocidad(float velocidad) {
        this.velocidad = velocidad;
    }

    public String getActividad() {return actividad; }

    public void setActividad(String actividad) {this.actividad = actividad;}

    public String getConfianza(){return confianza;}

    public void setConfianza(String confianza) {this.confianza = confianza;}


}

