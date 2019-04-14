package com.csi5175.projecttracker.Entity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ProjectManager {
    private List<Project> projects;
    private List<Project> filteredProjects;
    private static ProjectManager instance = null;

    public static ProjectManager getInstance() {
        if (instance == null) instance = new ProjectManager();
        return instance;
    }

    public List<Project> getProjects() {
        if (filteredProjects != null)
            return filteredProjects;
        return null;
    }

    public List<Project> getFilteredProjects(int type) {
        filteredProjects.clear();
        if (type == 3) {
            filteredProjects.addAll(projects);
            return filteredProjects;
        }
        long cur = Calendar.getInstance().getTimeInMillis();
        for (Project p : projects) {
            if (type == 0) {
                long date = 0;
                try {
                    date = new SimpleDateFormat("yyyy-MM-dd", Locale.CANADA).parse(p.getDue()).getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                long diff = date - cur;
                diff = TimeUnit.HOURS.convert(diff, TimeUnit.MILLISECONDS);
                if (diff > 0 && diff < 48) {
                    filteredProjects.add(p);
                }
            } else if (type == 1 && p.getCompleted()) {
                filteredProjects.add(p);
            } else if (type == 2 && !p.getCompleted()) {
                filteredProjects.add(p);
            }
        }
        return filteredProjects;
    }

    public void setProjects(List<Project> project) {
        this.projects = project;
        this.filteredProjects = new ArrayList<>();
        this.filteredProjects.addAll(project);
    }

    public int getSize() {
        return projects.size();
    }

    public Project getByUid(String string) {
        for (Project p : projects)
            if (p.getUid().equals(string))
                return p;
        return null;
    }
}
