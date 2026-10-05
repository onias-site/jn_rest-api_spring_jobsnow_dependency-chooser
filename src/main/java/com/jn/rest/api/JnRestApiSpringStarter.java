package com.jn.rest.api;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.CcpEntityOperationType;
import com.ccp.implementations.cache.gcp.memcache.CcpGcpMemCache;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.email.sendgrid.CcpSendGridEmailSender;
import com.ccp.implementations.file.bucket.gcp.CcpGcpFileBucket;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.instant.messenger.telegram.CcpTelegramInstantMessenger;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.main.authentication.gcp.oauth.CcpGcpMainAuthentication;
import com.ccp.implementations.mensageria.sender.gcp.pubsub.CcpGcpPubSubMensageriaSender;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.ccp.rest.api.spring.exceptions.handler.CcpRestApiExceptionHandlerSpring;
import com.ccp.rest.api.spring.servlet.filters.CcpPutSessionValuesAndExecuteTaskFilter;
import com.ccp.rest.api.spring.servlet.filters.CcpValidEmailFilter;
import com.ccp.rest.api.utils.CcpRestApiUtils;
import com.jn.entities.JnEntityJobsnowError;
import com.jn.rest.api.endpoints.JnRestApiLogin;
import com.jn.services.JnServiceLogin;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.PathParameter;

/**
 * Entry point of the REST API of the jn cost center (login). Wires the dependencies (local implementations when
 * {@code localEnvironment} is set, GCP and Elasticsearch otherwise), records unhandled errors in
 * {@code jn_jobsnow_error} and registers the servlet filters: e-mail syntax on {@code /login/*}, session values on
 * {@code /contact-us/*} and {@code /login/*}, and session validation on {@code /contact-us/*}.
 */
@EnableWebMvc
@EnableAutoConfiguration(exclude={MongoAutoConfiguration.class})
@ComponentScan(basePackageClasses = {
		JnRestApiLogin.class,
		CcpRestApiExceptionHandlerSpring.class,
})
@SpringBootApplication
public class JnRestApiSpringStarter {
	
	/**
	 * Wires the dependencies and starts Spring.
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		CcpGsonJsonHandler ccpGsonJsonHandler = new CcpGsonJsonHandler();
		CcpDependencyInjection.loadAllDependencies(
				ccpGsonJsonHandler
				);
		
		 
		boolean localEnvironment = CcpRestApiUtils.isLocalEnvironment();	
		CcpTelegramInstantMessenger ccpTelegramInstantMessenger = new CcpTelegramInstantMessenger();
		CcpMindrotPasswordHandler ccpMindrotPasswordHandler = new CcpMindrotPasswordHandler();
		CcpElasticSearchDbRequest ccpElasticSearchDbRequest = new CcpElasticSearchDbRequest();
		CcpGcpMainAuthentication ccpGcpMainAuthentication = new CcpGcpMainAuthentication();
		CcpElasticSerchDbBulk ccpElasticSerchDbBulk = new CcpElasticSerchDbBulk();
		CcpElasticSearchCrud ccpElasticSearchCrud = new CcpElasticSearchCrud();
		CcpApacheMimeHttp ccpApacheMimeHttp = new CcpApacheMimeHttp();
		// the queries (e.g. the purge of a versionable record's history) need it
		CcpElasticSearchQueryExecutor ccpElasticSearchQueryExecutor = new CcpElasticSearchQueryExecutor();

		CcpDependencyInjection.loadAllDependencies
		(
				localEnvironment ? CcpLocalInstances.syncMensageriaListener : new CcpGcpPubSubMensageriaSender(),
				localEnvironment ? CcpLocalInstances.email : new CcpSendGridEmailSender(),
				localEnvironment ? CcpLocalInstances.bucket : new CcpGcpFileBucket(),
				localEnvironment ? CcpLocalCacheInstances.map : new CcpGcpMemCache(),
				// local: writes to c:\logs\telegram instead of sending a real message to the support chat
				localEnvironment ? CcpLocalInstances.instantMessenger : ccpTelegramInstantMessenger,
				ccpMindrotPasswordHandler,
				ccpElasticSearchDbRequest,
				ccpGcpMainAuthentication,
				ccpElasticSerchDbBulk, 
				ccpElasticSearchCrud,
				ccpElasticSearchQueryExecutor,
				ccpApacheMimeHttp
		);

		CcpRestApiExceptionHandlerSpring.genericExceptionHandler = CcpEntityOperationType.save.getOperationCallback(JnEntityJobsnowError.ENTITY);

		SpringApplication.run(JnRestApiSpringStarter.class, args);
	}

	/**
	 * Adds to the OpenAPI document the path variables that an operation uses in its path but does not declare.
	 * @return the customizer
	 */
	@Bean
	public GlobalOpenApiCustomizer missingPathParamsCustomizer() {
		return openApi -> {
			Paths paths = openApi.getPaths();
			boolean pathsMissing = paths == null;
			if (pathsMissing) return;
			Pattern p = Pattern.compile("\\{(\\w+)\\}");
			Paths paths2 = openApi.getPaths();
			paths2.forEach((pathTemplate, pathItem) -> {
				Set<String> templateVars = new HashSet<>();
				Matcher m = p.matcher(pathTemplate);
				while (m.find()) {
					String group = m.group(1);
					templateVars.add(group);
					}
					boolean templateVarsEmpty = templateVars.isEmpty();
					if (templateVarsEmpty) return;
					var readOperations = pathItem.readOperations();
					readOperations.forEach(op -> {
					Set<String> declared = new HashSet<>();
					var parameters = op.getParameters();
					boolean hasParameters = parameters != null;
					if (hasParameters) {
						var parameters2 = op.getParameters();
						var stream = parameters2.stream();
						var filter2 = stream
							.filter(param -> "path".equals(param.getIn()));
							filter2
							.forEach(param -> declared.add(param.getName()));
					}
					Stream<String> stream2 = templateVars.stream();
					var filter3 = stream2
						.filter(v -> !declared.contains(v));
						filter3
						.forEach(v -> op.addParametersItem(
							new PathParameter().name(v).required(true).schema(new StringSchema())
						));
				});
			});
		};
	}

