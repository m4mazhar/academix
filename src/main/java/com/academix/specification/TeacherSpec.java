package com.academix.specification;

import com.academix.model.Teacher;
import com.academix.model.enums.TeacherStatus;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class TeacherSpec {

    private TeacherSpec() {}

    public static Specification<Teacher> nameLike(String name) {
        return (root, query, cb) ->
            StringUtils.hasText(name)
                ? cb.like(cb.lower(root.get("fullName")), "%" + name.toLowerCase() + "%")
                : cb.conjunction();
    }

    public static Specification<Teacher> isActive() {
        return (root, query, cb) ->
            cb.equal(root.get("status"), TeacherStatus.ACTIVE);
    }

    public static Specification<Teacher> hasStatus(String status) {
        return (root, query, cb) ->
            StringUtils.hasText(status)
                ? cb.equal(root.get("status"), TeacherStatus.valueOf(status))
                : cb.conjunction();
    }
}
