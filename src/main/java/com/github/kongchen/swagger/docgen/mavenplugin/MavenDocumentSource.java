package com.github.kongchen.swagger.docgen.mavenplugin;

import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.LogAdapter;
import com.github.kongchen.swagger.docgen.ninjamodel.NinjaToSwaggerModelMapper;
import com.github.kongchen.swagger.docgen.ninjamodel.Resource;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.core.SwaggerSpec;
import com.wordnik.swagger.jaxrs.JaxrsApiReader;
import com.wordnik.swagger.jaxrs.reader.DefaultJaxrsApiReader;
import com.wordnik.swagger.model.*;
import org.apache.maven.plugin.logging.Log;
import scala.Option;
import scala.collection.JavaConversions;
import scala.collection.mutable.Buffer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong
 * 05/13/2013
 */
public class MavenDocumentSource extends AbstractDocumentSource {
	private final ApiSource apiSource;
	private final NinjaToSwaggerModelMapper modelMapper;

	public MavenDocumentSource(ApiSource apiSource, Log log) {
		super(new LogAdapter(log),
				apiSource.getOutputPath(), apiSource.getOutputTemplate(), apiSource.getSwaggerDirectory(), apiSource.mustacheFileRoot, apiSource.isUseOutputFlatStructure(), apiSource.getOverridingModels());

		setApiVersion(apiSource.getApiVersion());
		setBasePath(apiSource.getBasePath() + apiSource.getApiUri());
		setApiInfo(apiSource.getApiInfo());
		this.apiSource = apiSource;
		this.modelMapper = new NinjaToSwaggerModelMapper();
	}

	@Override
	public void loadDocuments() throws GenerateException {
		SwaggerConfig swaggerConfig = new SwaggerConfig();
		swaggerConfig.setApiVersion(apiSource.getApiVersion());
		swaggerConfig.setSwaggerVersion(SwaggerSpec.version());
		List<ApiListingReference> apiListingReferences = new ArrayList<>();

		List<AuthorizationType> authorizationTypes = new ArrayList<>();
		Map<Class<?>, Resource> resources = apiSource.getValidResources();
		for (Class<?> key : resources.keySet()) {
			ApiListing doc;
			try {
				doc = getDocFromClass(resources.get(key), swaggerConfig, getBasePath());
			} catch (Exception e) {
				throw new GenerateException(e);
			}
			if (doc == null) continue;

			Buffer<AuthorizationType> buffer = doc.authorizations().toBuffer();
			authorizationTypes.addAll(JavaConversions.asJavaList(buffer));
			ApiListingReference apiListingReference = new ApiListingReference(doc.resourcePath(), doc.description(), doc.position());
			apiListingReferences.add(apiListingReference);
			acceptDocument(doc);
		}
		// sort apiListingRefernce by position
		Collections.sort(apiListingReferences, new Comparator<ApiListingReference>() {
			@Override
			public int compare(ApiListingReference o1, ApiListingReference o2) {
				if (o1 == null && o2 == null) return 0;
				if (o1 == null && o2 != null) return -1;
				if (o1 != null && o2 == null) return 1;
				return o1.position() - o2.position();
			}
		});
		serviceDocument = new ResourceListing(swaggerConfig.apiVersion(), swaggerConfig.swaggerVersion(),
				scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(apiListingReferences.iterator())),
				scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(authorizationTypes.iterator())),
				toSwaggerApiInfo(apiSource.getApiInfo()));
	}

	private Option<ApiInfo> toSwaggerApiInfo(ApiSourceInfo info) {
		if (info == null) return Option.empty();
		return Option.apply(new ApiInfo(info.getTitle(), info.getDescription(),
				info.getTermsOfServiceUrl(), info.getContact(),
				info.getLicense(), info.getLicenseUrl()));
	}

	private ApiListing getDocFromClass(Resource resource, SwaggerConfig swaggerConfig, String basePath) throws Exception {

		basePath = basePath + apiSource.getApiUri();
        JaxrsApiReader reader = new DefaultJaxrsApiReader();
		ApiListing apiListing = reader.read(basePath, resource.getControllerClass(), swaggerConfig).get();

		apiListing = modelMapper.changeJaxrsApiListingBasingOnNinjaValues(apiListing, resource, swaggerConfig, basePath + apiSource.getApiUri());

		return apiListing;
	}

}
