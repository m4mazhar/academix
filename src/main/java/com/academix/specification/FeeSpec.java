package com.academix.specification;

import com.academix.model.FeePayment;
import com.academix.model.enums.FeeStatus;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class FeeSpec {

    private FeeSpec() {}

    public static Specification<FeePayment> inBatch(Long batchId) {
        return (root, query, cb) ->
            batchId != null
                ? cb.equal(root.get("batch").get("id"), batchId)
                : cb.conjunction();
    }

    public static Specification<FeePayment> hasStatus(String status) {
        return (root, query, cb) ->
            StringUtils.hasText(status)
                ? cb.equal(root.get("status"), FeeStatus.valueOf(status))
                : cb.conjunction();
    }

    public static Specification<FeePayment> hasMonth(Integer month) {
        return (root, query, cb) ->
            month != null ? cb.equal(root.get("month"), month) : cb.conjunction();
    }

    public static Specification<FeePayment> hasYear(Integer year) {
        return (root, query, cb) ->
            year != null ? cb.equal(root.get("year"), year) : cb.conjunction();
    }
}
