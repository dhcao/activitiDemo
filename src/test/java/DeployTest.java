import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.util.io.InputStreamSource;
import org.activiti.engine.impl.util.io.StreamSource;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

/**
 * 部署流程
 *
 * @author caodahuan
 * @version 2019/8/28
 */
public class DeployTest {

    /**
     * 获取到部署的总接口
     *
     * @author caodahuan
     * @date 2019/8/28
     * @return void
     */
    public DeploymentBuilder getBuilder(){
        // 1.获取流程引擎实例(如果没有外部扩展，只会有一个默认的流程引擎实例) 这个是保存在一个map里面。
        // 如果自定义流程引擎，请构造ProcessEngine，并在里面注册进去。
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

        /*
         * 2.这是个流程部署的仓库接口。它处理流程的部署、删除、销毁、查询...等等（门面接口）
         * 源代码定义：提供对流程定义和部署存储库的访问的服务。主要涉及表：
         *  act_ge_bytearray:二进制数据表  png  bpmn 存在这个表
         *  act_re_deployment:部署信息表(id作为act_ge_bytearray的外键)
         *  act_re_procdef:流程定义数据表
         */
        RepositoryService repositoryService = processEngine.getRepositoryService();

        /*
         * 3.这个是用于部署新流程的构造器（建造者模式）;
         * 有2类接口方法：
         * 第一类：加载资源：
         * (1) addInputStream(...):通过流读取流程文件进行部署
         * (2) addClasspathResource(String resource):通过读取资源文件进行部署（png和bpmn）
         * (3) addZipInputStream(ZipInputStream zipInputStream):通过读取zip文件流进行部署
         *
         * 第二类：建造中补充参数：
         * 设置act_re_deployment表中字段：name、category、key、tenantId等等
         */
        DeploymentBuilder deployment = repositoryService.createDeployment();

        return deployment;
    }

    /**
     * 第一种部署方法：通过读取资源文件进行部署；png文件和bpmn文件
     * @author caodahuan
     * @date 2019/8/28
     * @return void
     */
    @Test
    public void methodOne(){

        // 1. 获取到部署新流程的构造器
        DeploymentBuilder builder = getBuilder();

        // 2. 选择部署方式为：通过资源文件进行部署
        builder.addClasspathResource("apply.bpmn");
        builder.addClasspathResource("apply.png");

        // 3. 设置一些需要的参数（非必须）
        builder.key("key"); // 设置key，可重复，可用来启动流程
        builder.name("报销流程"); // 设置流程名称
        builder.category("3"); // 流程类型

        // 4. 部署流程
        Deployment deploy = builder.deploy();
        System.out.println(deploy.getId());
    }

    /**
     * 第二种部署方式：通过 inputstream完成部署
     * 一般如果将流程嵌入到本地运营系统中，我们就是使用这种方法，创建文件之后上传到后台进行读取部署。
     * @author caodahuan
     * @date 2019/8/28
     * @return void
     */
    @Test
    public void methodTwo() throws Exception{

        // 1.获取部署构造器
        DeploymentBuilder builder = getBuilder();

        // 2.获取部署的资源文件：通过inputstream完成部署
        InputStream inputStream = new FileInputStream("C:/Users/84604/Desktop/aa/apply.bpmn");
        builder.addInputStream("apply.bpmn",inputStream);

        // 3. 设置一些需要的参数（非必须）
        builder.key("apply_key"); // 设置key，可重复，可用来启动流程
        builder.name("报销流程"); // 设置流程名称

        // 4. 部署流程
        builder.deploy();
    }

    /**
     * 第三种部署方式：通过读取zip文件流进行部署
     * 将apply.bpmn和apply.png打成一个zip包
     * 一般如果将流程嵌入到本地运营系统中，我们就是使用这种方法，创建文件之后上传到后台进行读取部署。
     * @author caodahuan
     * @date 2019/8/28
     * @return void
     */
    @Test
    public void methodThree() throws Exception{

        // 1. 获取部署构造器
        DeploymentBuilder builder = getBuilder();

        // 2.获取部署的资源文件：通过读取zip文件流进行部署
        InputStream inputStream = new FileInputStream("C:/Users/84604/Desktop/aa/apply.zip");
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        builder.addZipInputStream(zipInputStream);

        // 3. 设置一些需要的参数（非必须）
        builder.name("请假流程");
        builder.key("apply_key");
        builder.category("3");

        /*
         * 4.部署
         * 此处不删除第二种方法的部署，我们可以发现，部署同一个流程的结果：
         * 在‘流程定义数据表’(act_re_procdef)表中version字段，会有1，2。表示版本已经更新
         */
        builder.deploy();
    }

    /**
     * 第四种部署方式：通过读取bytes进行部署
     * 允许直接接受byte流
     * @author caodahuan
     * @date 2019/8/28
     * @return void
     */
    @Test
    public void methodFour() throws Exception{

        // 1. 获取部署构造器
        DeploymentBuilder builder = getBuilder();

        // 2.获取部署的资源文件：通过读取zip文件流进行部署
        InputStream inputStream = new FileInputStream("C:/Users/84604/Desktop/aa/apply.bpmn");

        byte[] b = new byte[inputStream.available()];
        inputStream.read(b);

        builder.addBytes("apply_bpmn",b);

        // 3. 设置一些需要的参数（非必须）
        builder.name("请假流程");
        builder.key("apply_key");
        builder.category("3");

        /*
         * 4.部署
         * 此处不删除第二种方法的部署，我们可以发现，部署同一个流程的结果：
         * 在‘流程定义数据表’(act_re_procdef)表中version字段，会有1，2。表示版本已经更新
         */
        builder.deploy();
    }

    /**
     * 第五种部署方式：构建BpmnModel部署流程
     * apply.bpmn可以直接改后缀为xml，变成一个xml文件。
     * 所以我们提供将xml文件构造成BpmnModel对象；来直接部署流程。
     *
     * @author caodahuan
     * @date 2019/8/28
     * @return void
     */
    @Test
    public void methodFive() throws Exception {

        // 1. 获取部署构造器
        DeploymentBuilder builder = getBuilder();

        // 2.获取部署的资源文件：通过读取zip文件流进行部署
        // 从xml文件中得到BpmnModel
        BpmnModel bpmnModel = readXMLFile("C:/Users/84604/Desktop/aa/apply.xml");
        BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
        String bpmn20Xml = new String(bpmnXMLConverter.convertToXML(bpmnModel), "UTF-8");
        builder.addString("xml_bpmnModel",bpmn20Xml);

        // 3. 设置一些需要的参数（非必须）
        builder.name("请假流程");
        builder.key("apply_key");
        builder.category("3");

        // 4.部署
        builder.deploy();
    }

    /**
     * 将xml读取为BpmnModel
     * @author caodahuan
     * @date 2019/8/28
     * @return org.activiti.bpmn.model.BpmnModel
     */
    protected BpmnModel readXMLFile(String resources) throws Exception {
        InputStream inputStream = new FileInputStream("C:/Users/84604/Desktop/aa/apply.bpmn");
        StreamSource xmlSource = new InputStreamSource(inputStream);
        BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xmlSource, false, false, "UTF-8");
        return bpmnModel;
    }

}
