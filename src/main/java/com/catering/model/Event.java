package com.catering.model;

public class Event {
    private int id;
    private int clientId;
    private String clientName;   // only filled when loading from the database
    private String eventName;
    private String venue;
    private String eventDate;    // format: yyyy-MM-dd
    private String startTime;    // format: HH:mm
    private String endTime;      // format: HH:mm
    private int staffRequired;
    private String status = "PLANNED";

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getClientId() { return clientId; }
    public void setClientId(int clientId) { this.clientId = clientId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public int getStaffRequired() { return staffRequired; }
    public void setStaffRequired(int staffRequired) { this.staffRequired = staffRequired; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
        // Lets a JComboBox show the event nicely
    @Override
    public String toString() {
        return eventName + " (" + eventDate + ", " + startTime + "-" + endTime + ")";
    }
        private String reportingTime;   // HH:mm, may be null

    public String getReportingTime() { return reportingTime; }
    public void setReportingTime(String reportingTime) { this.reportingTime = reportingTime; }

    // If no reporting time was set, staff report at the start time
    public String getReportingOrStart() {
        return (reportingTime == null || reportingTime.isEmpty()) ? startTime : reportingTime;
    }
}