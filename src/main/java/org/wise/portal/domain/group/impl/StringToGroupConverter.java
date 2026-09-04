package org.wise.portal.domain.group.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.group.Group;
import org.wise.portal.service.group.GroupService;

@Component
public class StringToGroupConverter implements Converter<String, Group> {

	@Autowired
	private GroupService groupService;

	@Override
	public Group convert(String id) {
		try {
			return groupService.retrieveById(Long.parseLong(id));
		} catch (ObjectNotFoundException e) {
			throw new IllegalArgumentException("Invalid group ID: " + id);
		}
	}
}
