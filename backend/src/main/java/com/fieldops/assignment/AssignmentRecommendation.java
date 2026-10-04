package com.fieldops.assignment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AssignmentRecommendation {

    private Long workOrderId;
    private String workOrderNumber;
    private String priority;
    private CandidateTechnicianScore topCandidate;
    private List<CandidateTechnicianScore> candidates = new ArrayList<>();
    private LocalDateTime generatedAt = LocalDateTime.now();

    public AssignmentRecommendation() {}

    public AssignmentRecommendation(Long workOrderId, String workOrderNumber, String priority,
                                    CandidateTechnicianScore topCandidate, List<CandidateTechnicianScore> candidates) {
        this.workOrderId = workOrderId;
        this.workOrderNumber = workOrderNumber;
        this.priority = priority;
        this.topCandidate = topCandidate;
        this.candidates = candidates != null ? candidates : new ArrayList<>();
        this.generatedAt = LocalDateTime.now();
    }

    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }

    public String getWorkOrderNumber() { return workOrderNumber; }
    public void setWorkOrderNumber(String workOrderNumber) { this.workOrderNumber = workOrderNumber; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public CandidateTechnicianScore getTopCandidate() { return topCandidate; }
    public void setTopCandidate(CandidateTechnicianScore topCandidate) { this.topCandidate = topCandidate; }

    public List<CandidateTechnicianScore> getCandidates() { return candidates; }
    public void setCandidates(List<CandidateTechnicianScore> candidates) { this.candidates = candidates; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}
