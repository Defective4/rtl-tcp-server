package io.github.defective4.sdr.rtltcp;

import java.net.InetSocketAddress;
import java.util.Random;

import io.github.defective4.sdr.rtltcp.server.CommandListener;
import io.github.defective4.sdr.rtltcp.server.DirectSampling;
import io.github.defective4.sdr.rtltcp.server.GainMode;
import io.github.defective4.sdr.rtltcp.server.RtlTcpServer;

public class Main {
    public static void main(String[] args) {
        Random rand = new Random();
        try (RtlTcpServer server = new RtlTcpServer()) {
            server.setSampleRate(250e3f);
            server.setSampleProvider(buffer -> {
                rand.nextBytes(buffer);
                return buffer.length;
            });
            server.setCommandListener(new CommandListener() {

                @Override
                public void setAGCMode(boolean agcMode) {
                    System.err.println("AGC Mode: " + agcMode);
                }

                @Override
                public void setBiasTee(boolean biasT) {
                    System.err.println("Bias Tee: " + biasT);
                }

                @Override
                public void setCenterFrequency(float frequency) {
                    System.err.println("Center frequency: " + frequency);
                }

                @Override
                public void setDirectSampling(DirectSampling samplingMode) {
                    System.err.println("Direct sampling: " + samplingMode);
                }

                @Override
                public void setFrequencyCorrection(int correction) {
                    System.err.println("Freq correction: " + correction);
                }

                @Override
                public void setGain(int gain) {
                    System.err.println("Gain: " + gain);
                }

                @Override
                public void setGainIndex(int gainIndex) {
                    System.err.println("Gain index: " + gainIndex);
                }

                @Override
                public void setGainMode(GainMode mode) {
                    System.err.println("Gain mode: " + mode);
                }

                @Override
                public void setIFGain(int stage, int gain) {
                    System.err.println("IF gain: " + stage + "/" + gain);
                }

                @Override
                public void setOffsetTuning(boolean offsetTuning) {
                    System.err.println("Offset tuning: " + offsetTuning);
                }

                @Override
                public void setRTLXtalFreq(float rtlFreq) {}

                @Override
                public void setSampleRate(float rate) {
                    System.err.println("Sample rate: " + rate);
                }

                @Override
                public void setTestMode(boolean testMode) {
                    System.err.println("Test mode: " + testMode);
                }

                @Override
                public void setTunerXtalFreq(float tunerFreq) {}
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
