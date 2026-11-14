package cc.polyfrost.oneconfig.utils.hypixel;

public class LocrawInfo {
    private final String serverId;
    private final String gameMode;
    private final String mapName;
    private String rawGameType = "";

    public LocrawInfo(String serverId, String gameMode, String mapName, String rawGameType) {
        this(serverId, gameMode, mapName);
        this.rawGameType = rawGameType;
    }

    public String getRawGameType() {
        return rawGameType;
    }

    public LocrawInfo(String serverId, String gameMode, String mapName) {
        this.serverId = serverId;
        this.gameMode = gameMode;
        this.mapName = mapName;
    }

    public String getServerId() {
        return serverId;
    }

    public String getGameMode() {
        return gameMode;
    }

    public String getMapName() {
        return mapName;
    }
}
