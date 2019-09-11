package com.neomi.protitulo;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
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
    private float mazimuth;
    private float X;
    private float Y;
    private float Z;
    private int CantSat;
    private ArrayList<Satellite> listaSatelites;

    private String positionDop;
    private String horizontalDop;
    private String verticalDop;
    private String geoidHeight;
    private String ageOfGpsData;
    private String antennaAltitude;
    private String temperatura;

    public int starCount = 0;
    public Map<String, Boolean> stars = new HashMap<>();


    public Ubicacion(String uid, String date, double latitud, double longitud, double altura, float velocidad, String actividad,
                     String confianza, float mazimuth, float X, float Y, float Z, int CantSat, String positionDop, String horizontalDop, String verticalDop, String geoidHeight, String ageOfGpsData, String antennaAltitude, ArrayList<Satellite> listaSatelites, String temperatura) {
        this.uid = uid;
        this.date = date;
        this.latitud = latitud;
        this.longitud = longitud;
        this.altura = altura;
        this.velocidad = velocidad;
        this.actividad = actividad;
        this.confianza = confianza;
        this.mazimuth = mazimuth;
        this.X = X;
        this.Y = Y;
        this.Z = Z;
        this.CantSat = CantSat;
        this.positionDop = positionDop;
        this.horizontalDop = horizontalDop;
        this.verticalDop = verticalDop;
        this.geoidHeight = geoidHeight;
        this.ageOfGpsData = ageOfGpsData;
        this.antennaAltitude = antennaAltitude;
        this.listaSatelites = listaSatelites;
        this.temperatura = temperatura;
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
        result.put("azimuth", mazimuth);
        result.put("X", X);
        result.put("Y", Y);
        result.put("Z", Z);
        result.put("CantSat", CantSat);
        result.put("starCount", starCount);
        result.put("stars", stars);
        result.put("positionDop",positionDop);
        result.put("horizontalDop",horizontalDop);
        result.put("verticalDop",verticalDop);
        result.put("geoidHeight",geoidHeight);
        result.put("ageOfGpsData",ageOfGpsData);
        result.put("antennaAltitude",antennaAltitude);
        result.put("listaSatelite", listaSatelites);
        result.put("temperatura", temperatura);

        return result;
    }

    public String getUid() {return uid;}

    public void setUid(String uid) {this.uid = uid;}

    public String getDate(){return date;}

    public void setDate (String date) {this.date = date;}

    public double getLatitud() {        return latitud;    }

    public void setLatitud(double latitud) {        this.latitud = latitud;    }

    public double getLongitud() {        return longitud;    }

    public void setLongitud(double longitud) {        this.longitud = longitud;    }

    public double getAltura() {        return altura;    }

    public void setAltura(double altura) {        this.altura = altura;    }

    public float getVelocidad() {        return velocidad;    }

    public void setVelocidad(float velocidad) {        this.velocidad = velocidad;    }

    public String getActividad() {return actividad; }

    public void setActividad(String actividad) {this.actividad = actividad;}

    public String getConfianza(){return confianza;}

    public void setConfianza(String confianza) {this.confianza = confianza;}

    public float getAzimuth(){return mazimuth;}

    public void setAzimuth(Float azimuth) {this.mazimuth = azimuth;}

    public float getX(){return X;}

    public void setX(float X) {this.X = X;}

    public float getY(){return Y;}

    public void setY(float Y) {this.Y = Y;}

    public float getZ(){return Z;}

    public void setZ(float Z) {this.Z = Z;}

    public int getCantSat() {        return CantSat;    }

    public void setCantSat(int CantSat) {        this.CantSat = CantSat;    }

    public ArrayList<Satellite> getlistaSatelites() {        return listaSatelites;    }

    public void setlistaSatelites(ArrayList<Satellite> listaSatelites) {        this.listaSatelites = listaSatelites;    }


    public String getPositionDop() {
        return positionDop;
    }

    public void setPositionDop(String positionDop) {
        this.positionDop = positionDop;
    }

    public String getHorizontalDop() {
        return horizontalDop;
    }

    public void setHorizontalDop(String horizontalDop) {
        this.horizontalDop = horizontalDop;
    }

    public String getVerticalDop() {
        return verticalDop;
    }

    public void setVerticalDop(String verticalDop) {
        this.verticalDop = verticalDop;
    }

    public String getGeoidHeight() {
        return geoidHeight;
    }

    public void setGeoidHeight(String geoidHeight) {
        this.geoidHeight = geoidHeight;
    }

    public String getAgeOfGpsData() {
        return ageOfGpsData;
    }

    public void setAgeOfGpsData(String ageOfGpsData) {
        this.ageOfGpsData = ageOfGpsData;
    }

    public String getAntennaAltitude() {
        return antennaAltitude;
    }

    public void setAntennaAltitude(String antennaAltitude) {
        this.antennaAltitude = antennaAltitude;
    }

    public String getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(String temperatura) {
        this.temperatura = temperatura;
    }
}

