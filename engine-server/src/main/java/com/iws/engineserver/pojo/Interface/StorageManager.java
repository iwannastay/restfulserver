package com.iws.engineserver.pojo.Interface;

import io.kubernetes.client.openapi.models.V1PodSpec;

import java.util.List;
import java.util.Map;

public interface StorageManager {
    class Menu{
        Info info;
        String name;
        List<Menu> subMenu;
        Menu superMenu;

        public Menu getSuperMenu() {
            return superMenu;
        }

        public void setSuperMenu(Menu superMenu) {
            this.superMenu = superMenu;
        }

        public Menu() {
        }

        public Menu(String name, List<Menu> subMenu) {
            this.name = name;
            this.subMenu = subMenu;
        }

        public Info getInfo() {
            return info;
        }

        public void setInfo(Info info) {
            this.info = info;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Menu> getSubMenu() {
            return subMenu;
        }

        public void setSubMenu(List<Menu> subMenu) {
            this.subMenu = subMenu;
        }

        @Override
        public String toString() {
            return "Menu{" +
                    "name='" + name + '\'' +
                    ", subMenu=" + subMenu.toString() +
                    '}';
        }
    }
    class Info{
        public TYPE type;
        public enum TYPE{ALG,USER,DEPLOY,TEST,NULL}
        public String path;
        public String host;
        public Integer maximum;
        public String status;


    }

    Map<String,String> getInfo();
    Menu createMenu(String name,Info.TYPE type);
    boolean deleteMenu(String name,Info.TYPE type);
    List<Menu> getMenus();




}
