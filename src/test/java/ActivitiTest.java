import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.task.Task;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

/**
 * TODO
 *
 * @author caodahuan
 * @version 2019/8/27
 */
public class ActivitiTest {

    /**
     * 创建流程
     * @param
     * @author caodahuan
     * @date 2019/8/27
     * @return void
     */
    @Test
    public void createActivitiTask(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        processEngine.getRepositoryService().createDeployment()
                .addClasspathResource("shenqing.bpmn")
                .addClasspathResource("shenqing.png")
                .deploy();
    }

    /**
     * 启动流程实例
     * @param
     * @author caodahuan
     * @date 2019/8/27
     * @return void
     */
    @Test
    public void testStartProcessInstance(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        processEngine.getRuntimeService()
                .startProcessInstanceById("shenqing_1:2:27504");//这个是查看数据库中act_re_procdef表
    }

    /**
     * 完成请假申请
     * @param
     * @author caodahuan
     * @date 2019/8/27
     * @return void
     */
    @Test
    public void testQingjia(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        processEngine.getTaskService()
                .complete("30005");
    }

    /**
     * 班主任查询当前正在执行任务
     * @param
     * @author caodahuan
     * @date 2019/8/27
     * @return void
     */
    @Test
    public void testQueryTask(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        List<Task> tasks = processEngine.getTaskService()
                .createTaskQuery()
                .taskAssignee("小毛")
                .list();
        tasks.forEach(t -> System.out.println(t.getName()));
    }

    /**
     * 小毛完成任务
     * @param
     * @author caodahuan
     * @date 2019/8/27
     * @return void
     */
    @Test
    public void testFinishTask_manager(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        processEngine.getTaskService()
                .complete("32502");
    }

    /**
     * 教务处的大毛完成任务
     * @param
     * @author caodahuan
     * @date 2019/8/27
     * @return void
     */
    @Test
    public void testFinishTask_Boss(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        processEngine.getTaskService()
                .complete("40002");
    }

    /**
     * 删除流程
     * @author caodahuan
     * @date 2019/8/27
     * @return void
     */
    @Test
    @Ignore
    public void testDelete(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        repositoryService.deleteDeployment("2501",true);
    }
}
