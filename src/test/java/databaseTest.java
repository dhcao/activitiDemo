import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.junit.Test;

/**
 * TODO
 *
 * @author caodahuan
 * @version 2019/8/27
 */
public class databaseTest {

    @Test
    public void createTable(){
        final ProcessEngine processEngine = ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("activiti.cfg.xml")
                .buildProcessEngine();

    }
}
