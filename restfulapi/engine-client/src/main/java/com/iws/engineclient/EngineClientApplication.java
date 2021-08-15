package com.iws.engineclient;

import com.iws.engineclient.pojo.ConsoleListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

@SpringBootApplication
public class EngineClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(EngineClientApplication.class, args);


        ConsoleListener consoleListener= new ConsoleListener(new Scanner(System.in), new ConsoleListener.Action() {

            public void act(String msg) {
                System.out.println("Command not found: " + msg);
            }
        });

        consoleListener.addAction("connect",new ConsoleListener.Action() {

            public void act(String msg) {
                //TODO
                RestTemplate restTemplate=new RestTemplate();
                String url="http://localhost:8001/testofserver";
                ResponseEntity<String> forEntity = restTemplate.getForEntity(url, String.class);
                System.out.println("Response: " + forEntity.getBody());
            }
        });


        consoleListener.addAction("test",new ConsoleListener.Action() {

            public void act(String msg) {
                //TODO
                try {
                    Process p = Runtime.getRuntime().exec("ssh -help");
                    InputStream is = p.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    p.waitFor();
                    if (p.exitValue() != 0) {
                        //说明命令执行失败
                        //可以进入到错误处理步骤中
                        System.out.println("WROOOOOONG!");
                    }
                    String s = null;
                    while ((s = reader.readLine()) != null) {
                        System.out.println(s);
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        consoleListener.listenInNewThread();
    }

}
