package com.academix.context;

import com.academix.service.UserService;
import com.academix.model.Branch;
import com.academix.model.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BranchContextInterceptor implements HandlerInterceptor {

    private final BranchContext branchContext;
    private final UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest req,
                             HttpServletResponse res, Object handler) {
        String param = req.getParameter("branchId");
        if (param != null) {
            Long requestedId = param.isBlank() ? null : Long.parseLong(param);
            User current = userService.currentUser();
            if (current != null) {
                boolean allowed = current.isSuperAdmin() ||
                    (requestedId != null && current.getAccessibleBranches().stream()
                        .anyMatch(b -> b.getId().equals(requestedId)));
                if (allowed) {
                    branchContext.setActiveBranchId(requestedId);
                    branchContext.setActiveBranchName(
                        requestedId == null ? "All Branches (HQ)" :
                        current.getAccessibleBranches().stream()
                            .filter(b -> b.getId().equals(requestedId))
                            .map(Branch::getBranchName)
                            .findFirst().orElse(null)
                    );
                }
            }
        }
        // Auto-select for single-branch users
        if (branchContext.getActiveBranchId() == null) {
            User current = userService.currentUser();
            if (current != null && !current.isSuperAdmin()) {
                List<Branch> branches = current.getAccessibleBranches();
                if (branches.size() == 1) {
                    branchContext.setActiveBranchId(branches.get(0).getId());
                    branchContext.setActiveBranchName(branches.get(0).getBranchName());
                }
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest req, HttpServletResponse res,
                           Object handler, ModelAndView mav) {
        if (mav != null) {
            mav.addObject("activeBranchId",   branchContext.getActiveBranchId());
            mav.addObject("activeBranchName", branchContext.getActiveBranchName());
        }
    }
}
