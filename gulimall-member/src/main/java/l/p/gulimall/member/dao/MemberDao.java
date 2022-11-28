package l.p.gulimall.member.dao;

import l.p.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author lp
 * @email lpruoyu@gmail.com
 * @date 2022-11-27 13:56:40
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
