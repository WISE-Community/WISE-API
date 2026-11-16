package org.wise.portal.domain.peergroup.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.service.peergroup.PeerGroupService;

@Component
public class StringToPeerGroupConverter implements Converter<String, PeerGroup> {
	@Autowired
	private PeerGroupService peerGroupService;

	@Override
	public PeerGroup convert(String id) {
		try {
			return peerGroupService.getById(Long.valueOf(id));
		} catch (ObjectNotFoundException e) {
			throw new IllegalArgumentException("Invalid peer group ID: " + id);
		}
	}
}
