package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(new Query<CategoryEntity>().getPage(params), new QueryWrapper<CategoryEntity>());

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listTree() {
        // 查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        /// 组装父子结构：

        // 查出一级分类
        List<CategoryEntity> level1 = entities.stream()
                .filter(entity -> entity.getParentCid() == 0)
                .map(entity -> {
                    entity.setChildren(getChildren(entity, entities));
                    return entity;
                })
                // 0 是排序的默认值
                .sorted((menu1, menu2) -> (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort()))
                .collect(Collectors.toList());
        return level1;
    }

    @Override
    public void removeMenusByIds(List<Long> ids) {
        //TODO 检查当前删除的菜单，是否被别的地方引用

//        这个方法不推荐直接使用，推荐使用逻辑删除
        baseMapper.deleteBatchIds(ids);
    }

    // 递归查找所有子分类
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> collect = all.stream()
//                .filter(entity -> entity.getParentCid() == root.getCatId()) // 这样写是有bug的
                .filter(entity -> entity.getParentCid().equals(root.getCatId())) // 应该使用equals比较
                .map(entity -> {
                    entity.setChildren(getChildren(entity, all));
                    return entity;
                })
                .sorted((menu1, menu2) -> (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort()))
                .collect(Collectors.toList());
        return collect;
    }


    /*
        public static void main(String[] args) {
        new ArrayList<Integer>().stream()
                .sorted((menu1, menu2) -> {
                            System.out.println(menu1);
                            System.out.println(menu2);
                            return (menu1  == null ? 0 : menu1) - (menu2 == null ? 0 : menu2 );
                        }
                        )
                .collect(Collectors.toList());
        System.out.println("不会报错");
    }
     */

}