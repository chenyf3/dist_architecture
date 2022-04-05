package com.xpay.dto

/**
 * 使用SSH方式部署项目时使用到的参数
 */
class SSHDto {
    /**
     * 源文件(多个文件用英文逗号分割: ,)
     */
    private String sourceFiles
    /**
     * 远程目录前缀
     */
    private String removePrefix
    /**
     * 远程目录
     */
    private String remoteDirectory
    /**
     * 部署命令(多个命令可用英文分号分割: ;)
     */
    private String deployCommand

    String getSourceFiles() {
        return sourceFiles
    }

    void setSourceFiles(String sourceFiles) {
        this.sourceFiles = sourceFiles
    }

    String getRemovePrefix() {
        return removePrefix
    }

    void setRemovePrefix(String removePrefix) {
        this.removePrefix = removePrefix
    }

    String getRemoteDirectory() {
        return remoteDirectory
    }

    void setRemoteDirectory(String remoteDirectory) {
        this.remoteDirectory = remoteDirectory
    }

    String getDeployCommand() {
        return deployCommand
    }

    void setDeployCommand(String deployCommand) {
        this.deployCommand = deployCommand
    }
}
