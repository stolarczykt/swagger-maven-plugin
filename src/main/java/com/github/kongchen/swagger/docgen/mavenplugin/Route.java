package com.github.kongchen.swagger.docgen.mavenplugin;

public class Route {

	private String httpMethod;
	private String uri;

	public Route(String httpMethod, String uri) {
		this.httpMethod = httpMethod;
		this.uri = uri;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public String getUri() {
		return uri;
	}
}
