package com.iws.engineserver.pojo;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class VisualTool {
    static List<JSONObject> jsonObjectList;
    static List<String> order=new ArrayList<>(Arrays.asList("name","tag","id","created","size"));
    {
        jsonObjectList=new ArrayList<>();
        for(int i=0;i!=10;++i){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name",i);
            jsonObject.put("tag",i);
            jsonObject.put("id",i);
            jsonObject.put("created",i);
            jsonObject.put("size",i);
            jsonObjectList.add(jsonObject);
        }
    }



    static public void showList(List<JSONObject> list,List<String> order){
        showLine(order);
        for(JSONObject jsonObject: list){
            Iterator<String> iterator = order.iterator();
            showLine(new ArrayList<>(){{
                while(iterator.hasNext()){
                    add(jsonObject.getString(iterator.next()));
                }
            }});
        }

    }

    static public void showLine(List<String> line){

        int stride=40,size=line.size();
        String str=String.format("%1$"+size*stride+ "s", "");
        StringBuilder stringBuilder = new StringBuilder(str);
        for(int index=0;index!=size;++index){
            String item=line.get(index);
            stringBuilder.replace(index*stride,index*stride+item.length(),item);
        }
        System.out.println(stringBuilder.toString());
    }

    public static void main(String[] args) {
        showList(jsonObjectList, order);
    }
}
