package com.example.attendancesystem.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    private int markAttendancePerMinute;
    private int registerPerMinute;

    public int getMarkAttendancePerMinute() {
        return markAttendancePerMinute;
    }

    public void setMarkAttendancePerMinute(int markAttendancePerMinute) {
        this.markAttendancePerMinute = markAttendancePerMinute;
    }

    public int getRegisterPerMinute() {
        return registerPerMinute;
    }

    public void setRegisterPerMinute(int registerPerMinute) {
        this.registerPerMinute = registerPerMinute;
    }
}
