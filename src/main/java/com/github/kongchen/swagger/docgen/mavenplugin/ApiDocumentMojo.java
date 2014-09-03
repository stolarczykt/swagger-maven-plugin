package com.github.kongchen.swagger.docgen.mavenplugin;

import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.GenerateException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kongchen
 * Date: 3/7/13
 */
@Mojo( name = "generate", defaultPhase = LifecyclePhase.COMPILE, configurator = "include-project-dependencies",
       requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class ApiDocumentMojo extends AbstractMojo {

    /**
     * A set of apiSources.
     * One apiSource can be considered as a set of APIs for one apiVersion in a basePath
     * 
     */
    @Parameter
    private List<ApiSource> apiSources;

    public List<ApiSource> getApiSources() {
        return apiSources;
    }

    public void setApiSources(List<ApiSource> apiSources) {
        this.apiSources = apiSources;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (apiSources == null) {
            throw new MojoFailureException("You must configure at least one apiSources element");
        }
        if (useSwaggerSpec11()) {
            throw new MojoExecutionException("You may use an old version of swagger which is not supported by swagger-maven-plugin 2.0+\n" +
                "swagger-maven-plugin 2.0+ only supports swagger-core 1.3.x");
        }

        try {
//	        for (ApiSource apiSource : apiSources) {
//		        ApplicationRoutes applicationRoutes = (ApplicationRoutes) Class.forName(apiSource.getLocations()).newInstance();
//		        RouterImpl router = new RouterImpl(null, null);
//		        applicationRoutes.init(router);
//
//		        Field allRouteBuildersField = router.getClass().getDeclaredField("allRouteBuilders");
//		        allRouteBuildersField.setAccessible(true);
//		        List<RouteBuilder> routeBuilders = (List<RouteBuilder>) allRouteBuildersField.get(router);
//
//		        for (RouteBuilder routeBuilder : routeBuilders) {
//
//			        Field controllerField = routeBuilder.getClass().getDeclaredField("controller");
//			        controllerField.setAccessible(true);
//			        Class controllerClass = (Class) controllerField.get(routeBuilder);
//			        if (controllerClass != null) {
//				        if (controllerClass.isAnnotationPresent(Api.class)) {
//					        Api apiAnnotation = (Api) controllerClass.getAnnotation(Api.class);
//					        Field controllerMethodField = routeBuilder.getClass().getDeclaredField("controllerMethod");
//					        controllerMethodField.setAccessible(true);
//					        Method controllerMethod = (Method) controllerMethodField.get(routeBuilder);
//					        if (controllerMethod != null) {
//						        if (controllerMethod.isAnnotationPresent(ApiOperation.class)) {
//							        ApiOperation apiOperationAnnotation = controllerMethod.getAnnotation(ApiOperation.class);
//							        System.out.println("*****>Api controller: " + controllerClass.getName() + ", desc: " + apiAnnotation.description());
//							        System.out.println("***>Api method: " + controllerMethod.getName());
//							        System.out.println("**>Value: " + apiOperationAnnotation.value());
//							        System.out.println("**>Response: " + apiOperationAnnotation.response().getName());
//						        }
//					        }
//				        }
//			        }
//		        }
//	        }


//		        System.out.println("======> " + routeBuilder);
//		        Field httpMethodField = routeBuilder.getClass().getDeclaredField("httpMethod");
//		        httpMethodField.setAccessible(true);
//		        System.out.println("====> Http method: " + httpMethodField.get(routeBuilder));
//
//		        Field uriField = routeBuilder.getClass().getDeclaredField("uri");
//		        uriField.setAccessible(true);
//		        System.out.println("====> Uri: " + uriField.get(routeBuilder));
//
//		        System.out.println("====> Controller class: " + ((controllerField.get(routeBuilder) == null) ? "null": ((Class) controllerField.get(routeBuilder)).getName()));
//
//		        Field controllerMethodField = routeBuilder.getClass().getDeclaredField("controllerMethod");
//		        controllerMethodField.setAccessible(true);
//		        System.out.println("====> Controller method: " + ((controllerMethodField.get(routeBuilder) == null) ? "null": ((Method) controllerMethodField.get(routeBuilder)).getName()));
//
//		        Field resultField = routeBuilder.getClass().getDeclaredField("result");
//		        resultField.setAccessible(true);
//		        System.out.println("====> Result: " + resultField.get(routeBuilder));


            for (ApiSource apiSource : apiSources) {

                AbstractDocumentSource documentSource = new MavenDocumentSource(apiSource, getLog());
                documentSource.loadOverridingModels();
                documentSource.loadDocuments();
				if (apiSource.getOutputPath() != null){
					File outputDirectory = new File(apiSource.getOutputPath()).getParentFile();
					if (outputDirectory != null && !outputDirectory.exists()) {
						if (!outputDirectory.mkdirs()) {
							throw new MojoExecutionException("Create directory[" +
									apiSource.getOutputPath() + "] for output failed.");
						}
					}
				}
                documentSource.toSwaggerDocuments(
                        apiSource.getSwaggerUIDocBasePath() == null
                                ? apiSource.getBasePath()
                                : apiSource.getSwaggerUIDocBasePath());
            }

        } catch (Exception e) {
            throw new MojoFailureException(e.getMessage(), e);
        } catch (GenerateException e) {
	        throw new RuntimeException(e);
        }
    }

    private boolean useSwaggerSpec11() {
        try {
            Class<?> tryClass = Class.forName("com.wordnik.swagger.annotations.ApiErrors");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
