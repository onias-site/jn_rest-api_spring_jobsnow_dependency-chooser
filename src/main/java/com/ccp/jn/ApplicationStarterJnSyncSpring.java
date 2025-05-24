package com.ccp.jn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.cache.gcp.memcache.CcpGcpMemCache;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.file.bucket.gcp.CcpGcpFileBucket;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.main.authentication.gcp.oauth.CcpGcpMainAuthentication;
import com.ccp.implementations.mensageria.sender.gcp.pubsub.CcpGcpPubSubMensageriaSender;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.jn.controller.ControllerJnLogin;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.ccp.web.servlet.filters.CcpPutSessionValuesAndExecuteTaskFilter;
import com.ccp.web.servlet.filters.CcpValidEmailFilter;
import com.ccp.web.spring.exceptions.handler.CcpSyncExceptionHandler;
import com.jn.business.commons.JnBusinessNotifyError;
import com.jn.business.login.JnBusinessValidateSession;
import com.jn.mensageria.JnMensageriaSender;

@EnableWebMvc
@EnableAutoConfiguration(exclude={MongoAutoConfiguration.class})
@ComponentScan(basePackageClasses = {
		ControllerJnLogin.class, 
		CcpSyncExceptionHandler.class,
})
@SpringBootApplication
public class ApplicationStarterJnSyncSpring {
	
	public static void main(String[] args) {
		
		boolean localEnviroment = new CcpStringDecorator("c:\\rh").file().exists();
		CcpDependencyInjection.loadAllDependencies
		(
				localEnviroment ? CcpLocalInstances.mensageriaSender.getLocalImplementation() : new CcpGcpPubSubMensageriaSender(),
				localEnviroment ? CcpLocalInstances.bucket.getLocalImplementation() : new CcpGcpFileBucket(),
				localEnviroment ? CcpLocalCacheInstances.map.getLocalImplementation() : new CcpGcpMemCache()
				,new CcpMindrotPasswordHandler()
				,new CcpElasticSearchDbRequest()
				,new CcpGcpMainAuthentication()
				,new CcpElasticSearchCrud()
				,new CcpGsonJsonHandler()
				,new CcpApacheMimeHttp() 
		);

		CcpSyncExceptionHandler.genericExceptionHandler = new JnMensageriaSender(JnBusinessNotifyError.INSTANCE);

		SpringApplication.run(ApplicationStarterJnSyncSpring.class, args);
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
		CcpPutSessionValuesAndExecuteTaskFilter filter = new CcpPutSessionValuesAndExecuteTaskFilter(JnBusinessValidateSession.INSTANCE);
		filtro.setFilter(filter);
		filtro.addUrlPatterns("/contact-us/*");
		return filtro;
	}
}
