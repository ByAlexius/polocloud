package dev.httpmarco.polocloud.base.services;

import dev.httpmarco.osgan.networking.ChannelTransmit;
import dev.httpmarco.polocloud.api.groups.CloudGroup;
import dev.httpmarco.polocloud.api.services.ServiceState;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
public final class LocalCloudService extends CloudServiceImpl {

    private final Path runningFolder;

    @Setter
    private Process process;
    @Setter
    private ChannelTransmit channelTransmit;
    private boolean subscribed;

    public LocalCloudService(CloudGroup group, int orderedId, UUID id, int port, ServiceState state) {
        super(group, orderedId, id, port, state);

        this.runningFolder = Path.of("running/" + name() + "-" + id());
        this.subscribed = false;
    }

    @SneakyThrows
    public void subscribeLog() {
        if(this.subscribed) {
            this.subscribed = false;
            return;
        }
        this.subscribed = true;

        log().forEach(System.out::println);
        var in = new BufferedReader(new InputStreamReader(this.process.getInputStream()));
        String line;
        while ((line = in.readLine()) != null) {
            if(!this.subscribed) {
                break;
            }
            System.out.println(line);
        }
    }

    @Override
    @SneakyThrows
    public List<String> log() {
        var logs = new ArrayList<String>();
        var inputStream = this.process.getInputStream();
        var bytes = new byte[2048];
        int length;
        while (inputStream.available() > 0 && (length = inputStream.read(bytes, 0, bytes.length)) != -1) {
            logs.addAll(Arrays.stream(new String(bytes, 0, length, StandardCharsets.UTF_8).split("\n")).toList());
        }
        return logs;
    }
}
