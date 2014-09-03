package com.github.kongchen.swagger.docgen.mavenplugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Resource {

	private Class<?> controllerClass;
	private String resourceUri;
	private List<RouteMethod> routeMethods;

	public Resource(Class<?> controllerClass, String resourceUri) {
		this.controllerClass = controllerClass;
		this.resourceUri = resourceUri;
		this.routeMethods = new ArrayList<>();
	}

	public Class<?> getControllerClass() {
		return controllerClass;
	}

	public String getResourceUri() {
		return resourceUri;
	}

	public List<RouteMethod> getRouteMethods() {
		return Collections.unmodifiableList(routeMethods);
	}

	public void addRouteMethod(RouteMethod routeMethod) {
		routeMethods.add(routeMethod);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Resource resource = (Resource) o;

		return controllerClass.equals(resource.controllerClass);
	}

	@Override
	public int hashCode() {
		int result = controllerClass.hashCode();
		return 31 * result;
	}
}
