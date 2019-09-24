package com.sample.controller;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.xml.transform.dom.DOMSource;

import static lombok.AccessLevel.PRIVATE;

@Slf4j
@NoArgsConstructor(access = PRIVATE)
public abstract class GlobalSoapEndpoint {

    public static DOMSource handle(DOMSource request, String namespaceUri, String localPart, String requestType, String responseType) throws Exception {
        log.debug("GlobalSoapEndpoint called : {} - {{}}:{} {} {}", request, namespaceUri, localPart, requestType, responseType);
        return null;
    }

}
