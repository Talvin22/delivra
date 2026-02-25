package site.delivra.application.utils;

import java.util.List;

public final class GeoUtils {

    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    private GeoUtils() {}

    public static double distanceMeters(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        return EARTH_RADIUS_METERS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public static double minDistanceToPolyline(double lat, double lng,
                                               List<FlexiblePolylineDecoder.Waypoint> polyline) {
        if (polyline == null || polyline.isEmpty()) {
            return Double.MAX_VALUE;
        }
        if (polyline.size() == 1) {
            return distanceMeters(lat, lng, polyline.get(0).lat(), polyline.get(0).lng());
        }

        double minDist = Double.MAX_VALUE;
        for (int i = 0; i < polyline.size() - 1; i++) {
            FlexiblePolylineDecoder.Waypoint a = polyline.get(i);
            FlexiblePolylineDecoder.Waypoint b = polyline.get(i + 1);
            double dist = distanceToSegment(lat, lng, a.lat(), a.lng(), b.lat(), b.lng());
            if (dist < minDist) {
                minDist = dist;
            }
        }
        return minDist;
    }

    private static double distanceToSegment(double pLat, double pLng,
                                            double aLat, double aLng,
                                            double bLat, double bLng) {
        double ax = aLng, ay = aLat;
        double bx = bLng, by = bLat;
        double px = pLng, py = pLat;

        double dx = bx - ax;
        double dy = by - ay;
        double lenSq = dx * dx + dy * dy;

        double closeLat, closeLng;
        if (lenSq == 0) {
            closeLat = aLat;
            closeLng = aLng;
        } else {
            double t = ((px - ax) * dx + (py - ay) * dy) / lenSq;
            t = Math.max(0, Math.min(1, t));
            closeLat = ay + t * dy;
            closeLng = ax + t * dx;
        }
        return distanceMeters(pLat, pLng, closeLat, closeLng);
    }
}
