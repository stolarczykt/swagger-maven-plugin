package com.github.kongchen.swagger.docgen.mavenplugin;

import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.ninjamodel.MethodUriInfo;
import com.github.kongchen.swagger.docgen.ninjamodel.Resource;
import com.github.kongchen.swagger.docgen.ninjamodel.RouteMethod;
import com.github.kongchen.swagger.docgen.ninjamodel.RouteMethodFactory;
import com.github.kongchen.swagger.docgen.reflection.ClassMember;
import com.wordnik.swagger.annotations.Api;
import ninja.RouteBuilder;
import ninja.RouterImpl;
import ninja.application.ApplicationRoutes;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.kongchen.swagger.docgen.util.Utils.getMethodUriInfo;

/**
 * Created with IntelliJ IDEA.
 * User: kongchen
 * Date: 3/7/13
 */
public class ApiSource {

	/**
	 * Java classes containing Swagger's annotation <code>@Api</code>, or Java packages containing those classes
	 * can be configured here, use ; as the delimiter if you have more than one location.
	 */
	@Parameter(required = true)
	private String routesClass;

	@Parameter(name = "apiInfo", required = false)
	private ApiSourceInfo apiInfo;

	/**
	 * The version of your APIs
	 */
	@Parameter(required = true)
	private String apiVersion;

	/**
	 * The hostUrl of your APIs.
	 */
	@Parameter(required = true)
	private String hostUrl;

	@Parameter(required = true)
	private String apiUri;

	/**
	 * <code>outputTemplate</code> is the path of a mustache template file,
	 * see more details in next section.
	 * If you don't want to generate extra api documents, just don't set it.
	 */
	@Parameter(required = false)
	private String outputTemplate;

	@Parameter
	private String outputPath;

	@Parameter
	private String swaggerDirectory;

	@Parameter
	public String mustacheFileRoot;

	@Parameter
	public boolean useOutputFlatStructure = true;

	@Parameter
	private String swaggerUIDocBasePath;

	@Parameter
	private String overridingModels;

	public Map<Class<?>, Resource> getValidResources() throws GenerateException {

		List<RouteBuilder> routeBuilders = getRouteBuilders();

		return buildResourcesFrom(routeBuilders);
	}

	private Map<Class<?>, Resource> buildResourcesFrom(List<RouteBuilder> routeBuilders) {
		Map<Class<?>, Resource> resources = new HashMap<>();
		RouteMethodFactory routeMethodFactory = new RouteMethodFactory();
		for (RouteBuilder routeBuilder : routeBuilders) {
			Class resourceClass = getResourceClass(routeBuilder);
			if (resourceClass != null) {
				if (resourceClass.isAnnotationPresent(Api.class)) {

					MethodUriInfo methodUriInfo = getMethodUriInfoFrom(routeBuilder);
					RouteMethod routeMethod = routeMethodFactory.createFrom(routeBuilder, methodUriInfo.getMethodUri());

					if (resources.containsKey(resourceClass)) {
						//TO-DO check if resource URI is the same
						Resource resource = resources.get(resourceClass);
						resource.addRouteMethod(routeMethod);
					} else {
						Resource resource = new Resource(resourceClass, methodUriInfo.getResourceUri());
						resource.addRouteMethod(routeMethod);
						resources.put(resourceClass, resource);
					}
				}
			}
		}
		return resources;
	}

	private MethodUriInfo getMethodUriInfoFrom(RouteBuilder routeBuilder) {
		ClassMember<String> uriField = new ClassMember<>(routeBuilder, "uri");
		String uri = uriField.getValue();
		return getMethodUriInfo(uri, apiUri);
	}

	private Class getResourceClass(RouteBuilder routeBuilder) {
		ClassMember<Class> controllerField = new ClassMember<>(routeBuilder, "controller");
		return controllerField.getValue();
	}

	private List<RouteBuilder> getRouteBuilders() {
		RouterImpl router = getRouterInstance();
		ClassMember<List<RouteBuilder>> routeBuilders = new ClassMember<>(router, "allRouteBuilders");
		return routeBuilders.getValue();
	}

	private RouterImpl getRouterInstance() {
		RouterImpl router;
		try {
			ApplicationRoutes applicationRoutes = (ApplicationRoutes) Class.forName(routesClass).newInstance();
			router = new RouterImpl(null, null);
			applicationRoutes.init(router);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		return router;
	}

	public ApiSourceInfo getApiInfo() {
		return apiInfo;
	}

	public void setApiInfo(ApiSourceInfo apiInfo) {
		this.apiInfo = apiInfo;
	}

	public String getRoutesClass() {
		return routesClass;
	}

	public void setRoutesClass(String routesClass) {
		this.routesClass = routesClass;
	}

	public String getOutputTemplate() {
		return outputTemplate;
	}

	public void setOutputTemplate(String outputTemplate) {
		this.outputTemplate = outputTemplate;
	}

	public String getMustacheFileRoot() {
		return mustacheFileRoot;
	}

	public void setMustacheFileRoot(String mustacheFileRoot) {
		this.mustacheFileRoot = mustacheFileRoot;
	}

	public boolean isUseOutputFlatStructure() {
		return useOutputFlatStructure;
	}

	public void setUseOutputFlatStructure(boolean useOutputFlatStructure) {
		this.useOutputFlatStructure = useOutputFlatStructure;
	}

	public String getOutputPath() {
		return outputPath;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}

	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	public String getHostUrl() {
		return hostUrl;
	}

	public void setHostUrl(String hostUrl) {
		this.hostUrl = hostUrl;
	}

	public String getSwaggerDirectory() {
		return swaggerDirectory;
	}

	public void setSwaggerDirectory(String swaggerDirectory) {
		this.swaggerDirectory = swaggerDirectory;
	}

	public void setSwaggerUIDocBasePath(String swaggerUIDocBasePath) {
		this.swaggerUIDocBasePath = swaggerUIDocBasePath;
	}

	public String getSwaggerUIDocBasePath() {
		return swaggerUIDocBasePath;
	}

	public String getOverridingModels() {
		return overridingModels;
	}

	public void setOverridingModels(String overridingModels) {
		this.overridingModels = overridingModels;
	}

	public String getApiUri() {
		return apiUri;
	}

	public void setApiUri(String apiUri) {
		this.apiUri = apiUri;
	}
}
