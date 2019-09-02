import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.DeploymentQuery;
import org.junit.Test;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 删除流程
 * 部署的流程分别会涉及三张表：
 * act_re_deployment：部署表
 * act_ge_bytearray：文件表
 * act_re_procdef：部署信息表
 *
 * 删除也是这3张表
 * 而且act_re_deployment和act_ge_bytearray有外键关联
 * 所有删除时需要关联删除，如果自己实现删除方法，也要删除关联表数据。
 *
 * @author caodahuan
 * @version 2019/8/28
 */
public class DeleteTest {

    /**
     * 单体删除：直接指定流程部署表的id进行删除
     * @author caodahuan
     * @date 2019/8/28
     * @return void
     */
    @Test
    public void deleteTeset(){

        // 1.获取流程实例
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

        // 2. 获取到“部署和流程定义”的接口
        RepositoryService repositoryService = processEngine.getRepositoryService();

        // 3.通过流程部署操作接口进行删除;第一个参数是部署表act_re_deployment中的主键id。后面的true，表示需要关联删除。
        repositoryService.deleteDeployment("15001",true);
    }

    /**
     * 一个完整的删除过程；
     * 1. 先部署
     * 2. 再查询到主键
     * 3. 最后根据主键删除
     * @author caodahuan
     * @date 2019/8/28
     * @return void
     */
    @Test
    public void deleteWhole(){

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

        // 3.删除；根据id删除，所以要获取ID。第一个参数是部署表act_re_deployment中的主键id。后面的true，表示需要关联删除。
        if (!CollectionUtils.isEmpty(list)) {
            String deploymentId = list.get(0).getId();
            repositoryService.deleteDeployment(deploymentId,true);
        }
    }


}
