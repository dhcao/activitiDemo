import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.util.json.JSONObject;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 流程实例API
 * 1.启动流程实例
 *
 * @author caodahuan
 * @version 2019/8/29
 */
public class InstanceTest {

    /**
     * 获取到操作流程实例的总接口
     * @author caodahuan
     * @date 2019/8/29
     * @return void
     */
    public RuntimeService getService(){

        // 1. 先获取流程引擎
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

        /*
         * 2. 获取操作实例的接口
         * 此接口与流程部署和定义操作的接口RepositoryService一样；
         * 提供了对丰富的操作方式，重点分为：
         * 1）startxxx：以start开头的启动流程实例方法。
         * 2）deleteProcessInstance：删除正在执行的流程
         * 3）getxxx：以get开头的获取流程实例（活动中或者等待中）相关信息，id列表，实例列表等等
         * 4）trigger：触发器形式，可以指定流程ID，和一些参数，指定流程执行。
         * 5）builder：获取流程实例构造器
         * 6）一些其他操作：流程构造器、参数的获取，修改，添加等等
         *
         */
        RuntimeService runtimeService = processEngine.getRuntimeService();

        return runtimeService;
    }

    /**
     * 启动流程实例：
     * 方法一：根据流程定义的ID;
     * 已经知道，在部署流程的时候，我们将流程的数据写到了表：act_re_prcodef
     * 通过表act_re_prcodef获取到流程，然后启动流程！
     * @author caodahuan
     * @date 2019/8/29
     * @return void
     */
    @Test
    @Transactional
    public void startMethodOne(){

        // 1. 获取到操作流程数据的接口
        RuntimeService service = getService();

        /*
         * 2. 根据ID启动流程,返回的是启动流程后的实例
         * 部署流程后，流程数据其实存储在act_re_procdef表中。根据id启动，即根据act_re_procdef表的id启动。
         * 1）根据流程获取流程定义数据的ID
         * 2）根据ID启动流程
         */
        // 部署好的流程定义数据
        ProcessDefinition definition = this.getDefinition();
        System.out.println(definition.getId());
        /*
         * 根据流程定义数据表中的ID启动流程
         * 启动流程后：操作以下表(可以在整个过程中观察这些表，看看效果，理解更深入)
         * 1、正在执行的流程实例表：
         * act_ru_execution（正在执行的流程实例表）：表示执行中的流程。(完成的实例将被删除)
         * act_ru_task（正在执行的任务表）：表示正在执行的任务。（完成的任务将被删除）
         * 历史表：
         * act_hi_taskinst（任务历史表）：插入一条数据，通过execution_id_关联到流程执行表act_ru_execution；
         * （此表存储已经完成的和正在执行的流程节点）如果end_time有值，表示已经结束(历史)；没值，表示正在执行的节点。
         * act_hi_procinst（实例历史表）：插入一条数据，通过proc_def_id_关联到流程定义表act_re_procdef；
         * （此表存储已经完成的和正在执行的实例）如果end_time有值，表示已经结束(历史)；没值，表示正在执行的实例。
         * act_hi_actinst（元素历史表）：插入2条数据，一条startEvent数据，启动元素完成。一条userTask，正在执行元素。
         * （此表存储已经完成的和正在执行的元素）如果end_time有值，表示已经结束(历史)；没值，表示正在执行的元素。
         */
        service.startProcessInstanceById(definition.getId());
    }

    /**
     * 方法二：根据流程定义（act_re_procdef）中的KEY启动流程实例
     * 通过方法一知道了总的流程，此处就不再展示完整的获取流程。直接从表中获取KEY值。
     * @author caodahuan
     * @date 2019/8/30
     * @return void
     */
    @Test
    public void startMethodTwo(){

        // 1. 获取流程实例操作接口
        RuntimeService service = getService();

        // 2. 从表中已经知道了key
        ProcessInstance apply = service.startProcessInstanceByKey("apply");
    }

    /**
     * 完成任务：
     * 因为完成通过TaskService。
     * @author caodahuan
     * @date 2019/8/30
     * @return void
     */
    @Test
    public void complete(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        taskService.complete("107502");
    }

    /**
     * 获取到流程定义，返回表中数据
     * 流程定义数据在表act_re_procdef表中。
     * @author caodahuan
     * @date 2019/8/29
     * @return void
     */
    private ProcessDefinition getDefinition() {
        // 1. 部署流程并返回流程ID
        String deploymentId = this.deployProcess();

        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        ProcessDefinitionQuery query = processEngine.getRepositoryService().createProcessDefinitionQuery();
        query.orderByDeploymentId();
        query.desc();
        query.deploymentId(deploymentId);
        List<ProcessDefinition> list = query.list();

        if (!CollectionUtils.isEmpty(list)) {
            return list.get(0);
        }

        return null;
    }

    /**
     * 部署一个流程并返回部署的ID，
     * 部署表：act_re_deployment 插入一条数据，并在资源表中记录对应的资源（图片文件、bpmn文件），二进制存储。并将流程的数据插入流程数据表
     * 资源表：act_ge_bytearray 插入对应的资源数据，可能多条，看传入多少文件。
     * 流程数据表：act_re_procdef 插入流程对应的定义数据。
     * @author caodahuan
     * @date 2019/8/29
     * @return java.lang.String
     */
    private String deployProcess(){
        // 1. 获取到部署新流程的构造器
        DeploymentBuilder builder = ProcessEngines.getDefaultProcessEngine().getRepositoryService().createDeployment();

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
        return deploy.getId();
    }
}
