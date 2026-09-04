package io.github.defective4.sdr.rtltcp.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Objects;

public class RtlTcpServer implements AutoCloseable {
    private static final DongleInfo DONGLE_INFO = new DongleInfo("RTL0".getBytes(), TunerType.R828D);
    private final RateLimiter limiter = new RateLimiter(0);
    private SampleProvider sampleProvider = buffer -> 0;
    private final ServerSocket server;

    public RtlTcpServer() throws IOException {
        server = new ServerSocket();
    }

    public void accept() throws IOException {
        try (Socket socket = server.accept();
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream())) {
            DONGLE_INFO.write(out);
            while (!socket.isClosed()) {
                byte[] data = new byte[1024];
                int len = sampleProvider.provide(data);
                if (len > data.length) len = data.length;
                if (len < 0) len = 0;
                if (limiter.getLimit() > 0) {
                    int count = limiter.limit(len);
                    if (count > 0) {
                        out.write(data, 0, len - count);
                        Thread.sleep(1000);
                        limiter.reset();
                        out.write(data, len - count, count);
                    } else {
                        out.write(data, 0, len);
                    }
                } else
                    out.write(data, 0, len);
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    public void bind(SocketAddress endpoint) throws IOException {
        server.bind(endpoint);
    }

    @Override
    public void close() throws IOException {
        server.close();
    }

    public boolean isBound() {
        return server.isBound();
    }

    public boolean isClosed() {
        return server.isClosed();
    }

    public void setSampleProvider(SampleProvider sampleProvider) {
        this.sampleProvider = Objects.requireNonNull(sampleProvider);
    }

    public void setSampleRate(float rate) {
        limiter.setLimit((int) rate * 2);
    }

}
