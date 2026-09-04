package io.github.defective4.sdr.rtltcp;

import java.net.InetSocketAddress;
import java.util.Random;

import io.github.defective4.sdr.rtltcp.server.RtlTcpServer;

public class Main {
    public static void main(String[] args) {
        Random rand = new Random();
        try (RtlTcpServer server = new RtlTcpServer()) {
            server.setSampleRate(192e3f);
            server.setSampleProvider(buffer -> {
                rand.nextBytes(buffer);
                return buffer.length;
            });
            server.bind(new InetSocketAddress(1234));
            while (true) try {
                server.accept();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
