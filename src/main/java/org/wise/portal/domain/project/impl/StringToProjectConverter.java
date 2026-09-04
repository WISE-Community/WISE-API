package org.wise.portal.domain.project.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.project.Project;
import org.wise.portal.service.project.ProjectService;

@Component
public class StringToProjectConverter implements Converter<String, Project> {
    @Autowired
    private ProjectService projectService;

    @Override
    public Project convert(String id) {
        try {
            return projectService.getById(Long.parseLong(id));
        } catch (ObjectNotFoundException e) {
            throw new IllegalArgumentException("Invalid project ID: " + id);
        }
    }
}