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
import io.github.defective4.sdr.rtltcp.server.command.TunerType;
import io.github.defective4.sdr.rtltcp.server.device.DongleInfo;
import io.github.defective4.sdr.rtltcp.server.signal.RateLimiter;
import io.github.defective4.sdr.rtltcp.server.signal.SampleProvider;

public class RtlTcpServer implements AutoCloseable {
    private static final DongleInfo DONGLE_INFO = new DongleInfo("RTL0".getBytes(), TunerType.R828D);
    private final RateLimiter limiter = new RateLimiter(0);
    private CommandListener listener = new CommandAdapter();
    private SampleProvider sampleProvider = buffer -> 0;
    private final ServerSocket server;

    private long timeout;

    public RtlTcpServer() throws IOException {
        server = new ServerSocket();
    }

    public void accept() throws IOException {
        try (Socket socket = server.accept();
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream())) {
            DONGLE_INFO.write(out);
            while (!socket.isClosed()) {
                if (timeout < System.currentTimeMillis()) {
                    byte[] data = new byte[1024];
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

    public void setCommandListener(CommandListener commandListener) {
        listener = Objects.requireNonNull(commandListener);
    }

    public void setSampleProvider(SampleProvider sampleProvider) {
        this.sampleProvider = Objects.requireNonNull(sampleProvider);
    }

    public void setSampleRate(float rate) {
        limiter.setLimit((int) rate * 2);
    }

}
