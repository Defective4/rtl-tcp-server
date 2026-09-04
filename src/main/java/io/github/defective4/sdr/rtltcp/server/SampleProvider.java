package io.github.defective4.sdr.rtltcp.server;

public interface SampleProvider {
    int provide(byte[] buffer);
}
