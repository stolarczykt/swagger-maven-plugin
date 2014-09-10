package com.github.kongchen.swagger.docgen.mavenplugin;

import com.github.kongchen.swagger.docgen.GenerateException;
import com.wordnik.swagger.annotations.Api;
import ninja.RouteBuilder;
import ninja.RouterImpl;
import ninja.application.ApplicationRoutes;
import org.apache.maven.plugins.annotations.Parameter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

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
	 * The basePath of your APIs.
	 */
	@Parameter(required = true)
	private String basePath;

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

	public Map<Class<?>, Resource> getValidClasses() throws GenerateException {

		Map<Class<?>, Resource> resources = new HashMap<>();

		try {
			ApplicationRoutes applicationRoutes = (ApplicationRoutes) Class.forName(routesClass).newInstance();
			RouterImpl router = new RouterImpl(null, null);
			applicationRoutes.init(router);

			Field allRouteBuildersField = router.getClass().getDeclaredField("allRouteBuilders");
			allRouteBuildersField.setAccessible(true);
			List<RouteBuilder> routeBuilders = (List<RouteBuilder>) allRouteBuildersField.get(router);

			for (RouteBuilder routeBuilder : routeBuilders) {

				Field controllerField = routeBuilder.getClass().getDeclaredField("controller");
				controllerField.setAccessible(true);
				Class controllerClass = (Class) controllerField.get(routeBuilder);
				if (controllerClass != null) {
					if (controllerClass.isAnnotationPresent(Api.class)) {

						Field httpMethodField = routeBuilder.getClass().getDeclaredField("httpMethod");
						httpMethodField.setAccessible(true);
						String httpMethod = (String) httpMethodField.get(routeBuilder);

						Field uriField = routeBuilder.getClass().getDeclaredField("uri");
						uriField.setAccessible(true);
						String uri = (String) uriField.get(routeBuilder);
						MethodUriInfo methodUriInfo = getMethodUriInfo(uri, apiUri);

						Field methodField = routeBuilder.getClass().getDeclaredField("controllerMethod");
						methodField.setAccessible(true);
						Method method = (Method) methodField.get(routeBuilder);

						RouteMethod routeMethod = new RouteMethod(methodUriInfo.getMethodUri(), httpMethod, method);

						if (resources.containsKey(controllerClass)) {
							//TO-DO check if resource URI is the same
							Resource resource = resources.get(controllerClass);
							resource.addRouteMethod(routeMethod);
						} else {
							Resource resource = new Resource(controllerClass, methodUriInfo.getResourceUri());
							resource.addRouteMethod(routeMethod);
							resources.put(controllerClass, resource);
						}
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return resources;
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

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
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
