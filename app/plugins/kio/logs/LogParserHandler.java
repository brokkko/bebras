package plugins.kio.logs;

public interface LogParserHandler {
    void handleSubLog(String logId, MachineInfo info);

    void handleCommand(long time, String cmd, Object[] extraArgs);
}
