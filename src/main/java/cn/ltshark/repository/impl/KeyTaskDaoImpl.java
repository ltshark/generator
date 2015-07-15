package cn.ltshark.repository.impl;

import cn.ltshark.entity.KeyTask;
import cn.ltshark.repository.KeyTaskDao;
import cn.ltshark.util.GlobalConfig;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by surfrong on 2015/7/13.
 */
@Component
public class KeyTaskDaoImpl implements KeyTaskDao {

    private static Logger logger = LoggerFactory.getLogger(KeyTaskDao.class);

    @Override
    public void deleteByUserLoginName(String id) {
        for (String path : GlobalConfig.TASK_PATHS) {
            File file = new File(path, id);
            if (!file.exists()) {
                continue;
            }
            file.delete();
        }
    }

    @Override
    public void save(List<KeyTask> keyTasks) {

    }

    @Override
    public KeyTask findOne(String id) {
        KeyTask keyTask = null;
        for (String path : GlobalConfig.TASK_PATHS) {
            File file = new File(path, id);
            if (!file.exists())
                continue;
            try {
                keyTask = KeyTask.toKeyTask(FileUtils.readFileToString(file, GlobalConfig.UTF_8));
            } catch (IOException e) {
                logger.info("getUserKeyTask %s", e.getMessage());
            }
            break;
        }
        return keyTask;
    }

    @Override
    public List<KeyTask> findAll() {
        return null;
    }

    @Override
    public void save(KeyTask entity) {
        String savePath = GlobalConfig.getStatePath(entity.getStatus());
        try {
            FileUtils.writeStringToFile(new File(savePath, entity.getUserLoginName()), entity.toString(), GlobalConfig.UTF_8);
        } catch (IOException e) {
            logger.error("save task error", e);
        }
    }

    @Override
    public List<KeyTask> findAll(List<Long> taskIds) {
        return null;
    }

    @Override
    public KeyTask findOne(Specification<KeyTask> spec) {
        return null;
    }

    @Override
    public List<KeyTask> findAll(Specification<KeyTask> spec) {
        return null;
    }

    @Override
    public Page<KeyTask> findAll(Specification<KeyTask> spec, PageRequest pageRequest) {
        return null;
    }
}
