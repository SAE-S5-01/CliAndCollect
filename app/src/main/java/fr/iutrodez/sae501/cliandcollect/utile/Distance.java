package fr.iutrodez.sae501.cliandcollect.utile;

public class Distance {

    private static final int rayonRechercheKm = 300;

    private final static double kmEnLatitude = rayonRechercheKm / 111 ;
    private final static double kmEnLongitude = rayonRechercheKm / (111 * Math.cos(Math.toRadians(48.8566)));

    public static double[] creationViewBox(double maLatitude , double maLongitude) {
        double[] viewbox = new double[4];
        double sud  = maLatitude - kmEnLatitude;
        double nord = maLatitude + kmEnLatitude;
        double ouest = maLongitude - kmEnLongitude;
        double est = maLongitude + kmEnLongitude;
        viewbox [1] = nord;
        viewbox [0] = ouest;
        viewbox [3] = sud;
        viewbox [2] = est;
        return viewbox;
    }
}
