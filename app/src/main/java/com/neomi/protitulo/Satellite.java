package com.neomi.protitulo;

import android.location.GnssStatus;

public class Satellite {
    private float azimuth = 0.0f;
    float elevation = 0.0f;
    private float snr = 0.0f;               // signal to noise ratio
    private int prn = 0;                    // pseudo random number
    boolean used = false;
    String type = "";               // GPS, Glonass, IRNSS, Galileo, Baidou

    private boolean validSatellite = false;
    private int signalStrength = 0;         // signal strength index
    public double x = 0;                   // cartesian coordinates
    public double y = 0;
    public double z = 0;

    @Override
    public String toString() {
        return "PRN: " + prn + " AZM: " + azimuth + " ALT: " + elevation + " SNR: " + snr +
                " x: " + x  + " y: " + y + " z: " + z;
    }

    Satellite (float azimuth, float elevation, float snr, int prn, boolean used) {
        this.azimuth = azimuth;
        this.elevation = elevation;
        this.snr = snr;
        this.prn = prn;
        this.used = used;
        setType();
        setSignalStrength();
    }

    Satellite (float azimuth, float elevation, float snr, int prn, boolean used, int type) {
        this.azimuth = azimuth;
        this.elevation = elevation;
        this.snr = snr;
        this.prn = prn;
        this.used = used;
        switch (type) {
            case GnssStatus.CONSTELLATION_GPS:
                this.type = "GPS"; break;
            case GnssStatus.CONSTELLATION_BEIDOU:
                this.type = "BEIDOU"; break;
            case GnssStatus.CONSTELLATION_GLONASS:
                this.type = "GLONASS"; break;
            case GnssStatus.CONSTELLATION_QZSS:
                this.type = "QZSS"; break;
            case GnssStatus.CONSTELLATION_GALILEO:
                this.type = "Galileo"; break;
            case GnssStatus.CONSTELLATION_SBAS:
                this.type = "SBAS"; break;
            default:
                this.type = "DEFENSE";
        }
        setSignalStrength();
    }

    public float getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(float azimuth) {
        this.azimuth = azimuth;
    }

    public float getElevation() {
        return elevation;
    }

    public void setElevation(float elevation) {
        this.elevation = elevation;
    }

    public float getSnr() {
        return snr;
    }

    public void setSnr(float snr) {
        this.snr = snr;
    }

    public int getPrn() {
        return prn;
    }

    public void setPrn(int prn) {
        this.prn = prn;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public String getType() {
        return type;
    }

    public void setType() {
        if ( (0 <= prn) && (prn <= 64) )
            this.type = "GPS";
        else if ( (65 <= prn) && (prn <= 96))
            this.type = "GLONASS";
        else
            this.type = "DEFENSE";
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public boolean isValidSatellite() {
        return validSatellite;
    }

    /* return boolean if the satellite data is valid */
    public void setValidSatellite() {
        validSatellite = true;
        if ( (azimuth < 0) || (azimuth > 360) ) validSatellite = false;
        else if ( (elevation < 0) || (elevation > 90) ) validSatellite = false;
        else if ( (prn < 0) || (prn > 1000) ) validSatellite = false;
        else if ( (snr < 0) || (snr > 100)) validSatellite = false;
        else if ( (Float.compare(azimuth, 0.0f) == 0) && (Float.compare(elevation, 0.0f) == 0) )
            validSatellite = false;
    }

    public void setSignalStrength() {
        signalStrength = Utils.setSatSignalStrength(snr);
    }
    public int getSignalStrength() {
        return signalStrength;
    }

}