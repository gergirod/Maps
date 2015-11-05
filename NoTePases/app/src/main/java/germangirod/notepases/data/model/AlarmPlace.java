package germangirod.notepases.data.model;

/**
 * Created by germangirod on 11/5/15.
 */
public class AlarmPlace {

    public double latitude;
    public double longitude;
    public double radius;
    public String name;

    public AlarmPlace(double latitude, double longitude, double radius, String name) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getRadius() {
        return radius;
    }

    public String getName() {
        return name;
    }
}
