package com.github.kongchen.swagger.docgen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.kongchen.swagger.docgen.mavenplugin.ApiSourceInfo;
import com.wordnik.swagger.converter.ModelConverters;
import com.wordnik.swagger.converter.OverrideConverter;
import com.wordnik.swagger.core.util.JsonSerializer;
import com.wordnik.swagger.core.util.JsonUtil;
import com.wordnik.swagger.model.ApiListing;
import com.wordnik.swagger.model.ApiListingReference;
import com.wordnik.swagger.model.ResourceListing;
import org.apache.commons.io.FileUtils;
import scala.collection.Iterator;
import scala.collection.JavaConversions;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong 05/13/2013
 */
public abstract class AbstractDocumentSource {

	protected final LogAdapter LOG;

	private final String outputPath;

	private final String templatePath;

	private final String mustacheFileRoot;

	private final String swaggerPath;

	protected ResourceListing serviceDocument;

	List<ApiListing> validDocuments = new ArrayList<ApiListing>();

	private String basePath;

	private String apiVersion;

	private ApiSourceInfo apiInfo;

	private ObjectMapper mapper = new ObjectMapper();

	private boolean useOutputFlatStructure;

	private String overridingModels;

	public AbstractDocumentSource(LogAdapter logAdapter, String outputPath,
	                              String outputTpl, String swaggerOutput, String mustacheFileRoot,
	                              boolean useOutputFlatStructure1, String overridingModels) {
		LOG = logAdapter;
		this.outputPath = outputPath;
		this.templatePath = outputTpl;
		this.mustacheFileRoot = mustacheFileRoot;
		this.useOutputFlatStructure = useOutputFlatStructure1;
		this.swaggerPath = swaggerOutput;
		this.overridingModels = overridingModels;
	}

	public abstract void loadDocuments() throws Exception, GenerateException;

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	public ApiSourceInfo getApiInfo() {
		return apiInfo;
	}

	public void setApiInfo(ApiSourceInfo apiInfo) {
		this.apiInfo = apiInfo;
	}

	protected void acceptDocument(ApiListing doc) {
		String basePath;
		// will append api's basePath. However, apiReader does not read it
		// correctly by now
		if (doc.basePath() != null) {
			basePath = this.basePath + doc.basePath();
		} else {
			basePath = this.basePath;
		}
		ApiListing newDoc = new ApiListing(doc.apiVersion(),
				doc.swaggerVersion(), basePath, doc.resourcePath(),
				doc.produces(), doc.consumes(), doc.protocols(),
				doc.authorizations(), doc.apis(), doc.models(),
				doc.description(), doc.position());
		validDocuments.add(newDoc);
	}

	public List<ApiListing> getValidDocuments() {
		return validDocuments;
	}

	public void toSwaggerDocuments(String swaggerUIDocBasePath)
			throws GenerateException {
		if (swaggerPath == null) {
			return;
		}
		File dir = new File(swaggerPath);
		if (dir.isFile()) {
			throw new GenerateException(String.format(
					"Swagger-outputDirectory[%s] must be a directory!",
					swaggerPath));
		}

		if (!dir.exists()) {
			try {
				FileUtils.forceMkdir(dir);
			} catch (IOException e) {
				throw new GenerateException(String.format(
						"Create Swagger-outputDirectory[%s] failed.",
						swaggerPath));
			}
		}
		cleanupOlds(dir);

		prepareServiceDocument();
		// rewrite basePath in swagger-ui output file using the value in
		// configuration file.
		writeInDirectory(dir, serviceDocument, swaggerUIDocBasePath);
		for (ApiListing doc : validDocuments) {
			writeInDirectory(dir, doc, basePath);
		}
	}

	public void loadOverridingModels() throws GenerateException {
		if (overridingModels != null) {
			try {
				JsonNode readTree = mapper.readTree(this.getClass()
						.getResourceAsStream(overridingModels));
				for (JsonNode jsonNode : readTree) {
					JsonNode classNameNode = jsonNode.get("className");
					String className = classNameNode.asText();
					JsonNode jsonStringNode = jsonNode.get("jsonString");
					String jsonString = jsonStringNode.asText();
					OverrideConverter converter = new OverrideConverter();
					converter.add(className, jsonString);
					ModelConverters.addConverter(converter, true);
				}
			} catch (JsonProcessingException e) {
				throw new GenerateException(
						String.format(
								"Swagger-overridingModels[%s] must be a valid JSON file!",
								overridingModels), e);
			} catch (IOException e) {
				throw new GenerateException(String.format(
						"Swagger-overridingModels[%s] not found!",
						overridingModels), e);
			}
		}
	}

