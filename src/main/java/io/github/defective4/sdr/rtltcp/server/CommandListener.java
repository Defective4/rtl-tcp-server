package io.github.defective4.sdr.rtltcp.server;

public interface CommandListener {

    void setAGCMode(boolean agcMode);

    void setBiasTee(boolean biasT);

    void setCenterFrequency(float frequency);

    void setDirectSampling(DirectSampling samplingMode);

    void setFrequencyCorrection(int correction);

    void setGain(int gain);

    void setGainIndex(int gainIndex);

    void setGainMode(GainMode mode);

    void setIFGain(int stage, int gain);

    void setOffsetTuning(boolean offsetTuning);

    void setRTLXtalFreq(float rtlFreq);

    void setSampleRate(float rate);

    void setTestMode(boolean testMode);

    void setTunerXtalFreq(float tunerFreq);

}
