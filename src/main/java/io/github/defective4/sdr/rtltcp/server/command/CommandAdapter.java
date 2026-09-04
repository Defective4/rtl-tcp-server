package io.github.defective4.sdr.rtltcp.server.command;

public class CommandAdapter implements CommandListener {

    @Override
    public void setAGCMode(boolean agcMode) {}

    @Override
    public void setBiasTee(boolean biasT) {}

    @Override
    public void setCenterFrequency(float frequency) {}

    @Override
    public void setDirectSampling(DirectSampling samplingMode) {}

    @Override
    public void setFrequencyCorrection(int correction) {}

    @Override
    public void setGain(int gain) {}

    @Override
    public void setGainIndex(int gainIndex) {}

    @Override
    public void setGainMode(GainMode mode) {}

    @Override
    public void setIFGain(int stage, int gain) {}

    @Override
    public void setOffsetTuning(boolean offsetTuning) {}

    @Override
    public void setRTLXtalFreq(float rtlFreq) {}

    @Override
    public void setSampleRate(float rate) {}

    @Override
    public void setTestMode(boolean testMode) {}

    @Override
    public void setTunerXtalFreq(float tunerFreq) {}

}
