package io.github.defective4.sdr.rtltcp.server.device;

import java.io.DataOutput;
import java.io.IOException;

public record DongleInfo(byte[] magic, TunerType tunerType) {
    public void write(DataOutput output) throws IOException {
        output.write(magic);
        output.writeInt(tunerType.getId());
        output.writeInt(tunerType.getGains().length);
    }
}
