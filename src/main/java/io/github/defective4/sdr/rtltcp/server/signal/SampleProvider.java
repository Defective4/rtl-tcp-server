package io.github.defective4.sdr.rtltcp.server.signal;

public interface SampleProvider {
    int provide(byte[] buffer);
}
