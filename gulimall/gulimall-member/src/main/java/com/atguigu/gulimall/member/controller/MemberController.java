package com.atguigu.gulimall.member.controller;

import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.exception.PasswordErrorException;
import com.atguigu.gulimall.member.exception.PhoneExistException;
import com.atguigu.gulimall.member.exception.UserErrorException;
import com.atguigu.gulimall.member.exception.UsernameExistException;
import com.atguigu.gulimall.member.service.MemberService;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.MemberRegistVo;
import com.atguigu.gulimall.member.vo.SocialUserAccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

/**
 * 会员
 *
 * @author lp
 * @email lpruoyu@gmail.com
 * @date 2024-05-22 14:24:11
 */

//@RefreshScope
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

//    @Autowired
//    private CouponFeignService couponFeignService;

//    @Value("${member.user.name}")
//    private String name;
//    @Value("${member.user.age}")
//    private Integer age;

//    @RequestMapping("/test")
//    public R test1() {
//        return R.ok().put("name", name).put("age", age);
//    }


//    @RequestMapping("/coupons")
//    public R test() {
//        MemberEntity member = new MemberEntity();
//        member.setUsername("lp");
//        return R.ok().put("member", member).put("coupons", couponFeignService.couponInfo());
//    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //  @RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    @PostMapping("/regist")
    public R regist(@RequestBody MemberRegistVo vo) {
        try {
            memberService.regist(vo);
        } catch (PhoneExistException e) {
            return R.error(BizCodeEnume.PHONE_EXIST_EXCEPTION);
        } catch (UsernameExistException e) {
            return R.error(BizCodeEnume.USER_EXIST_EXCEPTION);
        }
        return R.ok();
    }

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo) {
        try {
            MemberEntity member = memberService.login(vo);
            if (member != null) {
                return R.ok().setData(member);
            }
        } catch (UserErrorException e) {
            return R.error(BizCodeEnume.USER_ERROR_EXCEPTION);
        } catch (PasswordErrorException e) {
            return R.error(BizCodeEnume.PASSWORD_ERROR_EXCEPTION);
        }
        return R.error();
    }

    @PostMapping("/socialLogin")
    public R socialLogin(@RequestBody SocialUserAccessToken accessToken) {
        MemberEntity member = memberService.login(accessToken);
        if (member != null) {
            return R.ok().setData(member);
        }
        return R.error(BizCodeEnume.LOGIN_ERROR_EXCEPTION);
    }

}
