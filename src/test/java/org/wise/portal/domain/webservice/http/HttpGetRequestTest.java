/**
 * Copyright (c) 2007 Encore Research Group, University of Toronto
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.wise.portal.domain.webservice.http;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.easymock.EasyMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.wise.vle.domain.webservice.HttpStatusCodeException;
import org.wise.vle.domain.webservice.http.AbstractHttpRequest;
import org.wise.vle.domain.webservice.http.HttpGetRequest;

public class HttpGetRequestTest {

	private static final String URL = "/curnit";

	protected AbstractHttpRequest request;

	protected HttpMethod method;

  @BeforeEach
  public void setUp() throws Exception {
		method = EasyMock.createMock(HttpMethod.class);
		request = new HttpGetRequest(
				new HashMap<String, String>(1),
				new HashMap<String, String>(1), URL, HttpStatus.SC_OK);
	}

  @AfterEach
  public void tearDown() throws Exception {
		method = null;
		request = null;
	}

  @Test
  public void testIsValidResponseStatus_shouldThrowHttpStatusCodeException()
      throws Exception {
		EasyMock.expect(method.getStatusText()).andReturn("whatever")
				.anyTimes();
		EasyMock.expect(method.getResponseBodyAsString()).andReturn("whatever")
				.anyTimes();
		EasyMock.replay(method);
		try {
			request.isValidResponseStatus(method, HttpStatus.SC_CONFLICT);
			fail("Expected HttpStatusCodeException to be thrown");
		} catch (HttpStatusCodeException e) {
		}
		EasyMock.verify(method);
	}

  @Test
  public void testIsValidResponseStatus() throws Exception {
		EasyMock.replay(method);
		assertTrue(request.isValidResponseStatus(method, request
				.getExpectedResponseStatusCode()));
		EasyMock.verify(method);
	}

}
