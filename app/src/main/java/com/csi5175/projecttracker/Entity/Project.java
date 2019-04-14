package com.csi5175.projecttracker.Entity;

import android.content.Context;

import com.csi5175.projecttracker.R;

import org.json.JSONObject;

public class Project {
    private String uid;
    private String courseTitle;
    private String courseNumber;
    private String instructorName;
    private String projectNumber;
    private String projectDescription;
    private String due;
    private Boolean complete;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getCourseNumber() {
        return courseNumber;
    }

    public void setCourseNumber(String courseNumber) {
        this.courseNumber = courseNumber;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }

    public String getProjectNumber() {
        return projectNumber;
    }

    public void setProjectNumber(String projectNumber) {
        this.projectNumber = projectNumber;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public String getDue() {
        return due;
    }

    public void setDue(String due) {
        this.due = due;
    }

    public boolean getCompleted() {
        return complete;
    }

    public void setComplete(Boolean complete) {
        this.complete = complete;
    }

    public String toJson(Context context) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(context.getString(R.string.course_title), getCourseTitle());
            jsonObject.put(context.getString(R.string.course_number), getCourseNumber());
            jsonObject.put(context.getString(R.string.project_number), getProjectNumber());
            jsonObject.put(context.getString(R.string.project_description), getProjectDescription());
            jsonObject.put(context.getString(R.string.instructor_name), getInstructorName());
            jsonObject.put(context.getString(R.string.dueDate), getDue());
            jsonObject.put(context.getString(R.string.complete), getCompleted());
            jsonObject.put(context.getString(R.string.project_uid), getUid());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
}
