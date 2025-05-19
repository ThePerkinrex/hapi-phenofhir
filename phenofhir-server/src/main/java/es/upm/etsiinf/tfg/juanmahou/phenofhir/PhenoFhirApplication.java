package es.upm.etsiinf.tfg.juanmahou.phenofhir;

import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.RequestValidatingInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseValidatingInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import ca.uhn.fhir.context.FhirContext;

import java.util.List;

@SpringBootApplication
public class PhenoFhirApplication {

	public static void main(String[] args) {
		SpringApplication.run(PhenoFhirApplication.class, args);
	}

	@Bean
	public FhirContext fhirContext() {
		return FhirContext.forR4();
	}

	@Bean
	public ServletRegistrationBean<RestfulServer> fhirServletRegistration(
			FhirContext fhirContext,
			RequestValidatingInterceptor requestValidatingInterceptor,
			ResponseValidatingInterceptor responseValidatingInterceptor,
			List<IResourceProvider> providers  // our custom provider, see next
	) {
		RestfulServer server = new RestfulServer(fhirContext);
		// register your resource providers
		server.setResourceProviders(providers);
		server.registerInterceptor(requestValidatingInterceptor);
		server.registerInterceptor(responseValidatingInterceptor);
		ServletRegistrationBean<RestfulServer> servlet =
				new ServletRegistrationBean<>(server, "/fhir/*");
		servlet.setName("FHIRServlet");
		return servlet;
	}

}
