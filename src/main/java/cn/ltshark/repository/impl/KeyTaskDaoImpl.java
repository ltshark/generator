package cn.ltshark.repository.impl;

import cn.ltshark.entity.KeyTask;
import cn.ltshark.repository.KeyTaskDao;
import cn.ltshark.util.GlobalConfig;
import com.google.common.io.Files;
import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    public Page<KeyTask> findAll(int pageNumber, int pageSize) {
        return null;
    }

    @Override
    public void save(KeyTask entity) {
        String savePath = GlobalConfig.getStatePath(entity.getStatus());
        File file = new File(savePath, entity.getUserLoginName());
        try {
            FileUtils.writeStringToFile(file, entity.toString(), GlobalConfig.UTF_8);
            if (KeyTask.APPLYING_STATUS.equals(entity.getStatus()))
                return;
            File applyingFile = new File(GlobalConfig.getStatePath(KeyTask.APPLYING_STATUS), entity.getUserLoginName());
            boolean b = FileUtils.deleteQuietly(applyingFile);
            if (!b) {
                logger.error("delete task error {}", applyingFile.getAbsolutePath());
            }
        } catch (IOException e) {
            logger.error("save task error", e);
            FileUtils.deleteQuietly(file);
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
    public Page<KeyTask> findAll(Map<String, Object> spec, PageRequest pageRequest) {
        String path = GlobalConfig.getStatePath(String.valueOf(spec.get("status")));
        File[] files = new File(path).listFiles();
        if (files == null || files.length == 0)
            return new PageImpl<KeyTask>(Collections.<KeyTask>emptyList(), pageRequest, 0);
        List<KeyTask> list = new ArrayList<KeyTask>();
        for (File file : files) {
            try {
                KeyTask e = KeyTask.toKeyTask(Files.readFirstLine(file, GlobalConfig.UTF_8_CHARSET));
                list.add(e);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return new PageImpl<KeyTask>(list, pageRequest, list.size());
    }

}
