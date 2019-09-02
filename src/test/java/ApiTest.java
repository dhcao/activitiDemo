import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.DeploymentQuery;
import org.junit.Test;
import org.springframework.util.CollectionUtils;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * 其他的一些API测试
 *
 * @author caodahuan
 * @version 2019/8/27
 */
public class ApiTest {

    /**
     * 根据名称查询流程部署
     * @author caodahuan
     * @date 2019/8/27
     * @return void
     */
    @Test
    public void testQueryByName(){
        // 1. 获取默认的流程引擎
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

        // 2. 关于流程定义和部署的服务，可访问仓库表：act_re_deployment，act_re_deployment
        RepositoryService repositoryService = processEngine.getRepositoryService();
        // 2.1 处理查询条件类方法...类似于mybatis的Example那个处理方式
        DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();
        // 以下排序条件必须填写：
        deploymentQuery.orderByDeploymenTime();
        deploymentQuery.desc();

        // 此处如果不填写查询条件（名称）,并不会默认查询所有的列表，而是null。
        deploymentQuery.deploymentName("请假流程");
//        deploymentQuery.deploymentCategory("x");

        // 3. 真正的查询方法
        List<Deployment> list = deploymentQuery.list();

        list.forEach(x -> System.out.println(x.getName()));

    }

    /**
     * 打印流程图片
     * 1.先创建流程
     * 2.查询查询到流程
     * 3.得到ID，并根据ID查询出图片，并输出
     *
     * @author caodahuan
     * @date 2019/8/28
     * @return void
     */
    @Test
    public void testQueryPng() throws Exception{

        // 前置一：获取到流程操作接口和部署的构造器
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService(); // 流程操作接口
        DeploymentBuilder builder = repositoryService.createDeployment(); // 流程构造器

        // 1. 创建流程
        builder.addClasspathResource("apply.bpmn");
        builder.addClasspathResource("apply.png");
        builder.key("apply_key");
        builder.deploy();

        // 2. 根据key来查询到流程(也可以根据名称来查询)
        DeploymentQuery query = repositoryService.createDeploymentQuery(); // 得到一个查询器
        // 因为主键id是随机生成我们无法知道，所以查询时不能查到唯一(key和name都可重复)，只能查到列表。所以需要指定排序参数
        query.orderByDeploymentId();
        query.desc();
        query.deploymentKey("apply_key"); // 根据key来查询
        List<Deployment> list = query.list();


        // 3. 得到主键，并根据主键和文件名称打印图片！
        if (!CollectionUtils.isEmpty(list)) {
            String deploymentId = list.get(0).getId();
            /**
             * deploymentID
             * 文件的名称
             */
            InputStream inputStream = repositoryService.getResourceAsStream(deploymentId,"apply.png");
            OutputStream outputStream3 = new FileOutputStream("C:/Users/84604/Desktop/aa/apply.png");
            int b = -1 ;
            while ((b=inputStream.read())!=-1){
                outputStream3.write(b);
            }
            inputStream.close();
            outputStream3.close();
        }
    }
}
