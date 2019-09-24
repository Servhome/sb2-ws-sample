package com.sample.utils;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class MockEndpointDefinition {

    private String serviceName;
    private String namespace;
    private List<MockOperation> operations;

    @Getter
    @Setter
    @Builder
    public static class MockOperation {
        private String localPart;
        private String requestType;
        private String responseType;
    }
}
