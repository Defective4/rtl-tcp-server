package io.github.defective4.sdr.rtltcp.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Objects;

import io.github.defective4.sdr.rtltcp.server.command.CommandAdapter;
import io.github.defective4.sdr.rtltcp.server.command.CommandListener;
import io.github.defective4.sdr.rtltcp.server.command.DirectSampling;
import io.github.defective4.sdr.rtltcp.server.command.GainMode;
import io.github.defective4.sdr.rtltcp.server.device.DongleInfo;
import io.github.defective4.sdr.rtltcp.server.device.TunerType;
import io.github.defective4.sdr.rtltcp.server.signal.RateLimiter;
import io.github.defective4.sdr.rtltcp.server.signal.SampleProvider;

public class RtlTcpServer implements AutoCloseable {
    public static class Builder {
        private int blockSize = 1024;
        private CommandListener listener = new CommandAdapter();
        private SampleProvider sampleProvider = buffer -> 0;
        private TunerType tunerType = TunerType.R828D;

        public RtlTcpServer create() throws IOException {
            return new RtlTcpServer(listener, sampleProvider, tunerType, blockSize);
        }

        public Builder withBlockSize(int blockSize) {
            if (blockSize <= 0) throw new IllegalArgumentException("Block size can't be less than 0!");
            this.blockSize = blockSize;
            return this;
        }

        public Builder withListener(CommandListener listener) {
            this.listener = Objects.requireNonNull(listener);
            return this;
        }

        public Builder withSampleProvider(SampleProvider sampleProvider) {
            this.sampleProvider = Objects.requireNonNull(sampleProvider);
            return this;
        }

        public Builder withTunerType(TunerType tunerType) {
            this.tunerType = Objects.requireNonNull(tunerType);
            return this;
        }
    }
    private final int blockSize;
    private final DongleInfo dongleInfo;
    private final RateLimiter limiter = new RateLimiter(0);
    private final CommandListener listener;

    private final SampleProvider sampleProvider;
    private final ServerSocket server;

    private long timeout;

    private RtlTcpServer(CommandListener listener, SampleProvider sampleProvider, TunerType tunerType, int blockSize)
            throws IOException {
        this.listener = listener;
        this.sampleProvider = sampleProvider;
        server = new ServerSocket();
        dongleInfo = new DongleInfo("RTL0".getBytes(), tunerType);
        this.blockSize = blockSize;
    }

    public void accept() throws IOException {
        try (Socket socket = server.accept();
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream())) {
            dongleInfo.write(out);
            while (!socket.isClosed()) {
                if (timeout < System.currentTimeMillis()) {
                    byte[] data = new byte[blockSize];
                    int len = sampleProvider.provide(data);
                    if (len > data.length) len = data.length;
                    if (len < 0) len = 0;
                    if (limiter.getLimit() > 0) {
                        int count = limiter.limit(len);
                        if (count > 0) {
                            out.write(data, 0, len - count);
                            timeout = System.currentTimeMillis() + 1000;
                            limiter.reset();
                            out.write(data, len - count, count);
                        } else {
                            out.write(data, 0, len);
                        }
                    } else
                        out.write(data, 0, len);
                }

                while (in.available() >= 5) {
                    byte cmd = in.readByte();
                    int arg = in.readInt();
                    switch (cmd) {
                        case 0x01 -> listener.setCenterFrequency(arg);
                        case 0x02 -> listener.setSampleRate(arg);
                        case 0x03 -> listener.setGainMode(arg == 1 ? GainMode.MANUAL : GainMode.AUTO);
                        case 0x04 -> listener.setGain(arg);
                        case 0x05 -> listener.setFrequencyCorrection(arg);
                        case 0x06 -> listener.setIFGain(arg >> 16, arg & 0xffff);
                        case 0x07 -> listener.setTestMode(arg > 0);
                        case 0x08 -> listener.setAGCMode(arg > 0);
                        case 0x09 -> listener.setDirectSampling(switch (arg) {
                            default -> DirectSampling.OFF;
                            case 1 -> DirectSampling.I_BRANCH;
                            case 2 -> DirectSampling.Q_BRANCH;
                        });
                        case 0x0a -> listener.setOffsetTuning(arg > 0);
                        case 0x0b -> listener.setRTLXtalFreq(arg);
                        case 0x0c -> listener.setTunerXtalFreq(arg);
                        case 0x0d -> listener.setGainIndex(arg);
                        case 0x0e -> listener.setBiasTee(arg > 0);
                        default -> {}
                    }
                }
            }
        }
    }

    public void bind(SocketAddress endpoint) throws IOException {
        server.bind(endpoint);
    }

    @Override
    public void close() throws IOException {
        server.close();
    }

    public CommandListener getCommandListener() {
        return listener;
    }

    public boolean isBound() {
        return server.isBound();
    }

    public boolean isClosed() {
        return server.isClosed();
    }

    public void setSampleRate(float rate) {
        limiter.setLimit((int) rate * 2);
    }

}
