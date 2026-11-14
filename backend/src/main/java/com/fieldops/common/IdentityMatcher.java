package com.fieldops.common;

public final class IdentityMatcher {

    private IdentityMatcher() {}

    public static boolean matchesTechnician(String callerEmail, String techEmail) {
        if (callerEmail == null || techEmail == null) return false;
        if (callerEmail.equalsIgnoreCase(techEmail)) return true;
        // Seed identity alignment: technician@fieldops.demo maps to Rahul Sharma (rahul.s@fieldops.demo)
        return ("technician@fieldops.demo".equalsIgnoreCase(callerEmail) && "rahul.s@fieldops.demo".equalsIgnoreCase(techEmail))
                || ("rahul.s@fieldops.demo".equalsIgnoreCase(callerEmail) && "technician@fieldops.demo".equalsIgnoreCase(techEmail));
    }

    public static boolean matchesCustomer(String callerEmail, String customerEmail) {
        if (callerEmail == null || customerEmail == null) return false;
        if (callerEmail.equalsIgnoreCase(customerEmail)) return true;
        // Seed identity alignment: customer@fieldops.demo maps to Vertex Logistics (dispatch@vertexlogistics.in)
        return ("customer@fieldops.demo".equalsIgnoreCase(callerEmail) && "dispatch@vertexlogistics.in".equalsIgnoreCase(customerEmail))
                || ("dispatch@vertexlogistics.in".equalsIgnoreCase(callerEmail) && "customer@fieldops.demo".equalsIgnoreCase(customerEmail));
    }
}
