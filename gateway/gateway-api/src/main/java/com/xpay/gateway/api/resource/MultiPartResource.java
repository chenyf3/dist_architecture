package com.xpay.gateway.api.resource;

import org.springframework.core.io.ByteArrayResource;

public class MultiPartResource extends ByteArrayResource {
    private String filename;

    public MultiPartResource(byte[] byteArray) {
        super(byteArray);
    }

    public MultiPartResource(byte[] byteArray, String filename) {
        super(byteArray);
        this.filename = filename;
    }

    @Override
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
