package com.iws.engineserver.pojo;

import com.alibaba.fastjson.JSONObject;
import com.iws.engineserver.pojo.Interface.StorageManager;
import io.kubernetes.client.openapi.models.V1PodSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class NfsManager implements StorageManager {
    private static Logger logger = LoggerFactory.getLogger(NfsManager.class);

    static String rootPath="/mnt/data";
    static String algPath="/Algorithms";
    static String testPath="/TEST";
    static String userPath="/Users";
    static String deployPath="/Deployments";
    static String imagePath="/Images";

    public static String getAlgPath() {
        return algPath;
    }

    public static String getUserPath() {
        return userPath;
    }

    public static String getDeployPath() {
        return deployPath;
    }

    public String getRootPath() {
        return rootPath;
    }

    public String getImagePath() {
        return imagePath;
    }

    public NfsManager() {
    }


    @Override
    public Map<String, String> getInfo() {
        return null;
    }

    @Override
    public Menu createMenu(String name,Info.TYPE type) {
        Menu menu=null;
        switch (type){
            case ALG:
                menu=createAlgMenu(name);
                break;
            case DEPLOY:
                break;
            case USER:
                menu=createUserMenu(name);
                break;
            case TEST:
                menu=createTestMenu(name);
                break;
            case NULL:
                File file = new File(name);
                if (!file.exists()) {
                    file.mkdirs();
                }
                break;
            default:
                break;
        }
        return menu;
    }

    @Override
    public boolean deleteMenu(String name,Info.TYPE type) {
        boolean success=false;
        switch (type){
            case ALG:
                success=deleteAlgMenu(name);
                break;
            case DEPLOY:
                break;
            case USER:
                success=deleteUserMenu(name);
                break;
            case TEST:
                success=deleteTestMenu(name);
                break;
            case NULL:
                success=doDeleteEmptyDir(name);
                break;
            default:
                break;
        }

        return success;
    }

    @Override
    public List<Menu> getMenus() {
        return null;
    }


    public Menu createUserMenu(String name){
        String dir=rootPath + userPath + "/" + name;
        File file = new File(dir);
        if (!file.exists()) {
            logger.info("create user menu:"+name+" "+file.mkdirs());
            return new Menu(dir,null);
        }
        return null;
    }

    public boolean deleteUserMenu(String name){
        String dir=rootPath + userPath + "/" + name;
        File file = new File(dir);
        if(file.exists()&& 0== file.list().length){
            return true; //file.delete();
        }
         return false;
    }

    public Menu createAlgMenu(String name) {

        String dir=rootPath + algPath + "/" + name;
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }

        ArrayList<Menu> submenu = new ArrayList<>();
        submenu.add(createMenu(dir+"/DEMO",Info.TYPE.NULL));
        submenu.add(createMenu(dir+"/LOG",Info.TYPE.NULL));
        submenu.add(createMenu(dir+"/README",Info.TYPE.NULL));
        submenu.add(createMenu(dir+"/SDK",Info.TYPE.NULL));
        submenu.add(createMenu(dir+"/TEST",Info.TYPE.NULL));
        return new Menu(dir,submenu);
    }

    public boolean deleteAlgMenu(String name){
        String dir=rootPath + algPath + "/" + name;
        File file = new File(dir);
        return deleteDir(file);
    }

    public Menu createTestMenu(String name){
        String[] split = name.split("/");
        String dir=rootPath + algPath + "/" + split[0] +testPath+"/"+split[1];

        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        return new Menu(dir,null);
    }

    public boolean deleteTestMenu(String name){
        String[] split = name.split("/");
        String dir=rootPath + algPath + "/" + split[0] +testPath+"/"+split[1];
        File file = new File(dir);
        return deleteDir(file);
    }

    public static void main(String[] args) {
        NfsManager nfsManager = new NfsManager();

//        nfsManager.createMenu("1",Info.TYPE.ALG);
//        nfsManager.createMenu("1/101",Info.TYPE.TEST);
//        nfsManager.deleteMenu("1/101",Info.TYPE.TEST);
//        nfsManager.deleteMenu("1",Info.TYPE.ALG);

//        logger.info(nfsManager.getAlgMenuTemplate("123",null));

//        logger.info(nfsManager.deleteDir(new File(rootPath + algPath + "/" + "123")));

//        File file = new File(rootPath + algPath + "/" + "123" + "/DEMO/demo.txt");
//        logger.info(file.delete());
    }

    private static boolean doDeleteEmptyDir(String dir) {
        return (new File(dir)).delete();
    }


    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i!=children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }

    public Map<String,String> getAlgMenuTemplate(String algorithmNo,String downloadAddress){
        if(null==downloadAddress){
            downloadAddress="/download";
        }

        String prefix=downloadAddress + algPath + "/" + algorithmNo;
        String index=rootPath+algPath+"/"+algorithmNo;
        HashMap<String, String> map = new HashMap<>();
        map.put("readmePath",prefix+"/README/"+getFirstContent(index+"/README"));
        map.put("sdkPath",prefix+"/SDK/"+getFirstContent(index+"/SDK"));
        map.put("demoPath",prefix+"/DEMO/"+getFirstContent(index+"/DEMO"));
        map.put("logPath",prefix+"/LOG/"+getFirstContent(index+"/LOG"));

        return map;
    }

    public String getFirstContent(String path){
        File dir = new File(path);
        if(dir.isDirectory()){
            String[] list = dir.list();
            if(null!=list)
            {int i=0;
            while(i!=list.length&&new File(path+"/"+list[0]).isFile()) {
                return list[i++];
            }}
        }
        return "";
    }


}