	private void cleanupOlds(File dir) {
		if (dir.listFiles() != null) {
			for (File f : dir.listFiles()) {
				if (f.getName().endsWith("json")) {
					f.delete();
				}
			}
		}
	}

	private void prepareServiceDocument() {
		List<ApiListingReference> apiListingReferences = new ArrayList<ApiListingReference>();
		for (Iterator<ApiListingReference> iterator = serviceDocument.apis()
				.iterator(); iterator.hasNext(); ) {
			ApiListingReference apiListingReference = iterator.next();
			String newPath = apiListingReference.path();
			if (useOutputFlatStructure) {
				newPath = newPath.replaceAll("/", "_");
				if (newPath.startsWith("_")) {
					newPath = "/" + newPath.substring(1);
				}
			}
			newPath += ".{format}";
			apiListingReferences.add(new ApiListingReference(newPath,
					apiListingReference.description(), apiListingReference
					.position()));
		}
		// there's no setter of path for ApiListingReference, we need to create
		// a new ResourceListing for new path
		serviceDocument = new ResourceListing(serviceDocument.apiVersion(),
				serviceDocument.swaggerVersion(),
				scala.collection.immutable.List.fromIterator(JavaConversions
						.asScalaIterator(apiListingReferences.iterator())),
				serviceDocument.authorizations(), serviceDocument.info());
	}

	protected String resourcePathToFilename(String resourcePath) {
		if (resourcePath == null) {
			return "service.json";
		}
		String name = resourcePath;
		if (name.startsWith("/")) {
			name = name.substring(1);
		}
		if (name.endsWith("/")) {
			name = name.substring(0, name.length() - 1);
		}

		if (useOutputFlatStructure) {
			name = name.replaceAll("/", "_");
		}

		return name + ".json";
	}

	private void writeInDirectory(File dir, ApiListing apiListing,
	                              String basePath) throws GenerateException {
		String filename = resourcePathToFilename(apiListing.resourcePath());
		try {
			File serviceFile = createFile(dir, filename);
			String json = JsonSerializer.asJson(apiListing);
			JsonNode tree = mapper.readTree(json);
			if (basePath != null) {
				((ObjectNode) tree).put("basePath", basePath);
			}
			JsonUtil.mapper().writerWithDefaultPrettyPrinter()
					.writeValue(serviceFile, tree);
		} catch (IOException e) {
			throw new GenerateException(e);
		}
	}

	private void writeInDirectory(File dir, ResourceListing resourceListing,
	                              String basePath) throws GenerateException {
		String filename = resourcePathToFilename(null);
		try {
			File serviceFile = createFile(dir, filename);
			String json = JsonSerializer.asJson(resourceListing);
			JsonNode tree = mapper.readTree(json);
			if (basePath != null) {
				((ObjectNode) tree).put("basePath", basePath);
			}

			JsonUtil.mapper().writerWithDefaultPrettyPrinter()
					.writeValue(serviceFile, tree);
		} catch (IOException e) {
			throw new GenerateException(e);
		}
	}

	protected File createFile(File dir, String outputResourcePath)
			throws IOException {
		File serviceFile;
		int i = outputResourcePath.lastIndexOf("/");
		if (i != -1) {
			String fileName = outputResourcePath.substring(i + 1);
			String subDir = outputResourcePath.substring(0, i);
			File finalDirectory = new File(dir, subDir);
			finalDirectory.mkdirs();
			serviceFile = new File(finalDirectory, fileName);
		} else {
			serviceFile = new File(dir, outputResourcePath);
		}
		while (!serviceFile.createNewFile()) {
			serviceFile.delete();
		}
		LOG.info("Creating file " + serviceFile.getAbsolutePath());
		return serviceFile;
	}


	private URI getTemplateUri() throws GenerateException {
		URI uri;
		try {
			uri = new URI(templatePath);
		} catch (URISyntaxException e) {
			File file = new File(templatePath);
			if (!file.exists()) {
				throw new GenerateException(
						"Template "
								+ file.getAbsoluteFile()
								+ " not found. You can go to https://github.com/kongchen/api-doc-template to get templates.");
			}
			uri = file.toURI();
		}
		if (!uri.isAbsolute()) {
			File file = new File(templatePath);
			if (!file.exists()) {
				throw new GenerateException(
						"Template "
								+ file.getAbsoluteFile()
								+ " not found. You can go to https://github.com/kongchen/api-doc-template to get templates.");
			} else {
				uri = new File(templatePath).toURI();
			}
		}
		return uri;
	}


}
