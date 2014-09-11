# Ninja Swagger Maven Plugin
This plugin is created based on [Swager Maven Plugin](https://github.com/kongchen/swagger-maven-plugin), and lets your
[Ninja](http://www.ninjaframework.org/) project generate **Swagger JSON** and your **customized API documents** in build phase.

Minimal plugin configuration:

```xml
<build>
...
	<plugins>
	...
		<plugin>
			<groupId>com.github.kongchen</groupId>
			<artifactId>swagger-maven-plugin</artifactId>
			<version>2.3.1-SNAPSHOT</version>
			<configuration>
				<apiSources>
					<apiSource>
						<routesClass>conf.Routes</routesClass>
						<apiVersion>v1</apiVersion>
						<hostUrl>http://localhost:8080</hostUrl>
						<apiUri>/backend</apiUri>
						<swaggerDirectory>src/main/java/assets/swagger</swaggerDirectory>
						<swaggerUIDocBasePath>http://localhost:8080/swagger</swaggerUIDocBasePath>
					</apiSource>
				</apiSources>
			</configuration>
			<executions>
				<execution>
					<phase>compile</phase>
					<goals>
						<goal>generate</goal>
					</goals>
				</execution>
			</executions>
		</plugin>
	...
	</plugins>
...
</build>
```