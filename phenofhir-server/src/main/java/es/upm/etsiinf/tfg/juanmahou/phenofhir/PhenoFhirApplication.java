	package es.upm.etsiinf.tfg.juanmahou.phenofhir;

	import ca.uhn.fhir.rest.server.RestfulServer;
	import ca.uhn.fhir.rest.server.interceptor.RequestValidatingInterceptor;
	import ca.uhn.fhir.rest.server.interceptor.ResponseValidatingInterceptor;
	import es.upm.etsiinf.tfg.juanmahou.mapper.MapperRegistry;
	import es.upm.etsiinf.tfg.juanmahou.phenofhir.resources.GeneralPhenomicResources;
	import es.upm.etsiinf.tfg.juanmahou.phenofhir.transaction.TransactionProvider;
	import org.slf4j.Logger;
	import org.slf4j.LoggerFactory;
	import org.springframework.boot.SpringApplication;
	import org.springframework.boot.autoconfigure.SpringBootApplication;
	import org.springframework.boot.autoconfigure.domain.EntityScan;
	import org.springframework.boot.web.servlet.ServletRegistrationBean;
    import org.springframework.context.annotation.Bean;
	import ca.uhn.fhir.context.FhirContext;
	import org.springframework.transaction.annotation.EnableTransactionManagement;

    @SpringBootApplication(scanBasePackages = "es.upm.etsiinf.tfg.juanmahou")
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
			return FhirContext.forR4();
		}

		@Bean
		public ServletRegistrationBean<RestfulServer> fhirServletRegistration(
				FhirContext fhirContext,
				RequestValidatingInterceptor requestValidatingInterceptor,
				ResponseValidatingInterceptor responseValidatingInterceptor,
				GeneralPhenomicResources providers,
				TransactionProvider transactionProvider,
				MapperRegistry mapperRegistry
		) {
			RestfulServer server = new RestfulServer(fhirContext);
			// register your resource providers
			server.registerProviders(providers.getResources());
			server.registerProvider(transactionProvider);
			server.registerInterceptor(requestValidatingInterceptor);
			server.registerInterceptor(responseValidatingInterceptor);
			server.setServerName("PhenoFHIR");
			server.setServerVersion("0.1.0");
			ServletRegistrationBean<RestfulServer> servlet =
					new ServletRegistrationBean<>(server, "/fhir/*");
			servlet.setName("FHIRServlet");

			return servlet;


		}

	}
