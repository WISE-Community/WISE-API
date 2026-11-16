package org.wise.portal.domain.workgroup.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.workgroup.WorkgroupService;
import org.wise.portal.dao.ObjectNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class StringToWorkgroupConverter implements Converter<String, Workgroup> {
	@Autowired
	private WorkgroupService workgroupService;

	@Override
	public Workgroup convert(String id) {
		try {
			return workgroupService.retrieveById(Long.parseLong(id));
		} catch (ObjectNotFoundException e) {
			throw new IllegalArgumentException("Invalid workgroup ID: " + id);
		}
	}
}
