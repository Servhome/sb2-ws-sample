
public javax.xml.transform.dom.DOMSource $localPart(javax.xml.transform.dom.DOMSource request) throws Exception {
    org.slf4j.LoggerFactory.getLogger("custom.EndpointMapping").debug("Entered endpoint $localPart : " + request.toString());
    return com.sample.controller.GlobalSoapEndpoint.handle(request, "$namespaceUri", "$localPart", "$requestType", "$responseType");
}
