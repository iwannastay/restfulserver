package com.iws.engineserver.pojo;


import org.bson.Document;

public class AlgorithmPackage {
    String cpuNumbers;
    String memory;
    String gpuType;
    int gpuNumbers;

    public AlgorithmPackage() {
    }

    public AlgorithmPackage(Document document) {
        this(document.getString("cpuNumbers"),
                document.getString("memory"),
                document.getString("gpuType"),
                document.getInteger("gpuNumbers"));
    }

    public AlgorithmPackage(String cpuNumbers, String memory, String gpuType, int gpuNumbers) {
        this.cpuNumbers = cpuNumbers;
        this.memory = memory;
        this.gpuType = gpuType;
        this.gpuNumbers = gpuNumbers;
    }

    public String getCpuNumbers() {
        return cpuNumbers;
    }

    public void setCpuNumbers(String cpuNumbers) {
        this.cpuNumbers = cpuNumbers;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public String getGpuType() {
        return gpuType;
    }

    public void setGpuType(String gpuType) {
        this.gpuType = gpuType;
    }

    public int getGpuNumbers() {
        return gpuNumbers;
    }

    public void setGpuNumbers(int gpuNumbers) {
        this.gpuNumbers = gpuNumbers;
    }

    @Override
    public String toString() {
        return "algorithmPackage{" +
                "cpuNumbers='" + cpuNumbers + '\'' +
                ", memory='" + memory + '\'' +
                ", gpuType='" + gpuType + '\'' +
                ", gpuNumbers=" + gpuNumbers +
                '}';
    }
}
