package net.minecraftforge.fml.common;

import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import net.minecraftforge.fml.common.versioning.VersionParser;
import net.minecraftforge.fml.common.versioning.VersionRange;
import org.polyfrost.forgecompat.ForgeCompat;

import java.io.File;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ModContainer {
    Map<String, String> EMPTY_PROPERTIES = Collections.emptyMap();

    enum Disableable {
        YES, RESTART, NEVER, DEPENDENCIES
    }

    String getModId();

    String getName();

    String getVersion();

    File getSource();

    default Object getMod() {
        return ForgeCompat.INSTANCES.get(getModId());
    }

    default ModMetadata getMetadata() {
        return new ModMetadata(getModId(), getName(), getVersion());
    }

    default String getDisplayVersion() {
        return getVersion();
    }

    default void bindMetadata(MetadataCollection collection) {
    }

    default void setEnabledState(boolean enabled) {
    }

    default Set<ArtifactVersion> getRequirements() {
        return Collections.emptySet();
    }

    default List<ArtifactVersion> getDependencies() {
        return Collections.emptyList();
    }

    default List<ArtifactVersion> getDependants() {
        return Collections.emptyList();
    }

    default String getSortingRules() {
        return "";
    }

    default boolean registerBus(EventBus bus, LoadController controller) {
        return false;
    }

    default boolean matches(Object mod) {
        return mod == getMod();
    }

    default ArtifactVersion getProcessedVersion() {
        return new DefaultArtifactVersion(getModId(), getVersion());
    }

    default boolean isImmutable() {
        return false;
    }

    default String getGuiClassName() {
        return null;
    }

    default List<String> getOwnedPackages() {
        return Collections.emptyList();
    }

    default boolean shouldLoadInEnvironment() {
        return true;
    }

    default Class<?> getCustomResourcePackClass() {
        return null;
    }

    default Map<String, String> getCustomModProperties() {
        return EMPTY_PROPERTIES;
    }

    default Map<String, String> getSharedModDescriptor() {
        return null;
    }

    default Disableable canBeDisabled() {
        return Disableable.NEVER;
    }

    default VersionRange acceptableMinecraftVersionRange() {
        return VersionParser.parseRange("[1.8.9]");
    }

    default Certificate getSigningCertificate() {
        return null;
    }

    default URL getUpdateUrl() {
        return null;
    }
}
