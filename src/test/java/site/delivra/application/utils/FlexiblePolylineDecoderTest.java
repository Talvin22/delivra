package site.delivra.application.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlexiblePolylineDecoderTest {

    @Test
    void decode_null_returnsEmpty() {
        List<FlexiblePolylineDecoder.Waypoint> result = FlexiblePolylineDecoder.decode(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void decode_emptyString_returnsEmpty() {
        List<FlexiblePolylineDecoder.Waypoint> result = FlexiblePolylineDecoder.decode("");
        assertTrue(result.isEmpty());
    }

    @Test
    void decode_validPolyline_returnsWaypoints() {
        String encoded = "BFoz5xJ67i1B1B7PzIhaxL7Y";
        List<FlexiblePolylineDecoder.Waypoint> result = FlexiblePolylineDecoder.decode(encoded);
        assertFalse(result.isEmpty());
    }

    @Test
    void decode_multiSectionPolyline_concatenatesAllSections() {
        String encoded = "BFoz5xJ67i1B1B7PzIhaxL7Y;BFoz5xJ67i1B1B7PzIhaxL7Y";
        List<FlexiblePolylineDecoder.Waypoint> single = FlexiblePolylineDecoder.decode("BFoz5xJ67i1B1B7PzIhaxL7Y");
        List<FlexiblePolylineDecoder.Waypoint> multi = FlexiblePolylineDecoder.decode(encoded);
        assertEquals(single.size() * 2, multi.size());
    }

    @Test
    void decode_multiSectionWithEmptyPart_ignoresEmpty() {
        String encoded = "BFoz5xJ67i1B1B7PzIhaxL7Y;";
        List<FlexiblePolylineDecoder.Waypoint> single = FlexiblePolylineDecoder.decode("BFoz5xJ67i1B1B7PzIhaxL7Y");
        List<FlexiblePolylineDecoder.Waypoint> result = FlexiblePolylineDecoder.decode(encoded);
        assertEquals(single.size(), result.size());
    }

    @Test
    void decode_waypointCoordinatesAreInValidRange() {
        String encoded = "BFoz5xJ67i1B1B7PzIhaxL7Y";
        List<FlexiblePolylineDecoder.Waypoint> waypoints = FlexiblePolylineDecoder.decode(encoded);
        for (FlexiblePolylineDecoder.Waypoint wp : waypoints) {
            assertTrue(wp.lat() >= -90 && wp.lat() <= 90,
                    "Latitude out of range: " + wp.lat());
            assertTrue(wp.lng() >= -180 && wp.lng() <= 180,
                    "Longitude out of range: " + wp.lng());
        }
    }

    @Test
    void waypoint_recordEquality() {
        var a = new FlexiblePolylineDecoder.Waypoint(55.75, 37.62);
        var b = new FlexiblePolylineDecoder.Waypoint(55.75, 37.62);
        assertEquals(a, b);
    }
}
