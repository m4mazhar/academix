package com.academix.specification;

import com.academix.model.Student;
import com.academix.model.enums.StudentStatus;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class StudentSpec {

    private StudentSpec() {}

    public static Specification<Student> nameLike(String name) {
        return (root, query, cb) ->
            StringUtils.hasText(name)
                ? cb.like(cb.lower(root.get("fullName")), "%" + name.toLowerCase() + "%")
                : cb.conjunction();
    }

    public static Specification<Student> phoneLike(String phone) {
        return (root, query, cb) ->
            StringUtils.hasText(phone)
                ? cb.like(root.get("phone"), "%" + phone + "%")
                : cb.conjunction();
    }

    public static Specification<Student> inBatch(Long batchId) {
        return (root, query, cb) ->
            batchId != null
                ? cb.equal(root.get("batch").get("id"), batchId)
                : cb.conjunction();
    }

    public static Specification<Student> hasStatus(String status) {
        return (root, query, cb) ->
            StringUtils.hasText(status)
                ? cb.equal(root.get("status"), StudentStatus.valueOf(status))
                : cb.conjunction();
    }
}
