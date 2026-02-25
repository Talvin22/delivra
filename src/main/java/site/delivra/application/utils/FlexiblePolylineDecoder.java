package site.delivra.application.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FlexiblePolylineDecoder {

    private FlexiblePolylineDecoder() {}

    public record Waypoint(double lat, double lng) {}

    private static final int[] DECODING_TABLE = {
        62, -1, -1, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1,
         0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24, 25, -1, -1, -1, -1, 63, -1, 26, 27, 28, 29, 30, 31, 32, 33,
        34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51
    };

    public static List<Waypoint> decode(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return Collections.emptyList();
        }
        if (encoded.contains(";")) {
            List<Waypoint> result = new ArrayList<>();
            for (String part : encoded.split(";")) {
                if (!part.isEmpty()) result.addAll(decodeSection(part));
            }
            return result;
        }
        return decodeSection(encoded);
    }

    private static List<Waypoint> decodeSection(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return Collections.emptyList();
        }

        int[] index = {0};

        nextUnsignedValue(encoded, index);

        long precisionVal = nextUnsignedValue(encoded, index);
        int precision = (int) (precisionVal & 0x0F);

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

    private static long nextUnsignedValue(String encoded, int[] index) {
        long result = 0;
        int shift = 0;
        while (index[0] < encoded.length()) {
            int chunk = DECODING_TABLE[encoded.charAt(index[0]++) - 45];
            result |= ((long) (chunk & 0x1F)) << shift;
            shift += 5;
            if ((chunk & 0x20) == 0) {
                break;
            }
        }
        return result;
    }

    private static long toSigned(long value) {
        return (value & 1) == 0 ? (value >> 1) : -(value >> 1) - 1;
    }
}
