package cn.ltshark.util;

import cn.ltshark.entity.KeyTask;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ltshark on 15/7/10.
 */
@Component
public class GlobalConfig {

    public static final String UTF_8 = "UTF-8";

    public static final String ROOT_PATH = "/data/generator";

    public static final String KEY_TASK_PATH = ROOT_PATH + "/keyTask";
    public static final String APPLYING_TASK_PATH = KEY_TASK_PATH + "/applying";
    public static final String AGREE_TASK_PATH = KEY_TASK_PATH + "/agree";
    public static final String REFUSE_TASK_PATH = KEY_TASK_PATH + "/refuse";

    public static final String KEY_PATH = ROOT_PATH + "/key";

    public static final String[] TASK_PATHS = new String[]{APPLYING_TASK_PATH, AGREE_TASK_PATH, REFUSE_TASK_PATH};

    private static final Map<String, String> stateToPath;

    static {
        stateToPath = new HashMap<String, String>();
        stateToPath.put(KeyTask.APPLYING_STATUS, APPLYING_TASK_PATH);
        stateToPath.put(KeyTask.AGREE_APPLY_STATUS, AGREE_TASK_PATH);
        stateToPath.put(KeyTask.REFUSE_APPLY_STATUS, REFUSE_TASK_PATH);
    }

    public static String getStatePath(String state) {
        return stateToPath.get(state);
    }
}
