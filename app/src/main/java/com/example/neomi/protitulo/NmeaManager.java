package com.example.neomi.protitulo;

public class NmeaManager {
    String[] nmeaParts;

    public NmeaManager() {

    }

    public static boolean IsNullOrEmpty(String text){
        return text == null ||  text.trim().length() == 0;
    }

    public NmeaManager(String nmeaManager){

        if(IsNullOrEmpty(nmeaManager)){
            nmeaParts = new String[]{""};
            return;
        }
        nmeaParts = nmeaManager.split(",");

    }

    private boolean isGGA() {
        return nmeaParts[0].toUpperCase().contains("GGA");
    }

    private boolean isGSA() {
        return nmeaParts[0].toUpperCase().contains("GSA");
    }

    public boolean isLocationSentence(){
        return isGSA() || isGGA();
    }


    public String getLatestPdop(){
        if (isGSA()) {

            if (nmeaParts.length > 15 && !IsNullOrEmpty(nmeaParts[15])) {
                return nmeaParts[15];
            }
        }

        return null;
    }

    public String getLatestVdop(){
        if (isGSA()) {
            if (nmeaParts.length > 17 &&!IsNullOrEmpty(nmeaParts[17]) && !nmeaParts[17].startsWith("*")) {
                return nmeaParts[17].split("\\*")[0];
            }
        }

        return null;
    }

    public String getLatestHdop(){
        if (isGGA()) {
            if (nmeaParts.length > 8 &&!IsNullOrEmpty(nmeaParts[8])) {
                return nmeaParts[8];
            }
        }
        else if (isGSA()) {
            if (nmeaParts.length > 16 &&!IsNullOrEmpty(nmeaParts[16])) {
                return nmeaParts[16];
            }
        }

        return null;
    }

    public String getGeoIdHeight(){
        if (isGGA()) {
            if (nmeaParts.length > 11 &&!IsNullOrEmpty(nmeaParts[11])) {
                return nmeaParts[11];
            }
        }

        return null;
    }

    public String getAgeOfDgpsData(){
        if (isGGA()) {
            if (nmeaParts.length > 13 && !IsNullOrEmpty(nmeaParts[13])) {
                return nmeaParts[13];
            }
        }

        return null;
    }

    public String getDgpsId(){
        if (isGGA()) {
            if (nmeaParts.length > 14 &&!IsNullOrEmpty(nmeaParts[14]) && !nmeaParts[14].startsWith("*")) {
                return nmeaParts[14].split("\\*")[0];
            }
        }

        return null;
    }
}
