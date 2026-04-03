package com.finance.user;

/**
 * Roles available in the system.
 * VIEWER  - read-only dashboard access
 * ANALYST - read records + dashboard + trends
 * ADMIN   - full management access
 */
public enum Role {
    VIEWER,
    ANALYST,
    ADMIN
}
