package com.github.kongchen.swagger.docgen.mavenplugin;

import java.lang.reflect.Method;

public class RouteMethod {

	private String httpMethod;
	private String uri;
	private Method controllerMethod;

	public RouteMethod(String uri, String httpMethod, Method controllerMethod) {
		this.uri = uri;
		this.httpMethod = httpMethod;
		this.controllerMethod = controllerMethod;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public String getUri() {
		return uri;
	}

	public Method getControllerMethod() {
		return controllerMethod;
	}
}
