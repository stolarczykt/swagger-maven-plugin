package com.github.kongchen.swagger.docgen.util;

import com.github.kongchen.swagger.docgen.mavenplugin.MethodUriInfo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static com.github.kongchen.swagger.docgen.util.Utils.getMethodUriInfo;

@RunWith(Parameterized.class)
public class MethodUriInfoTest {

	private String expectedResourceUri;
	private String expectedMethodUri;
	private String routeUri;
	private String apiUri;

	public MethodUriInfoTest(String expectedResourceUri, String expectedMethodUri, String routeUri, String apiUri) {
		this.expectedResourceUri = expectedResourceUri;
		this.expectedMethodUri = expectedMethodUri;
		this.routeUri = routeUri;
		this.apiUri = apiUri;
	}

	@Parameterized.Parameters
	public static Collection parameters() {
		return Arrays.asList(new Object[][]{
				{"", "", "/backend", "/backend"},
				{"/tasks", "", "/backend/tasks", "/backend"},
				{"/tasks", "/{id}", "/backend/tasks/{id}", "/backend"},
				{"/tasks", "/{id}/sth", "/backend/tasks/{id}/sth", "/backend"}
		});
	}

	@Test
	public void should_retrieve_correct_resource_uri_and_method_uri_from_route_uri() throws Exception {
		MethodUriInfo methodUriInfo = getMethodUriInfo(routeUri, apiUri);
		Assert.assertEquals(expectedResourceUri, methodUriInfo.getResourceUri());
		Assert.assertEquals(expectedMethodUri, methodUriInfo.getMethodUri());
	}
}