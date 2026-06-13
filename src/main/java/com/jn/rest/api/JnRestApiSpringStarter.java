package com.jn.rest.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.cache.gcp.memcache.CcpGcpMemCache;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
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
import com.jn.business.login.JnBusinessSessionValidate;
import com.jn.business.messages.JnBusinessNotifyError;
import com.jn.mensageria.JnFunctionMensageriaSender;
import com.jn.rest.api.endpoints.JnRestApiLogin;

/**
 * Ponto de entrada da API REST do módulo JN (jobsnow principal). Inicializa o DI com as implementações
 * corretas (produção via GCP/Elasticsearch ou locais conforme {@code localEnvironment}), configura o handler
 * de exceções globais e registra os filtros de servlet para validação de e-mail, injeção de sessão e
 * validação de sessão.
 */
@EnableWebMvc
@EnableAutoConfiguration(exclude={MongoAutoConfiguration.class})
@ComponentScan(basePackageClasses = {
		JnRestApiLogin.class,
		CcpRestApiExceptionHandlerSpring.class,
})
@SpringBootApplication
public class JnRestApiSpringStarter {
	
	public static void main(String[] args) {
		CcpDependencyInjection.loadAllDependencies(
				new CcpGsonJsonHandler()
				);
		
		
		boolean localEnvironment = CcpRestApiUtils.isLocalEnvironment();	

		CcpDependencyInjection.loadAllDependencies
		(
				localEnvironment ? CcpLocalInstances.mensageriaSender : new CcpGcpPubSubMensageriaSender(),
				localEnvironment ? CcpLocalInstances.email : new CcpSendGridEmailSender(),
				localEnvironment ? CcpLocalInstances.bucket : new CcpGcpFileBucket(),
				localEnvironment ? CcpLocalCacheInstances.map : new CcpGcpMemCache(),
				new CcpTelegramInstantMessenger(),
				new CcpMindrotPasswordHandler(),
				new CcpElasticSearchDbRequest(),
				new CcpGcpMainAuthentication(),
				new CcpElasticSerchDbBulk(), 
				new CcpElasticSearchCrud(),
				new CcpApacheMimeHttp() 
		);

		CcpRestApiExceptionHandlerSpring.genericExceptionHandler = new JnFunctionMensageriaSender(JnBusinessNotifyError.INSTANCE);

		SpringApplication.run(JnRestApiSpringStarter.class, args);
	}

	@Bean
	public OpenAPI jnOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("JobsNow Authentication API")
						.description("REST API for authentication: login, password management, token and session control.")
						.version("1.0"));
	}

	@Bean
	public WebMvcConfigurer swaggerResourceHandler() {
		return new WebMvcConfigurer() {
			@Override
			public void addResourceHandlers(ResourceHandlerRegistry registry) {
				registry.addResourceHandler("/webjars/**")
						.addResourceLocations("classpath:/META-INF/resources/webjars/");
				registry.addResourceHandler("/swagger-ui/**")
						.addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/");
			}
		};
	}

	@Bean
	public FilterRegistrationBean<CcpValidEmailFilter> emailFilter() {
		FilterRegistrationBean<CcpValidEmailFilter> filtro = new FilterRegistrationBean<>();
		CcpValidEmailFilter emailSyntaxFilter = CcpValidEmailFilter.getEmailSyntaxFilter("login/");
		filtro.setFilter(emailSyntaxFilter);
		filtro.addUrlPatterns("/login/*");
		return filtro;
	}

	@Bean
	public FilterRegistrationBean<CcpPutSessionValuesAndExecuteTaskFilter> putSessionValuesFilter() {
		FilterRegistrationBean<CcpPutSessionValuesAndExecuteTaskFilter> filtro = new FilterRegistrationBean<>();
		filtro.setFilter(CcpPutSessionValuesAndExecuteTaskFilter.TASKLESS);
		filtro.addUrlPatterns("/contact-us/*", "/login/*");
		return filtro;
	}

	@Bean
	public FilterRegistrationBean<CcpPutSessionValuesAndExecuteTaskFilter> validateSessionFilter() {
		FilterRegistrationBean<CcpPutSessionValuesAndExecuteTaskFilter> filtro = new FilterRegistrationBean<>();
		CcpPutSessionValuesAndExecuteTaskFilter filter = new CcpPutSessionValuesAndExecuteTaskFilter(JnBusinessSessionValidate.INSTANCE);
		filtro.setFilter(filter);
		filtro.addUrlPatterns("/contact-us/*");
		return filtro;
	}
}
