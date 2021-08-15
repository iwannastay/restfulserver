package com.iws.engineserver.pojo;

import com.alibaba.fastjson.JSONObject;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class Algorithm {
    String algorithmNo;
    String codePath;
    String branch;
    String imageName;
    String compileScript;
    String shellParams;
    AlgorithmPackage algorithmPackage;
    int compileStatus;
    @Deprecated
    int algorithmStatus;
    List<String> testList;

    public List<String> getTestList() {
        return testList;
    }

    public void setTestList(List<String> testList) {
        this.testList = testList;
    }

    public void addTest(String id){

    }

    public void removeTest(String id){

    }

    public Algorithm() {
        testList =new ArrayList<String>();
    }

    public Algorithm(String algorithmNo, String codePath,String branch, String imageName, String compileScript,
                     String shellParams, AlgorithmPackage algorithmPackage, int compileStatus,
                     int algorithmStatus) {
        this.algorithmNo = algorithmNo;
        this.codePath = codePath;
        this.branch = branch;
        this.imageName = imageName;
        this.compileScript = compileScript;
        this.shellParams = shellParams;
        this.algorithmPackage = algorithmPackage;
        this.compileStatus = compileStatus;
        this.algorithmStatus=algorithmStatus;

        testList =new ArrayList<String>();
    }

    public Algorithm(Document document){
        this(document.getString("algorithmNo"),
                document.getString("codePath"),
                document.getString("branch"),
                document.getString("imageName"),
                document.getString("compileScript"),
                document.getString("shellParams"),
                new AlgorithmPackage((Document)document.get("algorithmPackage")),
                document.getInteger("compileStatus"),
                document.getInteger("algorithmStatus"));

        testList = (List<String>)document.get("testList");
    }

    public Document toDocument(){
        return Document.parse(JSONObject.toJSONString(this));
    }

    public String getAlgorithmNo() {
        return algorithmNo;
    }

    public void setAlgorithmNo(String algorithmNo) {
        this.algorithmNo = algorithmNo;
    }

    public String getCodePath() {
        return codePath;
    }

    public void setCodePath(String codePath) {
        this.codePath = codePath;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getCompileScript() {
        return compileScript;
    }

    public void setCompileScript(String compileScript) {
        this.compileScript = compileScript;
    }

    public String getShellParams() {
        return shellParams;
    }

    public void setShellParams(String shellParams) {
        this.shellParams = shellParams;
    }

    public AlgorithmPackage getAlgorithmPackage() {
        return algorithmPackage;
    }

    public void setAlgorithmPackage(AlgorithmPackage algorithmPackage) {
        this.algorithmPackage = algorithmPackage;
    }

    public int getCompileStatus() {
        return compileStatus;
    }

    public void setCompileStatus(int compileStatus) {
        this.compileStatus = compileStatus;
    }

    @Deprecated
    public int getAlgorithmStatus() {
        return algorithmStatus;
    }

    @Deprecated
    public void setAlgorithmStatus(int algorithmStatus) {
        this.algorithmStatus = algorithmStatus;
    }

    @Override
    public String toString() {
        return "Algorithm{" +
                "algorithmNo='" + algorithmNo + '\'' +
                ", codePath='" + codePath + '\'' +
                ", branch='" + branch + '\'' +
                ", imageName='" + imageName + '\'' +
                ", compileScript='" + compileScript + '\'' +
                ", shellParams='" + shellParams + '\'' +
                ", algorithmPackage=" + algorithmPackage +
                ", compileStatus=" + compileStatus +
                ", algorithmStatus=" + algorithmStatus +
                ", testList=" + testList +
                '}';
    }

    public static void main(String[] args) {
        Algorithm algorithm = new Algorithm();
        algorithm.setAlgorithmNo("100");
        algorithm.setImageName("10.16.17.92:8433/library/ubuntu16.04-ssh:v1");
        algorithm.setCodePath("https://github.com/iwannastay/helloworld.git");
        algorithm.setBranch("master");
        algorithm.setCompileScript("compileScript.sh");
        algorithm.setShellParams("/bin/bash");
        algorithm.setCompileStatus(1);
        algorithm.setAlgorithmStatus(2);
        algorithm.setAlgorithmPackage(new AlgorithmPackage(
                "200m",
                "800Mi",
                null,
                0
        ));

        List<String> tesList= new ArrayList<>();
        tesList.add("101");
        tesList.add("102");
        algorithm.setTestList(tesList);

        algorithm.getTestList().remove("101");

    }
}
