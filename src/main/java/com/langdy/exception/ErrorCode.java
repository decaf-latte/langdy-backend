package com.langdy.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_START_TIME(HttpStatus.BAD_REQUEST, "유효하지 않은 수업 시작 시각입니다."),
    PAST_START_TIME(HttpStatus.BAD_REQUEST, "과거 시각으로는 수업을 신청할 수 없습니다."),
    MISSING_STUDENT_ID(HttpStatus.BAD_REQUEST, "X-Student-Id 헤더가 누락되었습니다."),

    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "코스를 찾을 수 없습니다."),
    TEACHER_NOT_FOUND(HttpStatus.NOT_FOUND, "선생님을 찾을 수 없습니다."),
    STUDENT_NOT_FOUND(HttpStatus.NOT_FOUND, "학습자를 찾을 수 없습니다."),

    TEACHER_SCHEDULE_CONFLICT(HttpStatus.CONFLICT, "해당 시간에 선생님의 수업이 이미 존재합니다."),
    STUDENT_SCHEDULE_CONFLICT(HttpStatus.CONFLICT, "해당 시간에 학습자의 수업이 이미 존재합니다."),

    INVALID_LESSON_STATUS(HttpStatus.BAD_REQUEST, "수업 상태를 변경할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
