package com.example.neomi.protitulo;
public class Satellite {
    String satNum ;
    String azimuth ;
    String elevation ;
    String snr ;               // signal to noise ratio
    String prn ;                    // pseudo random number
    String used ;
    String type ;               // GPS, Glonass, IRNSS, Galileo, Baidou


      public Satellite(String satNum, String azimuth, String elevation, String snr, String prn, String used, String type) {
          this.azimuth = azimuth;
          this.elevation = elevation;
          this.snr = snr;
          this.prn = prn;
          this.used = used;
          this.type = type;

    }

    @Override
    public String toString() {
        return String.format("SateliteNum: %s Azimuth: %s Elev: %s SNR: %s PRN: %s Used: %s Type: %s", satNum, azimuth, elevation, snr, prn, used, type);
    }
    public String getSatNum() {
        return satNum;
    }

    public void setSatNum (String satNum) {this.satNum = satNum ;}

    public String getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(String azimuth) {
        this.azimuth = azimuth;
    }

    public String getElevation() {
        return elevation;
    }

    public void setElevation(String elevation) {
        this.elevation = elevation;
    }

    public String getSnr() {
        return snr;
    }

    public void setSnr(String snr) {
        this.snr = snr;
    }

    public String getPrn() {
        return prn;
    }

    public void setPrn(String prn) {
        this.prn = prn;
    }

    public String isUsed() {
        return used;
    }

    public void setUsed(String used) {
        this.used = used;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) { this.type = type;}





}
