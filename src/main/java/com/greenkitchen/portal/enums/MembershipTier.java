package com.greenkitchen.portal.enums;

public enum MembershipTier {
    ENERGY("Energy", 0, 2000000),
    VITALITY("Vitality", 2000000, 5000000), 
    RADIANCE("Radiance", 5000000, Long.MAX_VALUE);
    
    private final String displayName;
    private final long minSpending;
    private final long maxSpending;
    
    MembershipTier(String displayName, long minSpending, long maxSpending) {
        this.displayName = displayName;
        this.minSpending = minSpending;
        this.maxSpending = maxSpending;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public long getMinSpending() {
        return minSpending;
    }
    
    public long getMaxSpending() {
        return maxSpending;
    }
    
    public static MembershipTier getTierBySpending(long totalSpending) {
        if (totalSpending >= RADIANCE.minSpending) {
            return RADIANCE;
        } else if (totalSpending >= VITALITY.minSpending) {
            return VITALITY;
        } else {
            return ENERGY;
        }
    }
}
