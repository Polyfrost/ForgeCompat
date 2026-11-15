package net.minecraftforge.common;

import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.versioning.ComparableVersion;

import java.util.Collections;
import java.util.Map;

public class ForgeVersion {
    public static final String mcVersion = "1.8.9";

    public static int getBuildVersion() {
        return 2318;
    }

    public static final int majorVersion = 11;
    public static final int minorVersion = 15;
    public static final int revisionVersion = 1;
    public static final int buildVersion = 2318;

    public static String getVersion() {
        return "11.15.1.2318";
    }

    public static int getMajorVersion() {
        return majorVersion;
    }

    public static int getMinorVersion() {
        return minorVersion;
    }

    public static int getRevisionVersion() {
        return revisionVersion;
    }

    public static CheckResult getResult(ModContainer mod) {
        return new CheckResult(Status.UP_TO_DATE, null, Collections.emptyMap(), null);
    }

    public enum Status {
        PENDING, FAILED, UP_TO_DATE, OUTDATED, AHEAD, BETA, BETA_OUTDATED
    }

    public static class CheckResult {
        public final Status status;
        public final ComparableVersion target;
        public final Map<ComparableVersion, String> changes;
        public final String url;

        CheckResult(Status status, ComparableVersion target, Map<ComparableVersion, String> changes, String url) {
            this.status = status;
            this.target = target;
            this.changes = changes;
            this.url = url;
        }
    }
}
