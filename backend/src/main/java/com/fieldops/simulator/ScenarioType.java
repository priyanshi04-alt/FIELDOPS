package com.fieldops.simulator;

public enum ScenarioType {
    TECHNICIAN_UNAVAILABLE,       // e.g. sick leave, emergency vehicle breakdown
    TECHNICIAN_DELAYED,           // e.g. 60-minute traffic / facility access delay
    URGENT_JOB_INJECTED,          // sudden critical ticket injection
    INVENTORY_STOCKOUT_INJECTED,  // critical replacement part unavailable on van
    TECHNICIAN_REASSIGNED,        // shift reassignment across regions
    BULK_RESEQUENCE               // itinerary re-ordering test
}
