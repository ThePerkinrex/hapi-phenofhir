package es.upm.etsiinf.tfg.juanmahou.phenofhir.validation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.rest.server.interceptor.RequestValidatingInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseValidatingInterceptor;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import org.hl7.fhir.common.hapi.validation.support.*;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class FhirValidationConfig {

    @Bean
    public NpmPackageValidationSupport igSupport(FhirContext ctx) throws IOException {
        NpmPackageValidationSupport support = new NpmPackageValidationSupport(ctx);
        // load your local (un-published) phenomics-exchange IG
        support.loadPackageFromClasspath("package/phenomics-exchange-0.1.0.r4.tgz");
        return support;
    }

    @Bean
    public ValidationSupportChain validationSupportChain(
            NpmPackageValidationSupport npmPackageSupport, FhirContext ctx) {
        return new ValidationSupportChain(
                npmPackageSupport,
                new DefaultProfileValidationSupport(ctx),
                new CommonCodeSystemsTerminologyService(ctx),
                new InMemoryTerminologyServerValidationSupport(ctx),
                new SnapshotGeneratingValidationSupport(ctx)
        );
    }

    @Bean
    public FhirInstanceValidator instanceValidator(ValidationSupportChain chain) {
        return new FhirInstanceValidator(chain);
    }

    @Bean
    public RequestValidatingInterceptor requestValidator(FhirInstanceValidator validator) {
        RequestValidatingInterceptor interceptor = new RequestValidatingInterceptor();
        interceptor.addValidatorModule(validator);

        // Fail the request on ERROR-level issues:
        interceptor.setFailOnSeverity(ResultSeverityEnum.ERROR);

        // Always include full OperationOutcome in the response body:
        interceptor.setAddValidationResultsToResponseOperationOutcome(true);

        // Also add the OO as a response header if WARNINGS or worse occur:
        interceptor.setAddResponseOutcomeHeaderOnSeverity(ResultSeverityEnum.WARNING);

        return interceptor;
    }

    @Bean
    public ResponseValidatingInterceptor responseValidator(FhirInstanceValidator validator) {
        ResponseValidatingInterceptor interceptor = new ResponseValidatingInterceptor();
        interceptor.addValidatorModule(validator);

        // Fail outgoing responses on any ERROR-level issues
        interceptor.setFailOnSeverity(ResultSeverityEnum.ERROR);

        // If there are WARNINGs or worse, include the full OperationOutcome in the HTTP header:
        interceptor.setAddResponseOutcomeHeaderOnSeverity(ResultSeverityEnum.WARNING);

        // Optionally, you can also add a short status string header for any issues ≥ INFORMATION:
        interceptor.setAddResponseHeaderOnSeverity(ResultSeverityEnum.INFORMATION);

        // (You can customize the header name/value if you like)
        // interceptor.setResponseHeaderName("X-MyApp-FhirValidation");
        // interceptor.setResponseHeaderValue("ValidationFailure");

        return interceptor;
    }
}
