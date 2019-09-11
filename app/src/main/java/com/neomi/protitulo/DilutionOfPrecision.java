package com.neomi.protitulo;

public class DilutionOfPrecision {

    private double positionDop;
    private double horizontalDop;
    private double verticalDop;
    private String geoidHeight;
    private String ageOfGpsData;
    private String antennaAltitude;

    DilutionOfPrecision(double positionDop, double horizontalDop, double verticalDop, String geoidHeight, String ageOfGpsData, String antennaAltitude) {
        this.positionDop = positionDop;
        this.horizontalDop = horizontalDop;
        this.verticalDop = verticalDop;
        this.geoidHeight = geoidHeight;
        this.ageOfGpsData = ageOfGpsData;
        this.antennaAltitude = antennaAltitude;
    }

    public double getPositionDop() {
        return positionDop;
    }

    public void setPositionDop(double positionDop) {
        this.positionDop = positionDop;
    }

    public double getHorizontalDop() {
        return horizontalDop;
    }

    public void setHorizontalDop(double horizontalDop) {
        this.horizontalDop = horizontalDop;
    }

    public double getVerticalDop() {
        return verticalDop;
    }

    public void setVerticalDop(double verticalDop) {
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
}
