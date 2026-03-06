package com.maplewood.dto;

public class EnrollmentResponse {
    private boolean success;
    private String message;
    private String errorType;
    private ScheduleItemDTO enrollment;

    public static EnrollmentResponse error(String errorType, String message) {
        EnrollmentResponse r = new EnrollmentResponse();
        r.success = false;
        r.errorType = errorType;
        r.message = message;
        return r;
    }

    public static EnrollmentResponse success(String message, ScheduleItemDTO item) {
        EnrollmentResponse r = new EnrollmentResponse();
        r.success = true;
        r.message = message;
        r.enrollment = item;
        return r;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getErrorType() { return errorType; }
    public void setErrorType(String errorType) { this.errorType = errorType; }
    public ScheduleItemDTO getEnrollment() { return enrollment; }
    public void setEnrollment(ScheduleItemDTO enrollment) { this.enrollment = enrollment; }
}
