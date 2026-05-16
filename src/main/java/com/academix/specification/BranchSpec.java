package com.academix.specification;

import org.springframework.data.jpa.domain.Specification;

public class BranchSpec {

    private BranchSpec() {}

    /** null branchId → no filter (SUPER_ADMIN cross-branch view) */
    public static <T> Specification<T> inBranch(Long branchId) {
        return (root, query, cb) ->
            branchId == null
                ? cb.conjunction()
                : cb.equal(root.get("branch").get("id"), branchId);
    }
}
