package com.iws.engineserver.pojo;


import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.GitlabUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Qualifier;
import java.io.IOException;
import java.util.List;

public class GitManager {
    private static Logger logger = LoggerFactory.getLogger(GitManager.class);
    private static final String gitAdress="http://10.16.97.52:7443";
    private static final String accessToken="D2bY_v5-r3RjZU2GgWGC";
    private GitlabAPI gitlabAPI=null;

    public GitManager(){
        setDefaultGitlabAPI();
    }

    public GitlabAPI getGitlabAPI() {
        return gitlabAPI;
    }

    public void setDefaultGitlabAPI() {
        gitlabAPI = GitlabAPI.connect(gitAdress, accessToken);;
    }

    public static String getGitAdress() {
        return gitAdress;
    }

    public GitlabUser createUser(User request) throws IOException {
        //assert (request.getPassword().length()>=8);
        GitlabUser user = gitlabAPI.createUser(request.getEmail(), request.getPassword(), request.getUserName(), request.getUserName(),
                    null, null, null, null,
                    5, null, null, null,
                    false, false, true, true);
        if(user!=null){
            logger.info("User " + user.getName() + " created.");
        }
        return user;
    }


    public boolean deleteUser(String userName) throws IOException {
        List<GitlabUser> users = gitlabAPI.findUsers(userName);
        if (!users.isEmpty()){
            Integer id = users.get(0).getId();
            gitlabAPI.deleteUser(id);
            logger.info("User "+userName+" deleted.");
            return true;
        }else {
            logger.info("User "+userName+" not found.");
            return false;
        }
    }


    public static void main(String[] args) throws IOException {

        GitManager gitManager = new GitManager();
        List<GitlabUser> qqq = gitManager.getGitlabAPI().findUsers("qqq");
        if(!qqq.isEmpty()){
            GitlabUser gitlabUser = qqq.get(0);
            logger.info(gitlabUser.getName()+gitlabUser.getUsername());
        }

        User user = new User("test","12345678",101010,"123@qq.com","token1", 1);
        logger.info(gitManager.createUser(user).getUsername());
        logger.info(String.valueOf(gitManager.deleteUser(user.getUserName())));
    }
}
