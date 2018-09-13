package com.jieyee.docker.webconsole.bean;

import java.util.Map;

public class CreateContainerBean {
    private String imageName = null;
    private String serverSelect = null;
    private String containerName = null;
    private String command = null;
    private String[] entrypoint = null;
    private Map<String, String> portMappings;
    /**
     * @return the imageName
     */
    public String getImageName() {
        return imageName;
    }
    /**
     * @param imageName the imageName to set
     */
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
    /**
     * @return the serverSelect
     */
    public String getServerSelect() {
        return serverSelect;
    }
    /**
     * @param serverSelect the serverSelect to set
     */
    public void setServerSelect(String serverSelect) {
        this.serverSelect = serverSelect;
    }
    /**
     * @return the containerName
     */
    public String getContainerName() {
        return containerName;
    }
    /**
     * @param containerName the containerName to set
     */
    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }
    /**
     * @return the command
     */
    public String getCommand() {
        return command;
    }
    /**
     * @param command the command to set
     */
    public void setCommand(String command) {
        this.command = command;
    }
    /**
     * @return the entrypoint
     */
    public String[] getEntrypoint() {
        return entrypoint;
    }
    /**
     * @param entrypoint the entrypoint to set
     */
    public void setEntrypoint(String[] entrypoint) {
        this.entrypoint = entrypoint;
    }
    /**
     * @return the portMappings
     */
    public Map<String, String> getPortMappings() {
        return portMappings;
    }
    /**
     * @param portMappings the portMappings to set
     */
    public void setPortMappings(Map<String, String> portMappings) {
        this.portMappings = portMappings;
    }
}
