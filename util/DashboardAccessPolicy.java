package util;

/** Maps role-specific dashboard pages to the only role allowed to open them. */
public final class DashboardAccessPolicy {
    private DashboardAccessPolicy() {}

    public static String requiredRole(String servletPath) {
        if (servletPath == null) return null;
        return switch (servletPath) {
            case "/patient-dashboard.html" -> "patient";
            case "/doctor-dashboard.html" -> "doctor";
            case "/admin-dashboard.html" -> "admin";
            default -> null;
        };
    }

    public static boolean isAllowed(String servletPath, String role) {
        String required = requiredRole(servletPath);
        return required == null || required.equals(role);
    }
}
