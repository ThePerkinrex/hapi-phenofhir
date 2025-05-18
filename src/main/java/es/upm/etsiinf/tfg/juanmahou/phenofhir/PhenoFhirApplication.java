package es.upm.etsiinf.tfg.juanmahou.phenofhir;

import ca.uhn.fhir.rest.server.RestfulServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import ca.uhn.fhir.context.FhirContext;

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
			PatientResourceProvider patientProvider  // our custom provider, see next
	) {
		RestfulServer server = new RestfulServer(fhirContext);
		// register your resource providers
		server.setResourceProviders(patientProvider);
		ServletRegistrationBean<RestfulServer> servlet =
				new ServletRegistrationBean<>(server, "/fhir/*");
		servlet.setName("FHIRServlet");
		return servlet;
	}

}
