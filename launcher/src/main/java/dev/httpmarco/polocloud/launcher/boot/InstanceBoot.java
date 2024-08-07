package dev.httpmarco.polocloud.launcher.boot;

import dev.httpmarco.polocloud.launcher.util.FileSystemUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public final class InstanceBoot extends AbstractBoot {

    @Override
    public void dependencyLoading() {

    }

    @Override
    public @NotNull File bootFile() {
        var path = Path.of("local/dependencies/polocloud-instance.jar");

        if (!Files.exists(path)) {
            // create path if not exists
            path.toFile().getParentFile().mkdirs();

            FileSystemUtils.copyClassPathFile(this.getClass().getClassLoader(), "polocloud-instance.jar", path.toString());
        }
        return path.toFile();
    }
}