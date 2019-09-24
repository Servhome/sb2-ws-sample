package com.sample.utils;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.ParameterAnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;
import lombok.NoArgsConstructor;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.io.StringWriter;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public abstract class MockedEndpointGenerator {

    public static Class<?> generateMockEndpoint(MockEndpointDefinition def) {

        ClassPool pool = ClassPool.getDefault();

        CtClass cc = pool.makeClass(def.getServiceName() + "Endpoint");
        ClassFile classFile = cc.getClassFile();
        ConstPool constpool = classFile.getConstPool();

        classFile.addAttribute(addSingleAnnotation(constpool, Endpoint.class.getName()));

        for (MockEndpointDefinition.MockOperation operation : def.getOperations()) {
            try {
                CtMethod mthd = CtNewMethod.make(templateMethod(operation, def.getNamespace()), cc);
                ConstPool mthdConstPool = mthd.getMethodInfo().getConstPool();

                // add method annotations
                AnnotationsAttribute annotationsAttribute = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
                Annotation[] annotations = new Annotation[]{
                        addAnnotation(mthdConstPool, ResponsePayload.class.getName()),
                        addAnnotation(mthdConstPool, PayloadRoot.class.getName(),
                                new String[][]{
                                        new String[]{"namespace", def.getNamespace()},
                                        new String[]{"localPart", operation.getLocalPart()}
                                }
                        )
                };
                annotationsAttribute.setAnnotations(annotations);
                mthd.getMethodInfo().addAttribute(annotationsAttribute);

                // add method's parameter annotation
                ParameterAnnotationsAttribute parameterAttributeInfo = new ParameterAnnotationsAttribute(mthdConstPool, ParameterAnnotationsAttribute.visibleTag);
                ConstPool parameterConstPool = parameterAttributeInfo.getConstPool();

                Annotation annotation = addAnnotation(parameterConstPool, RequestPayload.class.getName());
                Annotation[][] annotations2 = new Annotation[][]{
                    new Annotation[] {annotation}
                };
                parameterAttributeInfo.setAnnotations(annotations2);
                mthd.getMethodInfo().addAttribute(parameterAttributeInfo);

                cc.addMethod(mthd);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        try {
            return cc.toClass();
        } catch (Exception e) {
            throw new IllegalStateException("Custom Endpoint class creation failed", e);
        }
    }

    private static Annotation addAnnotation(ConstPool constpool, String annotationClassName, String[]... e) {
        Annotation annotation = new Annotation(annotationClassName, constpool);

        for (String[] fieldValue : e) {
            annotation.addMemberValue(fieldValue[0], new StringMemberValue(fieldValue[1], constpool));
        }
        return annotation;
    }

    private static AnnotationsAttribute addSingleAnnotation(ConstPool constpool, String annotationClassName, String[]... e) {
        AnnotationsAttribute annotationsAttribute = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
        Annotation annotation = new Annotation(annotationClassName, constpool);

        for (String[] fieldValue : e) {
            annotation.addMemberValue(fieldValue[0], new StringMemberValue(fieldValue[1], constpool));
        }
        annotationsAttribute.setAnnotation(annotation);
        return annotationsAttribute;
    }

    private static String templateMethod(MockEndpointDefinition.MockOperation def, String namespace) {

        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty("file.resource.loader.class", ClasspathResourceLoader.class.getName());
        velocityEngine.init();

        Template template = velocityEngine.getTemplate("javassist/EndpointMethod.java.tpl");
        VelocityContext context = new VelocityContext();
        context.put("namespaceUri", namespace);
        context.put("localPart", def.getLocalPart());
        context.put("requestType", def.getRequestType());
        context.put("responseType", def.getResponseType());

        StringWriter sw = new StringWriter();
        template.merge(context, sw);

        return sw.toString();
    }
}
