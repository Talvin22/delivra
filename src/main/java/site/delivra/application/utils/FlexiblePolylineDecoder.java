package site.delivra.application.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Decoder for HERE Flexible Polyline encoding format.
 * Spec: https://github.com/heremaps/flexible-polyline
 */
public final class FlexiblePolylineDecoder {

    private FlexiblePolylineDecoder() {}

    public record Waypoint(double lat, double lng) {}

    /**
     * Decodes a HERE Flexible Polyline encoded string into a list of lat/lng waypoints.
     */
    public static List<Waypoint> decode(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return Collections.emptyList();
        }

        int[] index = {0};

        // Header: first unsigned value contains (version << 4) | precision
        long headerVal = nextUnsignedValue(encoded, index);
        int precision = (int) (headerVal & 0x0F);

        // Second header value: (thirdDimPrecision << 4) | thirdDim — skip, we only use lat/lng
        nextUnsignedValue(encoded, index);

        double factor = Math.pow(10, precision);
        List<Waypoint> result = new ArrayList<>();

        long lastLat = 0;
        long lastLng = 0;

        while (index[0] < encoded.length()) {
            lastLat += toSigned(nextUnsignedValue(encoded, index));
            lastLng += toSigned(nextUnsignedValue(encoded, index));
            result.add(new Waypoint(lastLat / factor, lastLng / factor));
        }

        return result;
    }

    /**
     * Reads the next variable-length unsigned integer, advancing index[0].
     * Each character contributes 5 bits; the continuation bit is bit 5 (0x20).
     */
    private static long nextUnsignedValue(String encoded, int[] index) {
        long result = 0;
        int shift = 0;
        while (index[0] < encoded.length()) {
            int chunk = encoded.charAt(index[0]++) - 63;
            result |= ((long) (chunk & 0x1F)) << shift;
            shift += 5;
            if ((chunk & 0x20) == 0) {
                break;
            }
        }
        return result;
    }

    /**
     * Zigzag decode: converts unsigned integer to signed.
     */
    private static long toSigned(long value) {
        return (value & 1) == 0 ? (value >> 1) : -(value >> 1) - 1;
    }
}
