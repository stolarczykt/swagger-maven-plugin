package com.github.kongchen.swagger.docgen.ninjamodel;

public class MethodUriInfo {

	private String resourceUri;
	private String methodUri;

	public MethodUriInfo(String resourceUri, String methodUri) {
		this.resourceUri = resourceUri;
		this.methodUri = methodUri;
	}

	public String getResourceUri() {
		return resourceUri;
	}

	public String getMethodUri() {
		return methodUri;
	}
}
