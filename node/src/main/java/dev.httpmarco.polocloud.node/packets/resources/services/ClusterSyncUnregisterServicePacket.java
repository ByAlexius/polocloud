package dev.httpmarco.polocloud.node.packets.resources.services;

import dev.httpmarco.polocloud.api.packet.AbstractStringComponentPacket;

public final class ClusterSyncUnregisterServicePacket extends AbstractStringComponentPacket {

    public ClusterSyncUnregisterServicePacket(String serviceId) {
        super(serviceId);
    }

    public String serviceId() {
        return this.content();
    }
}
