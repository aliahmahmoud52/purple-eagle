package com.maplewood.dto;

public class CourseDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Double credits;
    private Integer hoursPerWeek;
    private String courseType;
    private Integer gradeLevelMin;
    private Integer gradeLevelMax;
    private Integer semesterOrder;
    private Long prerequisiteId;
    private String prerequisiteName;
    private Long sectionId;
    private String days;
    private String startTime;
    private String endTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getCredits() { return credits; }
    public void setCredits(Double credits) { this.credits = credits; }
    public Integer getHoursPerWeek() { return hoursPerWeek; }
    public void setHoursPerWeek(Integer hoursPerWeek) { this.hoursPerWeek = hoursPerWeek; }
    public String getCourseType() { return courseType; }
    public void setCourseType(String courseType) { this.courseType = courseType; }
    public Integer getGradeLevelMin() { return gradeLevelMin; }
    public void setGradeLevelMin(Integer gradeLevelMin) { this.gradeLevelMin = gradeLevelMin; }
    public Integer getGradeLevelMax() { return gradeLevelMax; }
    public void setGradeLevelMax(Integer gradeLevelMax) { this.gradeLevelMax = gradeLevelMax; }
    public Integer getSemesterOrder() { return semesterOrder; }
    public void setSemesterOrder(Integer semesterOrder) { this.semesterOrder = semesterOrder; }
    public Long getPrerequisiteId() { return prerequisiteId; }
    public void setPrerequisiteId(Long prerequisiteId) { this.prerequisiteId = prerequisiteId; }
    public String getPrerequisiteName() { return prerequisiteName; }
    public void setPrerequisiteName(String prerequisiteName) { this.prerequisiteName = prerequisiteName; }
    public Long getSectionId() { return sectionId; }
    public void setSectionId(Long sectionId) { this.sectionId = sectionId; }
    public String getDays() { return days; }
    public void setDays(String days) { this.days = days; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
}
