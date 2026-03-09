package com.langdy.repository;

import com.langdy.entity.LessonStatus;
import com.langdy.entity.Teacher;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.langdy.entity.QLesson.lesson;
import static com.langdy.entity.QTeacher.teacher;

@Repository
@RequiredArgsConstructor
public class LessonQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<Teacher> findAvailableTeachers(LocalDateTime startAt) {
        return queryFactory
                .selectFrom(teacher)
                .where(teacher.id.notIn(
                        JPAExpressions
                                .select(lesson.teacherId)
                                .from(lesson)
                                .where(
                                        lesson.startAt.eq(startAt),
                                        lesson.status.eq(LessonStatus.BOOKED)
                                )
                ))
                .orderBy(teacher.id.asc())
                .fetch();
    }
}
