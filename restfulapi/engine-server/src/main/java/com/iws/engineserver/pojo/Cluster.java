package com.iws.engineserver.pojo;


import com.alibaba.fastjson.JSONObject;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Yaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Cluster {
    private static Logger logger = LoggerFactory.getLogger(Cluster.class);

    String kubeConfigPath;
    ApiClient client;

    public Cluster() throws IOException, ApiException {
        this("/root/.kube/config");
    }

    public Cluster(String configPath) throws IOException, ApiException {
        kubeConfigPath = configPath;
        client = Config.fromConfig(kubeConfigPath);
        Configuration.setDefaultApiClient(client);

    }



    public List<String> getPods() throws ApiException {
        CoreV1Api api = new CoreV1Api(client);
        V1PodList list =
                api.listPodForAllNamespaces(null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);


        List<V1Pod> items = list.getItems();
        List<String> podList = new LinkedList<>();
        for (V1Pod item : items) {
            podList.add(Objects.requireNonNull(item.getMetadata()).getName());
        }
        return podList;
    }

    public List<V1Pod> getPods(String namespace, String labelSelector) {
        CoreV1Api api = new CoreV1Api(client);

        V1PodList list = null;
        List<V1Pod> podList=null;
        try {
            list = api.listNamespacedPod(namespace,null,
                    null,
                    null,
                    null,
                    labelSelector,
                    null,
                    null,
                    null,
                    null,
                    null);

            podList=list.getItems();
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return podList;
    }

    //TODO: might need a labelselector
    public List<V1Deployment> getDeployments(String namespace) {
        AppsV1Api api = new AppsV1Api(client);


        List<V1Deployment> dpList=null;
        try {
            dpList = api.listNamespacedDeployment(namespace,null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null)
                    .getItems();

        } catch (ApiException e) {
            e.printStackTrace();
        }
        return dpList;
    }

    public List<V1Job> getJobs(String namespace) {
        BatchV1Api api = new BatchV1Api(client);


        List<V1Job> jobList=null;
        try {
            jobList = api.listNamespacedJob(namespace,null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null)
                    .getItems();
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return jobList;
    }

    public void createPodByYaml(String args) throws IOException, ApiException {
        CoreV1Api api = new CoreV1Api(client);
        //Yaml.addModelMap("v1","Pod",V1Pod.class);
        File file = ResourceUtils.getFile("/home/iws/Public/k8stest/test.yaml");
        V1Pod v1Pod = (V1Pod) Yaml.load(file);
        logger.info(Yaml.dump(v1Pod));
        V1Pod v1Pod1 = api.createNamespacedPod("default", v1Pod, "false", null, null);

    }

    public V1Pod getPod(String name, String namespace){
        List<V1Pod> podList = getPods(namespace,null);
        if (podList.isEmpty()) return null;
        for(V1Pod pod:podList) {
            if (name.equals(pod.getMetadata().getName())) {
                CoreV1Api coreV1Api = new CoreV1Api();
                try {
                    V1Pod result = coreV1Api.readNamespacedPod(name, namespace, null, null, null);
                    return result;
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public V1Deployment getDeployment(String name, String namespace){
        List<V1Deployment> deploymentList = getDeployments(namespace);
        if (deploymentList.isEmpty()) return null;
        for(V1Deployment deployment:deploymentList) {
            if (name.equals(deployment.getMetadata().getName())) {
                AppsV1Api appsV1Api = new AppsV1Api();
                try {
                    V1Deployment result = appsV1Api.readNamespacedDeployment(name, namespace, null, null, null);
                    return result;
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    public V1Job getJob(String name, String namespace){
        List<V1Job> jobs = getJobs(namespace);
        if (jobs.isEmpty()) return null;
        for(V1Job job:jobs){
            if(name.equals(job.getMetadata().getName())){
                BatchV1Api batchV1Api = new BatchV1Api();
                try {
                    V1Job theJob = batchV1Api.readNamespacedJob(name, namespace, null, null, null);
                    return theJob;
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    public String createNamespace(String name) throws ApiException {
        if(!getNamespace(name).equals("")) return name;


        V1Namespace v1Namespace = new V1Namespace();
        v1Namespace.setApiVersion("v1");
        v1Namespace.setKind("Namespace");

        V1ObjectMeta objectMeta = new V1ObjectMeta();
        objectMeta.setName(name);

        HashMap<String, String> labels = new HashMap<>();
        labels.put("user", name);
        objectMeta.setLabels(labels);
        v1Namespace.setMetadata(objectMeta);

        CoreV1Api coreV1Api = new CoreV1Api();
        V1Namespace namespace = coreV1Api.createNamespace(v1Namespace, null, null, null);
        return Objects.requireNonNull(namespace.getMetadata()).getName();
    }

    public String getNamespace(String name) {
        //TODO
        // search namespaceList and get userNamespace
        // If not present then create one
        CoreV1Api coreV1Api = new CoreV1Api();
        V1NamespaceList v1NamespaceList = null;
        try {
            v1NamespaceList = coreV1Api.listNamespace(
                    null, false, null, null,
                    null, null, null,
                    null, null, false);
        } catch (ApiException e) {
            e.printStackTrace();
        }
        if (v1NamespaceList==null) return null;

        V1Namespace namespace=null;
        for(V1Namespace item:v1NamespaceList.getItems()){
            if(name.equals(Objects.requireNonNull(item.getMetadata()).getName()))
                namespace=item;
        }
        if(namespace!=null) return namespace.getMetadata().getName();

        return "";
    }


    public boolean createDeploymentAndService(Development info, Integer nodePort,String namespace ) {
        //namespace
        try {
            createNamespace(namespace);
        } catch (ApiException e) {
            e.printStackTrace();
        }


        //basic
        V1Deployment deployment = new V1Deployment();
        deployment.setApiVersion("apps/v1");
        deployment.setKind("Deployment");

        //metadata
        V1ObjectMeta objectMeta = new V1ObjectMeta();
        objectMeta.setName(info.getName());
        objectMeta.setNamespace(namespace);

        //annotation
        Map<String, String> annotation = new HashMap<>();
        annotation.put("type", "development-container");
        objectMeta.setAnnotations(annotation);

        //label
        Map<String, String> labels = new HashMap<>();
        labels.put("user", info.getUser());
        labels.put("app", info.getName());
        objectMeta.setLabels(labels);


        //spec
        V1DeploymentSpec deploymentSpec = new V1DeploymentSpec();
        deploymentSpec.setReplicas(1);
        deploymentSpec.setMinReadySeconds(10);

//        V1DeploymentStrategy v1DeploymentStrategy = new V1DeploymentStrategy();
//        deploymentSpec.setStrategy(v1DeploymentStrategy);


        //template
        V1PodTemplateSpec templateSpec = new V1PodTemplateSpec();
        templateSpec.setMetadata(objectMeta);

        V1PodSpec podSpec = new V1PodSpec();

            //secrets
        List<V1LocalObjectReference> imagePullSecretes=new ArrayList<>();
        V1LocalObjectReference harborSecret = new V1LocalObjectReference();
        harborSecret.setName("harbor-admin-secret");
        imagePullSecretes.add(harborSecret);
        podSpec.setImagePullSecrets(imagePullSecretes);

        List<V1Container> listContainer = new ArrayList<>();
        V1Container container = new V1Container();
        container.setName(info.getName());
        container.setImage(
                info.getDefaultImage()==null ? info.getBaseImage():info.getDefaultImage()
        );
        container.setImagePullPolicy("Always");


            //request & limit
        V1ResourceRequirements v1ResourceRequirements = new V1ResourceRequirements();
        Map<String, Quantity> requests = new HashMap<>();
        requests.put("cpu", new Quantity(info.getCpuNumbers()));  //"200m"
        requests.put("memory", new Quantity(info.getMemory())); //"10Mi"
        v1ResourceRequirements.setRequests(requests);

        if (info.getGpuNumbers() != 0) {
            Map<String, Quantity> limits = new HashMap<>();
            limits.put("nvidia.com/gpu", new Quantity(String.valueOf(info.getGpuNumbers())));
            v1ResourceRequirements.setLimits(limits);
        }

        container.setResources(v1ResourceRequirements);

            //command
        List<String> command = new ArrayList<>();
        command.add("/bin/bash");
        command.add("-c");
        container.setCommand(command);

        List<String> args = new ArrayList<>();
        args.add(
                "/usr/sbin/sshd && " +
                info.getCommand()+
                "&& while true;do sleep 10; done;");
        container.setArgs(args);

            //containerPort
        List<V1ContainerPort> listContainerPort = new ArrayList<>();
        V1ContainerPort containerPort = new V1ContainerPort();
        containerPort.setContainerPort(info.getContainerPort());
        listContainerPort.add(containerPort);
        container.setPorts(listContainerPort);


//        //V1VolumeMount
//        List<V1VolumeMount> v1VolumeMounts = new ArrayList<>();
//        V1VolumeMount v1VolumeMount = new V1VolumeMount();
//        v1VolumeMount.setMountPath("/data/");
//        v1VolumeMount.setName("nfs-file");
//        v1VolumeMounts.add(v1VolumeMount);
//        container.setVolumeMounts(v1VolumeMounts);
//
//        List<V1Volume> volumes =new ArrayList<>();
//        V1Volume v1Volume = new V1Volume();
//        v1Volume.setName("nfs-file");
//        V1NFSVolumeSource v1NFSVolumeSource = new V1NFSVolumeSource();
//        v1NFSVolumeSource.setServer("10.16.17.92");
//        v1NFSVolumeSource.setPath("/mnt/data/Users/"+info.getUser());
//        v1NFSVolumeSource.setReadOnly(false);
//        v1Volume.setNfs(v1NFSVolumeSource);
//        volumes.add(v1Volume);
//        podSpec.setVolumes(volumes);


        listContainer.add(container);
        podSpec.setContainers(listContainer);
        templateSpec.setSpec(podSpec);

        deploymentSpec.setTemplate(templateSpec);

        //selector
        Map<String, String> matchLabels = new HashMap<>();
        matchLabels.put("user", labels.get("user"));
        matchLabels.put("app", labels.get("app"));
        V1LabelSelector selector = new V1LabelSelector();
        selector.setMatchLabels(matchLabels);

        deploymentSpec.setTemplate(templateSpec);
        deploymentSpec.setSelector(selector);

        deployment.setMetadata(objectMeta);
        deployment.setSpec(deploymentSpec);

        //svc
        V1Service svc = new V1Service();
        svc.setApiVersion("v1");
        svc.setKind("Service");
        svc.setMetadata(objectMeta);

        //spec
        V1ServiceSpec v1ServiceSpec = new V1ServiceSpec();
        v1ServiceSpec.setType("NodePort");

        //selector
        v1ServiceSpec.setSelector(matchLabels);

        //ports
        List<V1ServicePort> ports=new ArrayList<>();
        V1ServicePort port = new V1ServicePort();
        port.setPort(80);
        port.setTargetPort(new IntOrString(22));
        port.setNodePort(nodePort);
        ports.add(port);
        v1ServiceSpec.setPorts(ports);
        svc.setSpec(v1ServiceSpec);

        CoreV1Api coreV1Api = new CoreV1Api();
        AppsV1Api appsV1Api = new AppsV1Api();

        try {
            FileWriter fileWriter = new FileWriter("./development-template.yml");
            Yaml.dump(deployment, fileWriter);

            FileWriter fileWriter2 = new FileWriter("./svc-development-template.yml");
            Yaml.dump(svc, fileWriter2);
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            appsV1Api.createNamespacedDeployment(objectMeta.getNamespace(), deployment, null, null, null);
            coreV1Api.createNamespacedService(objectMeta.getNamespace(), svc, null, null, null);
            return true;
        } catch (ApiException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean createJob(Algorithm algorithm, String namespace){
        //namespace
        try {
            createNamespace(namespace);
        } catch (ApiException e) {
            e.printStackTrace();
        }

        //basic
        V1Job job = new V1Job();
        job.setApiVersion("batch/v1");
        job.setKind("Job");

        //metadata
        V1ObjectMeta objectMeta = new V1ObjectMeta();
        objectMeta.setName("alg-"+algorithm.getAlgorithmNo());
        objectMeta.setNamespace(namespace);

        //label
        Map<String, String> labels = new HashMap<>();
        labels.put("app", objectMeta.getName());
        objectMeta.setLabels(labels);

        //spec
        V1JobSpec jobSpec = new V1JobSpec();
        jobSpec.setCompletions(1);
        jobSpec.setParallelism(1);

        //template
        V1PodTemplateSpec templateSpec = new V1PodTemplateSpec();
        templateSpec.setMetadata(objectMeta);

        V1PodSpec podSpec = new V1PodSpec();
        podSpec.setRestartPolicy("Never");

        //secrets
        List<V1LocalObjectReference> imagePullSecretes=new ArrayList<>();
        V1LocalObjectReference harborSecret = new V1LocalObjectReference();
        harborSecret.setName("harbor-admin-secret");
        imagePullSecretes.add(harborSecret);
        podSpec.setImagePullSecrets(imagePullSecretes);

        List<V1Container> listContainer = new ArrayList<>();
        V1Container container = new V1Container();
        container.setName(objectMeta.getName());
        container.setImage(
                algorithm.getImageName()
        );
        container.setImagePullPolicy("IfNotPresent");

        //request & limit
        AlgorithmPackage algorithmPackage = algorithm.getAlgorithmPackage();

        V1ResourceRequirements v1ResourceRequirements = new V1ResourceRequirements();
        Map<String, Quantity> requests = new HashMap<>();
        requests.put("cpu", new Quantity(algorithmPackage.getCpuNumbers()));  //"200m"
        requests.put("memory", new Quantity(algorithmPackage.getMemory())); //"10Mi"
        v1ResourceRequirements.setRequests(requests);

        if (algorithmPackage.getGpuNumbers() != 0) {
            Map<String, Quantity> limits = new HashMap<>();
            limits.put("nvidia.com/gpu", new Quantity(String.valueOf(algorithmPackage.getGpuNumbers())));
            v1ResourceRequirements.setLimits(limits);
        }

        container.setResources(v1ResourceRequirements);

        //command
        List<String> command = new ArrayList<>();
        command.add("/bin/bash");
        command.add("-c");
        container.setCommand(command);

        List<String> args = new ArrayList<>();
        args.add("/usr/sbin/sshd && " +
                "chmod +x /gitCompProject/"+algorithm.getCompileScript()+" && /bin/bash /gitCompProject/"+algorithm.getCompileScript()+
                " && "+algorithm.getShellParams());
        container.setArgs(args);

        //containerPort
        List<V1ContainerPort> listContainerPort = new ArrayList<>();
        V1ContainerPort containerPort = new V1ContainerPort();
        containerPort.setContainerPort(22);
        listContainerPort.add(containerPort);
        container.setPorts(listContainerPort);


        //V1VolumeMount
        List<V1VolumeMount> v1VolumeMounts = new ArrayList<>();
        V1VolumeMount exposeFile = new V1VolumeMount();
        exposeFile.setMountPath("/CompileData");
        exposeFile.setName("expose-file");
        v1VolumeMounts.add(exposeFile);

        V1VolumeMount gitRepo = new V1VolumeMount();
        gitRepo.setMountPath("/gitCompProject");
        gitRepo.setName("git-repo");
        v1VolumeMounts.add(gitRepo);
        container.setVolumeMounts(v1VolumeMounts);


        //V1Volume
        List<V1Volume> volumes =new ArrayList<>();
        V1Volume v1Volume = new V1Volume();
        v1Volume.setName("expose-file");
        V1NFSVolumeSource v1NFSVolumeSource = new V1NFSVolumeSource();
        v1NFSVolumeSource.setServer("10.16.17.92");
        v1NFSVolumeSource.setPath("/mnt/data/Algorithms/"+algorithm.getAlgorithmNo());
        v1NFSVolumeSource.setReadOnly(false);
        v1Volume.setNfs(v1NFSVolumeSource);
        volumes.add(v1Volume);

        V1Volume v1Volume2 = new V1Volume();
        v1Volume2.setName("git-repo");
        V1GitRepoVolumeSource v1GitRepoVolumeSource = new V1GitRepoVolumeSource();
        v1GitRepoVolumeSource.setRepository(algorithm.getCodePath());
        v1GitRepoVolumeSource.setRevision(algorithm.getBranch());
        v1GitRepoVolumeSource.setDirectory(".");
        v1Volume2.setGitRepo(v1GitRepoVolumeSource);
        volumes.add(v1Volume2);

        podSpec.setVolumes(volumes);



        listContainer.add(container);
        podSpec.setContainers(listContainer);
        templateSpec.setSpec(podSpec);


        jobSpec.setTemplate(templateSpec);


        job.setMetadata(objectMeta);
        job.setSpec(jobSpec);

        BatchV1Api batchV1Api = new BatchV1Api();

        try {
            FileWriter fileWriter = new FileWriter("./alg-template.yml");
            Yaml.dump(job, fileWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            batchV1Api.createNamespacedJob(objectMeta.getNamespace(), job, null, null, null);
            return true;
        } catch (ApiException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createPodAndService(Algorithm algorithm, String id, Integer nodePort, String namespace){
        //namespace
        try {
            createNamespace(namespace);
        } catch (ApiException e) {
            e.printStackTrace();
        }
        V1Pod pod = new V1Pod();
        pod.setApiVersion("v1");
        pod.setKind("Pod");

        V1ObjectMeta objectMeta = new V1ObjectMeta();
        objectMeta.setName("test-"+algorithm.getAlgorithmNo()+"-"+id);
        objectMeta.setNamespace(namespace);

        //label
        Map<String, String> labels = new HashMap<>();
        labels.put("app", objectMeta.getName());
        objectMeta.setLabels(labels);

        //spec
        V1PodSpec podSpec = new V1PodSpec();

        //secrets
        List<V1LocalObjectReference> imagePullSecretes=new ArrayList<>();
        V1LocalObjectReference harborSecret = new V1LocalObjectReference();
        harborSecret.setName("harbor-admin-secret");
        imagePullSecretes.add(harborSecret);
        podSpec.setImagePullSecrets(imagePullSecretes);

        List<V1Container> listContainer = new ArrayList<>();
        V1Container container = new V1Container();
        container.setName(objectMeta.getName());
        container.setImage(
                algorithm.getImageName()
        );
        container.setImagePullPolicy("IfNotPresent");

        //request & limit
        AlgorithmPackage algorithmPackage = algorithm.getAlgorithmPackage();

        V1ResourceRequirements v1ResourceRequirements = new V1ResourceRequirements();
        Map<String, Quantity> requests = new HashMap<>();
        requests.put("cpu", new Quantity(algorithmPackage.getCpuNumbers()));  //"200m"
        requests.put("memory", new Quantity(algorithmPackage.getMemory())); //"10Mi"
        v1ResourceRequirements.setRequests(requests);

        if (algorithmPackage.getGpuNumbers() != 0) {
            Map<String, Quantity> limits = new HashMap<>();
            limits.put("nvidia.com/gpu", new Quantity(String.valueOf(algorithmPackage.getGpuNumbers())));
            v1ResourceRequirements.setLimits(limits);
        }

        container.setResources(v1ResourceRequirements);

        //command
        List<String> command = new ArrayList<>();
        command.add("/bin/bash");
        command.add("-c");
        container.setCommand(command);

        List<String> args = new ArrayList<>();
        args.add("/usr/sbin/sshd && " +
                "chmod +x /gitCompProject/runScript.sh && /bin/bash /gitCompProject/runScript.sh"
                +"&& while true;do sleep 10; done;");
        container.setArgs(args);

        //containerPort
        List<V1ContainerPort> listContainerPort = new ArrayList<>();
        V1ContainerPort containerPort = new V1ContainerPort();
        containerPort.setContainerPort(22);
        listContainerPort.add(containerPort);
        container.setPorts(listContainerPort);

        List<V1VolumeMount> v1VolumeMounts = new ArrayList<>();
        V1VolumeMount exposeFile = new V1VolumeMount();
        exposeFile.setMountPath("/TestData/LOG");
        exposeFile.setName("expose-file");
        v1VolumeMounts.add(exposeFile);

        V1VolumeMount exposeFile2 = new V1VolumeMount();
        exposeFile2.setMountPath("/TestData");
        exposeFile2.setName("demo");
        v1VolumeMounts.add(exposeFile2);

        V1VolumeMount gitRepo = new V1VolumeMount();
        gitRepo.setMountPath("/gitCompProject");
        gitRepo.setName("git-repo");
        v1VolumeMounts.add(gitRepo);
        container.setVolumeMounts(v1VolumeMounts);



        List<V1Volume> volumes =new ArrayList<>();
        V1Volume v1Volume = new V1Volume();
        v1Volume.setName("expose-file");
        V1NFSVolumeSource v1NFSVolumeSource = new V1NFSVolumeSource();
        v1NFSVolumeSource.setServer("10.16.17.92");
        v1NFSVolumeSource.setPath("/mnt/data/Algorithms/"+algorithm.getAlgorithmNo()+"/TEST/"+id);
        v1NFSVolumeSource.setReadOnly(false);
        v1Volume.setNfs(v1NFSVolumeSource);
        volumes.add(v1Volume);


        V1Volume v1Volume1 = new V1Volume();
        v1Volume1.setName("demo");
        V1NFSVolumeSource v1NFSVolumeSource2 = new V1NFSVolumeSource();
        v1NFSVolumeSource2.setServer("10.16.17.92");
        v1NFSVolumeSource2.setPath("/mnt/data/Algorithms/"+algorithm.getAlgorithmNo()+"/DEMO");
        v1NFSVolumeSource2.setReadOnly(false);
        v1Volume1.setNfs(v1NFSVolumeSource2);
        volumes.add(v1Volume1);

        V1Volume v1Volume2 = new V1Volume();
        v1Volume2.setName("git-repo");
        V1GitRepoVolumeSource v1GitRepoVolumeSource = new V1GitRepoVolumeSource();
        v1GitRepoVolumeSource.setRepository(algorithm.getCodePath());
        v1GitRepoVolumeSource.setRevision(algorithm.getBranch());
        v1GitRepoVolumeSource.setDirectory(".");
        v1Volume2.setGitRepo(v1GitRepoVolumeSource);
        volumes.add(v1Volume2);

        podSpec.setVolumes(volumes);

        listContainer.add(container);
        podSpec.setContainers(listContainer);

        pod.setMetadata(objectMeta);
        pod.setSpec(podSpec);



        //svc
        V1Service svc = new V1Service();
        svc.setApiVersion("v1");
        svc.setKind("Service");
        svc.setMetadata(objectMeta);

        //spec
        V1ServiceSpec v1ServiceSpec = new V1ServiceSpec();
        v1ServiceSpec.setType("NodePort");

        //selector
        Map<String, String> matchLabels = new HashMap<>();
        matchLabels.put("app", labels.get("app"));
        v1ServiceSpec.setSelector(matchLabels);

        //ports
        List<V1ServicePort> ports=new ArrayList<>();
        V1ServicePort port = new V1ServicePort();
        port.setPort(8022);
        port.setTargetPort(new IntOrString(22));
        port.setNodePort(nodePort);
        ports.add(port);
        v1ServiceSpec.setPorts(ports);
        svc.setSpec(v1ServiceSpec);

        CoreV1Api coreV1Api = new CoreV1Api();

        try {
            FileWriter fileWriter = new FileWriter("./test-template.yml");
            Yaml.dump(pod, fileWriter);

            fileWriter = new FileWriter("./svc-test-template.yml");
            Yaml.dump(svc, fileWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            coreV1Api.createNamespacedPod(objectMeta.getNamespace(), pod, null, null, null);
            coreV1Api.createNamespacedService(objectMeta.getNamespace(), svc, null, null, null);
            return true;
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteDeployment(String name,String namespace) {
        AppsV1Api appsV1Api = new AppsV1Api();
        try {
            V1Status aDefault = appsV1Api.deleteNamespacedDeployment(name, namespace, null, null, 30, null, null, null);
            logger.info("Delete Deployment: " + aDefault.getStatus());
            return true;
        } catch (ApiException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteJob(String name, String namespace) {
        BatchV1Api batchV1Api = new BatchV1Api();
        try {
            V1Status aDefault = batchV1Api.deleteNamespacedJob(name,
                    namespace,
                    null,
                    null,
                    5,
                    null,
                    "Background",
                    null);
            logger.info("Delete Job: " + aDefault.getStatus());
            return true;

        } catch (ApiException e) {
            e.printStackTrace();
        }
        return false;
    }


    public void createServiceByArgs(Development info) {

    }

    public static void main(String[] args) throws IOException, ApiException {
        Cluster cluster = new Cluster("cluster-config");

        //cluster.createPodByYaml("");

        Development info = JSONObject.toJavaObject(Development.testExample, Development.class);
        //logger.info(cluster.createNamespace("heiheiei"));

        //logger.info(cluster.createDeploymentByArgs(info,"eg-test").toString());

        Algorithm algorithm = new Algorithm();
        algorithm.setAlgorithmNo("1");
        algorithm.setCodePath("https://github.com/iwannastay/helloworld.git");
        algorithm.setAlgorithmPackage(new AlgorithmPackage(
                "200m",
                "800Mi",
                null,
                1
        ));
        algorithm.setImageName("10.16.17.92:8433/public/ubuntu16.04-ssh:v1");

        System.out.println(cluster.createJob(algorithm,"default"));
        cluster.getJob("alg-1","default");
        V1beta1Event v1beta1Event = new V1beta1Event();
        //cluster.deleteJob("alg-1","default");

        //cluster.deleteDeployment(info.getName(),"eg-test");

//        List<V1Pod> pods = cluster.getPods("eg-test","user=iws");

//        List<V1Pod> pods = cluster.getPods("logs",null);
//        for (V1Pod pod : pods) {
//            Information podinfo = new Information();
//            logger.info(podinfo.extractInfo(pod).toInfo());
//        }

//        //JAVA <--> JSON
//        info.setStatus("Oops!");
//        String s = JSON.toJSONString(info);
//        JSONObject jsonObject = JSONObject.parseObject(s);
//        Information reinfo = JSON.toJavaObject(jsonObject, Information.class);
//        logger.info(info);
//        logger.info(s);
//        logger.info(reinfo);


        //cluster.getPodByName("test-iws","eg-test");
        logger.info("End");
    }

    public boolean deletePod(String name, String namespace) {
        CoreV1Api coreV1Api = new CoreV1Api();
        try {
            V1Pod pod = coreV1Api.deleteNamespacedPod(name, namespace, null, null, 5, null, null, null);
            logger.info("Delete test: " + pod.getMetadata().getName());
            return true;
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteSvc(String name, String namespace) {
        CoreV1Api coreV1Api = new CoreV1Api();
        try {
            V1Status v1Status = coreV1Api.deleteNamespacedService(name, namespace, null, null, 5, null, null, null);
            logger.info("Delete test svc: " + v1Status.getStatus());
            return true;
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return false;
    }

    public V1Service getSvc(String name, String namespace) {
        List<V1Service> sVcs = getSVcs(namespace);
        if (sVcs.isEmpty()) return null;
        for(V1Service svc:sVcs){
            if(name.equals(svc.getMetadata().getName())){
                CoreV1Api coreV1Api = new CoreV1Api();
                try {
                    V1Service v1Service = coreV1Api.readNamespacedService(name, namespace, null, null, null);
                    return v1Service;
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public List<V1Service> getSVcs(String namespace) {
        CoreV1Api api = new CoreV1Api(client);

        V1ServiceList list = null;
        List<V1Service> svcList=null;
        try {
            list = api.listNamespacedService(namespace,null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null);

            svcList=list.getItems();
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return svcList;
    }
}

