package io.github.defective4.sdr.rtltcp.server.command;

import java.util.Arrays;

public enum TunerType {
    E4000(1, new int[] { -10, 15, 40, 65, 90, 115, 140, 165, 190, 215, 240, 290, 340, 420 }),
    FC0012(2, new int[] { -99, -40, 71, 179, 192 }),
    FC0013(3,
            new int[] { -99, -73, -65, -63, -60, -58, -54, 58, 61, 63, 65, 67, 68, 70, 71, 179, 181, 182, 184, 186, 188,
                    191, 197 }),
    FC2580(4, new int[] { 0 }), R820T(5,
            new int[] { 0, 9, 14, 27, 37, 77, 87, 125, 144, 157, 166, 197, 207, 229, 254, 280, 297, 328, 338, 364, 372,
                    386, 402, 421, 434, 439, 445, 480, 496 }),
    R828D(6, new int[] { 0, 9, 14, 27, 37, 77, 87, 125, 144, 157, 166, 197, 207, 229, 254, 280, 297, 328, 338, 364, 372,
            386, 402, 421, 434, 439, 445, 480, 496 }),
    UNKNOWN(0, new int[] { 0 });

    private final int[] gains;
    private final int id;

    private TunerType(int id, int[] gains) {
        this.id = id;
        this.gains = gains;
    }

    public int[] getGains() {
        return Arrays.copyOf(gains, gains.length);
    }

    public int getId() {
        return id;
    }

}
