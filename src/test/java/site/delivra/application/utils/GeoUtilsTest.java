package site.delivra.application.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeoUtilsTest {

    @Test
    void calculateDistance_samePoint_returnsZero() {
        double dist = GeoUtils.calculateDistance(55.75, 37.62, 55.75, 37.62);
        assertEquals(0.0, dist, 0.001);
    }

    @Test
    void distanceMeters_knownDistance_withinTolerance() {
        double dist = GeoUtils.calculateDistance(50.4501, 30.5234, 49.8397, 24.0297);
        assertTrue(dist > 460_000 && dist < 480_000,
                "Kyiv-Lviv distance should be ~467km, got: " + dist);
    }

    @Test
    void calculateDistance_isSymmetric() {
        double d1 = GeoUtils.calculateDistance(55.75, 37.62, 48.85, 2.35);
        double d2 = GeoUtils.calculateDistance(48.85, 2.35, 55.75, 37.62);
        assertEquals(d1, d2, 0.001);
    }

    @Test
    void minDistanceToPolyline_nullPolyline_returnsMaxValue() {
        double dist = GeoUtils.minDistanceToPolyline(55.75, 37.62, null);
        assertEquals(Double.MAX_VALUE, dist);
    }

    @Test
    void minDistanceToPolyline_emptyPolyline_returnsMaxValue() {
        double dist = GeoUtils.minDistanceToPolyline(55.75, 37.62, List.of());
        assertEquals(Double.MAX_VALUE, dist);
    }

    @Test
    void minDistanceToPolyline_singleWaypoint_returnsPointDistance() {
        var waypoint = new FlexiblePolylineDecoder.Waypoint(55.75, 37.62);
        double dist = GeoUtils.minDistanceToPolyline(55.75, 37.62, List.of(waypoint));
        assertEquals(0.0, dist, 0.001);
    }

    @Test
    void minDistanceToPolyline_pointOnSegment_returnsNearZero() {
        var a = new FlexiblePolylineDecoder.Waypoint(55.75, 37.60);
        var b = new FlexiblePolylineDecoder.Waypoint(55.75, 37.70);
        var mid = new FlexiblePolylineDecoder.Waypoint(55.75, 37.65);
        double dist = GeoUtils.minDistanceToPolyline(mid.lat(), mid.lng(), List.of(a, b));
        assertTrue(dist < 50, "Point on segment should have near-zero distance, got: " + dist);
    }

    @Test
    void minDistanceToPolyline_pointFarFromPolyline_returnsLargeDistance() {
        var a = new FlexiblePolylineDecoder.Waypoint(55.75, 37.60);
        var b = new FlexiblePolylineDecoder.Waypoint(55.75, 37.70);
        double dist = GeoUtils.minDistanceToPolyline(48.85, 2.35, List.of(a, b));
        assertTrue(dist > 1_000_000, "Paris should be very far from Moscow segment, got: " + dist);
    }

    @Test
    void minDistanceToPolyline_usesClosestSegment() {
        var p1 = new FlexiblePolylineDecoder.Waypoint(55.75, 37.60);
        var p2 = new FlexiblePolylineDecoder.Waypoint(55.75, 37.65);
        var p3 = new FlexiblePolylineDecoder.Waypoint(55.75, 37.70);

        double distToPolyline = GeoUtils.minDistanceToPolyline(55.751, 37.625, List.of(p1, p2, p3));
        double directToP1 = GeoUtils.calculateDistance(55.751, 37.625, 55.75, 37.60);
        assertTrue(distToPolyline < directToP1, "Polyline distance should be less than distance to endpoint");
    }
}
