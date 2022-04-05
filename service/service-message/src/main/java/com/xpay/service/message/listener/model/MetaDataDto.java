package com.xpay.service.message.listener.model;

import com.xpay.service.message.config.properties.AMQMonitorProperties;

import java.util.Set;

public class MetaDataDto {
    private Set<String> destinations;
    private AMQMonitorProperties.Node node;

    public Set<String> getDestinations() {
        return destinations;
    }

    public void setDestinations(Set<String> destinations) {
        this.destinations = destinations;
    }

    public AMQMonitorProperties.Node getNode() {
        return node;
    }

    public void setNode(AMQMonitorProperties.Node node) {
        this.node = node;
    }
}
