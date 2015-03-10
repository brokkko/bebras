package plugins.kio.logs;

public class MachineInfo {

    private String os;
    private String manufacturer;
    private String cpu;
    private String version;
    private String language;
    private String playerType;
    private String dpi;
    private String screenWidth;
    private String screenHeight;

    public MachineInfo(String os, String manufacturer, String cpu, String version, String language, String playerType, String dpi, String screenWidth, String screenHeight) {
        this.os = os;
        this.manufacturer = manufacturer;
        this.cpu = cpu;
        this.version = version;
        this.language = language;
        this.playerType = playerType;
        this.dpi = dpi;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public String getOs() {
        return os;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getCpu() {
        return cpu;
    }

    public String getVersion() {
        return version;
    }

    public String getLanguage() {
        return language;
    }

    public String getPlayerType() {
        return playerType;
    }

    public String getDpi() {
        return dpi;
    }

    public String getScreenWidth() {
        return screenWidth;
    }

    public String getScreenHeight() {
        return screenHeight;
    }
}
