package com.sample.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;

import java.util.Arrays;

@EnableWs
@Configuration
@Slf4j
public class WebServiceConfig {

    @Autowired
    @Value("${soap.endpoints.path:/services}")
    private String webservicePath;

    @Bean
    public ServletRegistrationBean messageDispatcherServlet(ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        servlet.setEnableLoggingRequestDetails(true);
        servlet.setDispatchTraceRequest(true);
        return new ServletRegistrationBean(servlet, webservicePath + "/*");
    }

    public static class CustomWsInitializer implements ApplicationContextInitializer<GenericApplicationContext> {

        public static final String SOAP_ENDPOINTS = "soap.endpoints.";

        @Override
        public void initialize(GenericApplicationContext genericApplicationContext) {
            Environment env = genericApplicationContext.getEnvironment();

            Arrays.stream(env.getProperty("soap.endpoints", String[].class))
                    .forEach(endpointName -> registerEndpointService(genericApplicationContext, env, endpointName, env.getProperty("soap.endpoints.path"))
            );
        }

        private void registerEndpointService(GenericApplicationContext genericApplicationContext, Environment env, String endpointName, String locationUri) {
            Resource resource = genericApplicationContext.getResource(env.getProperty(SOAP_ENDPOINTS + endpointName + ".wsdl.location"));
            SimpleXsdSchema schema = new SimpleXsdSchema(resource);
            genericApplicationContext.registerBean(endpointName + "Schema", SimpleXsdSchema.class, () -> schema);

            String portTypeName = env.getProperty(SOAP_ENDPOINTS + endpointName + ".portType.name");
            String targetNamespace = env.getProperty(SOAP_ENDPOINTS + endpointName + ".target.namespace");

            genericApplicationContext.registerBean(endpointName + "Service",
                    DefaultWsdl11Definition.class,
                    () -> {
                        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
                        wsdl11Definition.setPortTypeName(portTypeName);
                        wsdl11Definition.setLocationUri(locationUri);
                        wsdl11Definition.setTargetNamespace(targetNamespace);
                        wsdl11Definition.setSchema(schema);

                        return wsdl11Definition;
                    });
        }
    }
}
