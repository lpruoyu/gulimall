package l.p.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import l.p.common.utils.PageUtils;
import l.p.gulimall.member.entity.MemberLevelEntity;

import java.util.Map;

/**
 * 会员等级
 *
 * @author lp
 * @email lpruoyu@gmail.com
 * @date 2022-11-27 13:56:40
 */
public interface MemberLevelService extends IService<MemberLevelEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

