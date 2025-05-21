	package es.upm.etsiinf.tfg.juanmahou.phenofhir;

	import ca.uhn.fhir.rest.server.IResourceProvider;
	import ca.uhn.fhir.rest.server.RestfulServer;
	import ca.uhn.fhir.rest.server.interceptor.RequestValidatingInterceptor;
	import ca.uhn.fhir.rest.server.interceptor.ResponseValidatingInterceptor;
	import entities.org.phenopackets.schema.v2.core.OntologyClass;
	import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.Mapper;
	import es.upm.etsiinf.tfg.juanmahou.phenofhir.registry.MapperRegistry;
	import es.upm.etsiinf.tfg.juanmahou.phenofhir.registry.NotFoundException;
	import es.upm.etsiinf.tfg.juanmahou.phenofhir.resources.GeneralPhenomicResource;
	import jakarta.annotation.PostConstruct;
	import org.hl7.fhir.r4b.model.Coding;
	import org.slf4j.Logger;
	import org.slf4j.LoggerFactory;
	import org.springframework.boot.SpringApplication;
	import org.springframework.boot.autoconfigure.SpringBootApplication;
	import org.springframework.boot.autoconfigure.domain.EntityScan;
	import org.springframework.boot.web.servlet.ServletRegistrationBean;
	import org.springframework.context.ApplicationContext;
	import org.springframework.context.annotation.Bean;
	import ca.uhn.fhir.context.FhirContext;
	import org.springframework.transaction.annotation.EnableTransactionManagement;

	import java.util.List;
	import java.util.Objects;

	@SpringBootApplication
	@EntityScan({
			"es.upm.etsiinf.tfg.juanmahou.phenofhir.persistence",
			"entities.org.ga4gh.vrsatile.v1",
			"entities.org.phenopackets.schema.v2"
	})
	@EnableTransactionManagement
	public class PhenoFhirApplication {

		private static final Logger log = LoggerFactory.getLogger(PhenoFhirApplication.class);

		public static void main(String[] args) {
			SpringApplication.run(PhenoFhirApplication.class, args);
		}

		@Bean
		public FhirContext fhirContext() {
			return FhirContext.forR4B();
		}

		@Bean
		public ServletRegistrationBean<RestfulServer> fhirServletRegistration(
				FhirContext fhirContext,
				RequestValidatingInterceptor requestValidatingInterceptor,
				ResponseValidatingInterceptor responseValidatingInterceptor,
				List<GeneralPhenomicResource> providers
		) {
			RestfulServer server = new RestfulServer(fhirContext);
			// register your resource providers
			server.setResourceProviders(providers.toArray(IResourceProvider[]::new));
			server.registerInterceptor(requestValidatingInterceptor);
			server.registerInterceptor(responseValidatingInterceptor);
			ServletRegistrationBean<RestfulServer> servlet =
					new ServletRegistrationBean<>(server, "/fhir/*");
			servlet.setName("FHIRServlet");
			return servlet;
		}

	}