	/**
	 * Describes the API in the OpenAPI document.
	 * @return the description
	 */
	@Bean
	public OpenAPI jnOpenAPI() {
		OpenAPI openAPI = new OpenAPI();
		Info info2 = new Info();
		Info title = info2
						.title("JobsNow Authentication API");
						Info description = title
						.description("REST API for authentication: login, password management, token and session control.");
						Info version = description
						.version("1.0");
						OpenAPI info = openAPI
						.info(version);
						return info;
	}

	/**
	 * Serves the static resources of the Swagger UI.
	 * @return the configurer
	 */
	@Bean
	public WebMvcConfigurer swaggerResourceHandler() {
		var webMvcConfigurer = new WebMvcConfigurer() {
			@Override
			public void addResourceHandlers(ResourceHandlerRegistry registry) {
				ResourceHandlerRegistration addResourceHandler = registry.addResourceHandler("/webjars/**");
				addResourceHandler
						.addResourceLocations("classpath:/META-INF/resources/webjars/");
						ResourceHandlerRegistration addResourceHandler2 = registry.addResourceHandler("/swagger-ui/**");
						addResourceHandler2
						.addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/");
			}
		};
		return webMvcConfigurer;
	}

	/**
	 * Rejects an invalid e-mail in the path of {@code /login/*}.
	 * @return the filter registration
	 */
	@Bean
	public FilterRegistrationBean<CcpValidEmailFilter> emailFilter() {
		FilterRegistrationBean<CcpValidEmailFilter> filterRegistration = new FilterRegistrationBean<>();
		CcpValidEmailFilter emailSyntaxFilter = CcpValidEmailFilter.getEmailSyntaxFilter("login/");
		filterRegistration.setFilter(emailSyntaxFilter);
		filterRegistration.addUrlPatterns("/login/*");
		return filterRegistration;
	}

	/**
	 * Puts the session values (user agent, ip, e-mail) in the body of {@code /contact-us/*} and {@code /login/*}.
	 * @return the filter registration
	 */
	@Bean
	public FilterRegistrationBean<CcpPutSessionValuesAndExecuteTaskFilter> putSessionValuesFilter() {
		FilterRegistrationBean<CcpPutSessionValuesAndExecuteTaskFilter> filterRegistration = new FilterRegistrationBean<>();
		filterRegistration.setFilter(CcpPutSessionValuesAndExecuteTaskFilter.TASKLESS);
		filterRegistration.addUrlPatterns("/contact-us/*", "/login/*");
		return filterRegistration;
	}

	/**
	 * Validates the session ({@code JnServiceLogin.ValidateLogin}) on {@code /contact-us/*}.
	 * @return the filter registration
	 */
	@Bean
	public FilterRegistrationBean<CcpPutSessionValuesAndExecuteTaskFilter> validateSessionFilter() {
		FilterRegistrationBean<CcpPutSessionValuesAndExecuteTaskFilter> filterRegistration = new FilterRegistrationBean<>();
		CcpPutSessionValuesAndExecuteTaskFilter filter = new CcpPutSessionValuesAndExecuteTaskFilter(JnServiceLogin.ValidateLogin);
		filterRegistration.setFilter(filter);
		filterRegistration.addUrlPatterns("/contact-us/*");
		return filterRegistration;
	}
}
