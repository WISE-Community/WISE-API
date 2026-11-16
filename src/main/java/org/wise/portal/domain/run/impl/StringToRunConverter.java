package org.wise.portal.domain.run.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.run.Run;
import org.wise.portal.service.run.RunService;

@Component
public class StringToRunConverter implements Converter<String, Run> {
	@Autowired
	private RunService runService;

	@Override
	public Run convert(String id) {
		try {
			return runService.retrieveById(Long.parseLong(id));
		} catch (ObjectNotFoundException e) {
			throw new IllegalArgumentException("Invalid project ID: " + id);
		}
	}
}
