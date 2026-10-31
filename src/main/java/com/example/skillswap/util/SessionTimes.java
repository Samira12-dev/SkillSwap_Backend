package com.example.skillswap.util;

import com.example.skillswap.entity.Session;

import java.time.LocalDateTime;

public final class SessionTimes {

    private SessionTimes() {
    }

    public static LocalDateTime scheduledEnd(Session session) {
        if (session == null || session.getDate() == null || session.getDuration() == null) {
            return null;
        }

        return session.getDate().plusMinutes(session.getDuration());
    }
}
