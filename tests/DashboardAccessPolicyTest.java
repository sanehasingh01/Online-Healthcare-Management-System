package tests;

import util.DashboardAccessPolicy;

/** Checks the role rules used by the dashboard access filter. */
public final class DashboardAccessPolicyTest {
    private DashboardAccessPolicyTest() {}

    public static void main(String[] args) {
        check("patient".equals(DashboardAccessPolicy.requiredRole("/patient-dashboard.html")), "patient page requires patient role");
        check("doctor".equals(DashboardAccessPolicy.requiredRole("/doctor-dashboard.html")), "doctor page requires doctor role");
        check("admin".equals(DashboardAccessPolicy.requiredRole("/admin-dashboard.html")), "admin page requires admin role");
        check(DashboardAccessPolicy.isAllowed("/patient-dashboard.html", "patient"), "matching patient role is allowed");
        check(!DashboardAccessPolicy.isAllowed("/patient-dashboard.html", "doctor"), "doctor is denied patient dashboard");
        check(!DashboardAccessPolicy.isAllowed("/admin-dashboard.html", "patient"), "patient is denied admin dashboard");
        check(DashboardAccessPolicy.isAllowed("/index.html", null), "public page remains public");
        System.out.println("DashboardAccessPolicy checks passed: 7");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError("Failed: " + message);
    }
}
