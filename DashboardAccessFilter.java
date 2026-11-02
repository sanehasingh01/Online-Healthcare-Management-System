import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import util.DashboardAccessPolicy;

/** Protects static role dashboards from direct access by unauthenticated or wrong-role users. */
@WebFilter(urlPatterns = {
        "/patient-dashboard.html",
        "/doctor-dashboard.html",
        "/admin-dashboard.html"
})
public class DashboardAccessFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requiredRole = DashboardAccessPolicy.requiredRole(httpRequest.getServletPath());
        if (requiredRole == null) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        Object role = session == null ? null : session.getAttribute("userRole");
        if (!(role instanceof String)) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.html?error=loginrequired");
            return;
        }
        if (!requiredRole.equals(role)) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "This dashboard is not available to your account.");
            return;
        }

        httpResponse.setHeader("Cache-Control", "no-store");
        chain.doFilter(request, response);
    }
}
