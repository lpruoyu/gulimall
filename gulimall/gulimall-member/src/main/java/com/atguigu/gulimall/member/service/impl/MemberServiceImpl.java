package com.atguigu.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.member.dao.MemberDao;
import com.atguigu.gulimall.member.dao.MemberLevelDao;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.entity.MemberLevelEntity;
import com.atguigu.gulimall.member.exception.PasswordErrorException;
import com.atguigu.gulimall.member.exception.PhoneExistException;
import com.atguigu.gulimall.member.exception.UserErrorException;
import com.atguigu.gulimall.member.exception.UsernameExistException;
import com.atguigu.gulimall.member.service.MemberService;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.MemberRegistVo;
import com.atguigu.gulimall.member.vo.SocialUserAccessToken;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(new Query<MemberEntity>().getPage(params), new QueryWrapper<MemberEntity>());

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo vo) {
        //检查用户名和手机号是否唯一。为了让controller能感知异常：异常机制
        String phone = vo.getPhone();
        checkPhoneUnique(phone);
        String userName = vo.getUserName();
        checkUsernameUnique(userName);

        MemberEntity entity = new MemberEntity();
        entity.setMobile(phone);
        entity.setUsername(userName);
        entity.setNickname(userName);

        //设置默认等级
        MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();
        entity.setLevelId(levelEntity.getId());

        //密码要进行加密存储。//当然，也可以在前端就加密发过来
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(vo.getPassword());
        entity.setPassword(encode);

        //其他的默认信息
        //保存
        this.baseMapper.insert(entity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        Integer mobile = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (mobile > 0) {
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkUsernameUnique(String username) throws UsernameExistException {
        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if (count > 0) {
            throw new UsernameExistException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) throws UserErrorException, PasswordErrorException {
        String loginacct = vo.getLoginacct();
        MemberEntity member = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginacct).or().eq("mobile", loginacct));
        if (null == member) {
            throw new UserErrorException();
        }
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        boolean matches = passwordEncoder.matches(vo.getPassword(), member.getPassword());
        if (!matches) {
            throw new PasswordErrorException();
        }
        return member;
    }

    @Override
    public MemberEntity login(SocialUserAccessToken socialUser) {
        //登录和注册合并逻辑

        String uid = socialUser.getUid(); // 社交网站用于标识用户的唯一ID
        String accessToken = socialUser.getAccess_token();

        //1、判断当前社交用户是否已经登录过系统；
        MemberDao memberDao = this.baseMapper;
        MemberEntity memberEntity = memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));

        long expiresIn = socialUser.getExpires_in();

        if (memberEntity != null) {
            //这个用户已经注册过
            MemberEntity update = new MemberEntity();
            update.setAccessToken(accessToken);
            update.setExpiresIn(expiresIn);
            memberDao.updateById(update);

            memberEntity.setAccessToken(accessToken);
            memberEntity.setExpiresIn(expiresIn);
            return memberEntity;
        } else {
            //2、没有查到当前社交用户对应的记录我们就需要注册一个(将social_id和我们系统的会员id关联起来)
            MemberEntity regist = new MemberEntity();

            try {
                //3、查询当前社交用户的社交账号信息（昵称，性别等）
                Map<String, String> query = new HashMap<>();
                query.put("access_token", accessToken);
                query.put("uid", uid);
                HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", new HashMap<>(), query);
                if (response.getStatusLine().getStatusCode() == 200) {
                    //查询成功
                    String json = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = JSON.parseObject(json);
                    //获取用户信息
                    String name = jsonObject.getString("name");
                    String gender = jsonObject.getString("gender");
                    //........
                    regist.setNickname(name);
                    regist.setUsername(name);
                    regist.setGender("m".equals(gender) ? 1 : 0);
                    //........
                }
            } catch (Exception e) {}

//            关联social_uid和用户id
            regist.setSocialUid(socialUser.getUid());
            regist.setAccessToken(accessToken);
            regist.setExpiresIn(expiresIn);

//            注册用户
            memberDao.insert(regist);

            return regist;
        }
    }

}