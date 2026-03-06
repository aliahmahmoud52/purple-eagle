package com.maplewood.model;

import jakarta.persistence.*;

@Entity
@Table(name = "courses")
public class Course {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true) private String code;
    @Column(nullable = false) private String name;
    private String description;
    @Column(nullable = false) private Double credits;
    @Column(name = "hours_per_week", nullable = false) private Integer hoursPerWeek;
    @Column(name = "specialization_id", nullable = false) private Long specializationId;
    @Column(name = "prerequisite_id", insertable = false, updatable = false) private Long prerequisiteId;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "prerequisite_id") private Course prerequisite;
    @Column(name = "course_type", nullable = false) private String courseType;
    @Column(name = "grade_level_min") private Integer gradeLevelMin;
    @Column(name = "grade_level_max") private Integer gradeLevelMax;
    @Column(name = "semester_order", nullable = false) private Integer semesterOrder;

    public Course() {}
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
    public Long getSpecializationId() { return specializationId; }
    public void setSpecializationId(Long specializationId) { this.specializationId = specializationId; }
    public Long getPrerequisiteId() { return prerequisiteId; }
    public Course getPrerequisite() { return prerequisite; }
    public void setPrerequisite(Course prerequisite) { this.prerequisite = prerequisite; }
    public String getCourseType() { return courseType; }
    public void setCourseType(String courseType) { this.courseType = courseType; }
    public Integer getGradeLevelMin() { return gradeLevelMin; }
    public void setGradeLevelMin(Integer gradeLevelMin) { this.gradeLevelMin = gradeLevelMin; }
    public Integer getGradeLevelMax() { return gradeLevelMax; }
    public void setGradeLevelMax(Integer gradeLevelMax) { this.gradeLevelMax = gradeLevelMax; }
    public Integer getSemesterOrder() { return semesterOrder; }
    public void setSemesterOrder(Integer semesterOrder) { this.semesterOrder = semesterOrder; }
}
