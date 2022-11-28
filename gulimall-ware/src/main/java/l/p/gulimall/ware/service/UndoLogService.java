package l.p.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import l.p.common.utils.PageUtils;
import l.p.gulimall.ware.entity.UndoLogEntity;

import java.util.Map;

/**
 * 
 *
 * @author lp
 * @email lpruoyu@gmail.com
 * @date 2022-11-27 14:11:35
 */
public interface UndoLogService extends IService<UndoLogEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

