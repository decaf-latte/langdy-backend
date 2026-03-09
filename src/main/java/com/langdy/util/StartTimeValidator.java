package com.langdy.util;

import com.langdy.exception.BusinessException;
import com.langdy.exception.ErrorCode;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public final class StartTimeValidator {

    private StartTimeValidator() {
    }

    public static void validate(LocalDateTime startAt, Clock clock) {
        if (!startAt.equals(startAt.truncatedTo(ChronoUnit.MINUTES))) {
            throw new BusinessException(ErrorCode.INVALID_START_TIME);
        }

        if (startAt.getMinute() % 30 != 0) {
            throw new BusinessException(ErrorCode.INVALID_START_TIME);
        }

        if (startAt.isBefore(LocalDateTime.now(clock))) {
            throw new BusinessException(ErrorCode.PAST_START_TIME);
        }
    }
}
