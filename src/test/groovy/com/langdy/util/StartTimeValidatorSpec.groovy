package com.langdy.util

import com.langdy.exception.BusinessException
import com.langdy.exception.ErrorCode
import spock.lang.Specification

import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneOffset

class StartTimeValidatorSpec extends Specification {

    Clock clock = Clock.fixed(
            LocalDateTime.of(2026, 3, 1, 0, 0).toInstant(ZoneOffset.UTC),
            ZoneOffset.UTC
    )

    def "정상적인 시작 시각이면 예외가 발생하지 않는다"() {
        when:
        StartTimeValidator.validate(startAt, clock)

        then:
        noExceptionThrown()

        where:
        startAt                                    | _
        LocalDateTime.of(2026, 3, 10, 9, 0, 0)    | _ // 정각
        LocalDateTime.of(2026, 3, 10, 9, 30, 0)   | _ // 30분
        LocalDateTime.of(2026, 3, 10, 10, 0, 0)   | _ // 10시 정각
    }

    def "유효하지 않은 시작 시각이면 INVALID_START_TIME 에러"() {
        when:
        StartTimeValidator.validate(startAt, clock)

        then:
        def e = thrown(BusinessException)
        e.errorCode == ErrorCode.INVALID_START_TIME

        where:
        startAt                                          | _
        LocalDateTime.of(2026, 3, 10, 9, 15, 0)         | _ // 15분
        LocalDateTime.of(2026, 3, 10, 9, 0, 30)         | _ // 초 포함
        LocalDateTime.of(2026, 3, 10, 9, 45, 0)         | _ // 45분
        LocalDateTime.of(2026, 3, 10, 9, 10, 0)         | _ // 10분
    }

    def "과거 시각이면 PAST_START_TIME 에러"() {
        when:
        StartTimeValidator.validate(LocalDateTime.of(2026, 2, 28, 9, 0, 0), clock)

        then:
        def e = thrown(BusinessException)
        e.errorCode == ErrorCode.PAST_START_TIME
    }
}
