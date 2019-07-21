package com.example.neomi.protitulo;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Ubicacion {
    private String uid;
    private String date;
    private double latitud;
    private double longitud;
    private double altura;
    private float velocidad;
    private String actividad;
    private String confianza;
    private float azimuth;
    private float X;
    private float Y;
    private float Z;
    public int starCount = 0;
    public Map<String, Boolean> stars = new HashMap<>();


    public Ubicacion(String uid , String date, double latitud, double longitud, double altura, float velocidad, String actividad,
                     String confianza, float azimuth,float X, float Y, float Z ) {
        this.uid = uid;
        this.date = date;
        this.latitud = latitud;
        this.longitud = longitud;
        this.altura = altura;
        this.velocidad = velocidad;
        this.actividad = actividad;
        this.confianza = confianza;
        this.azimuth = azimuth;
        this.X = X;
        this.Y = Y;
        this.Z = Z;

    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("date",date);
        result.put("latitud", latitud);
        result.put("longitud", longitud);
        result.put("altura", altura);
        result.put("velocidad", velocidad);
        result.put("actividad",actividad);
        result.put("confianza",confianza);
        result.put("azimuth", azimuth);
        result.put("X", X);
        result.put("Y", Y);
        result.put("Z", Z);
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

    public String getDate(){return date;}

    public void setDate (String date) {this.date = date;}

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

    public float getAzimuth(){return azimuth;}

    public void setAzimuth(Float azimuth) {this.azimuth = azimuth;}

    public float getX(){return X;}

    public void setX(float X) {this.X = X;}

    public float getY(){return Y;}

    public void setY(float Y) {this.Y = Y;}

    public float getZ(){return Z;}

    public void setZ(float Z) {this.Z = Z;}


}

