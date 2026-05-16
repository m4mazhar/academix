package com.academix.context;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Component
@SessionScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
@Getter @Setter
public class BranchContext {
    /** null = all branches (SUPER_ADMIN cross-branch view only) */
    private Long activeBranchId;
    private String activeBranchName;
}
