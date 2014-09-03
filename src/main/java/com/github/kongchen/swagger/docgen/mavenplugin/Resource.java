package com.github.kongchen.swagger.docgen.mavenplugin;

import java.lang.reflect.Method;

public class Resource {

	private Class<?> controller;
	private Method method;
	private String httpMethod;

	public Resource(Class<?> controller, Method method, String httpMethod) {
		this.controller = controller;
		this.method = method;
		this.httpMethod = httpMethod;
	}

	public Class<?> getController() {
		return controller;
	}

	public Method getMethod() {
		return method;
	}

	public String getHttpMethod() {
		return httpMethod;
	}
}
