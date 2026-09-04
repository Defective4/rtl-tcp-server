package io.github.defective4.sdr.rtltcp.server;

public class RateLimiter {
    private int count = 0;
    private int limit;

    public RateLimiter(int limit) {
        this.limit = limit;
    }

    public int getLimit() {
        return limit;
    }

    public int limit(int data) {
        count += data;
        return Math.max(0, count - limit);
    }

    public void reset() {
        count = 0;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
