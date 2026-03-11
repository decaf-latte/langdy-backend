package com.langdy.dto;

import com.langdy.entity.Teacher;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AvailableTeachersResponse {

    private final List<TeacherInfo> teachers;

    public static AvailableTeachersResponse from(List<Teacher> teachers) {
        List<TeacherInfo> teacherInfos = teachers.stream()
                .map(TeacherInfo::from)
                .toList();
        return new AvailableTeachersResponse(teacherInfos);
    }

    @Getter
    @AllArgsConstructor
    public static class TeacherInfo {

        private final Long id;
        private final String name;

        public static TeacherInfo from(Teacher teacher) {
            return new TeacherInfo(teacher.getId(), teacher.getName());
        }
    }
}
