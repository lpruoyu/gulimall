<template>
  <div>
    <el-button type="danger" @click="batchDelete">批量删除</el-button>
    <el-tree
      ref="tree"
      :data="menus"
      :props="defaultProps"
      @node-click="handleNodeClick"
      :expand-on-click-node="false"
      show-checkbox
      node-key="catId"
      :default-expanded-keys="expandedKey"
    >
      <span class="custom-tree-node" slot-scope="{ node, data }">
        <span>{{ node.label }}</span>
        <span>
          <el-button
            v-if="node.level <= 2"
            type="text"
            size="mini"
            @click="() => append(data)"
          >
            Append
          </el-button>
          <el-button type="text" size="mini" @click="() => edit(data)">
            Edit
          </el-button>
          <el-button
            v-if="node.childNodes.length == 0"
            type="text"
            size="mini"
            @click="() => remove(node, data)"
          >
            Delete
          </el-button>
        </span>
      </span>
    </el-tree>
    <el-dialog
      :title="title"
      :visible.sync="dialogFormVisible"
      width="30%"
      :close-on-click-modal="false"
    >
      <el-form :model="category">
        <el-form-item label="分类名称">
          <el-input v-model="category.name" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="分类图标" v-if="dialogType == 'edit'">
          <el-input v-model="category.icon" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="分类单位" v-if="dialogType == 'edit'">
          <el-input
            v-model="category.productUnit"
            autocomplete="off"
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitData">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      dialogType: "", // "append"/"edit"
      title: "",
      category: {
        name: "",
        parentCid: 0,
        catLevel: 0,
        showStatus: 1,
        sort: 0,
        catId: 0 /* 修改时需要用到catId */,
        icon: "",
        productUnit: "",
        productCount: 0,
      },
      dialogFormVisible: false,
      menus: [],
      defaultProps: {
        children: "children",
        label: "name",
      },
      expandedKey: [],
    };
  },
  methods: {
    handleNodeClick(data) {
      console.log(data);
    },
    batchDelete() {
      let checkedKeys = this.$refs.tree.getCheckedKeys();
      console.log("checkedKeys", checkedKeys);
      this.$confirm(`是否批量删除菜单?`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          this.$http({
            url: this.$http.adornUrl(`/product/category/delete`),
            method: "post",
            data: this.$http.adornData(checkedKeys, false),
          })
            .then(({ data }) => {
              if (data.code == 0) {
                this.$message({
                  message: "菜单删除成功",
                  type: "success",
                });
                // 刷新出新的菜单
                this.getMenus();
              } else {
                this.$message.error("菜单删除失败");
              }
            })
            .catch(() => {
              this.$message.error("菜单删除失败");
            });
        })
        .catch(() => {
          this.$message({
            type: "info",
            message: "用户取消删除",
          });
        });
    },
    getMenus() {
      this.$http({
        url: this.$http.adornUrl("/product/category/list/tree"),
        method: "get",
      }).then(({ data }) => {
        // console.log(data.data);
        this.menus = data.data;
      });
    },
    submitData() {
      if (this.dialogType == "edit") {
        this.editCategory();
      }

      if (this.dialogType == "append") {
        this.appendCategory();
      }
    },

    edit(data) {
      console.log("edit:", data);
      this.title = "修改分类";
      this.dialogType = "edit";
      this.dialogFormVisible = true;

      // 回显数据应该从数据库中查找最新数据
      this.$http({
        url: this.$http.adornUrl(`/product/category/info/${data.catId}`),
        method: "get",
      }).then(({ data }) => {
        console.log("要回显的数据：", data);
        this.category.name = data.data.name;
        this.category.catId = data.data.catId;
        this.category.icon = data.data.icon;
        this.category.productUnit = data.data.productUnit;
        this.category.parentCid = data.data.parentCid;
      });
    },
    // 修改三级分类
    editCategory() {
      console.log("editCategory:", this.category);
      this.dialogFormVisible = false;
      if (this.category.name == "" || this.category.name === "") {
        this.$message.error("菜单修改失败");
        return;
      }
      var { name, catId, icon, productUnit } = this.category;
      // var data = {name, catId, icon, productUnit}
      // 不想更新的数据不往出发！
      this.$http({
        url: this.$http.adornUrl("/product/category/update"),
        method: "post",
        // data: this.$http.adornData(data, false),
        data: this.$http.adornData({ name, catId, icon, productUnit }, false),
      }).then(({ data }) => {
        this.$message({
          message: "菜单修改成功",
          type: "success",
        });
        this.dialogFormVisible = false;
        // 刷新出新的菜单
        this.getMenus();
        // 设置需要默认展开的菜单
        this.expandedKey = [this.category.parentCid];
      });
    },

    append(data) {
      console.log("append:", data);
      // 重置this.category
      this.category = {
        name: "",
        parentCid: 0,
        catLevel: 0,
        showStatus: 1,
        sort: 0,
        catId: null, // catId是自增的
        icon: "",
        productUnit: "",
        productCount: 0,
      };

      this.dialogType = "append";
      this.title = "添加分类";
      this.dialogFormVisible = true;
      this.category.parentCid = data.catId;
      this.category.catLevel = data.catLevel * 1 + 1; // *1是为了防止是字符串
    },
    // 添加三级分类
    appendCategory() {
      console.log("appendCategory:", this.category);
      if (this.category.name == "" || this.category.name === "") {
        this.$message.error("菜单添加失败");
        return;
      }
      this.$http({
        url: this.$http.adornUrl("/product/category/save"),
        method: "post",
        data: this.$http.adornData(this.category, false),
      }).then(({ data }) => {
        this.$message({
          message: "菜单保存成功",
          type: "success",
        });
        this.dialogFormVisible = false;
        // 刷新出新的菜单
        this.getMenus();
        // 设置需要默认展开的菜单
        this.expandedKey = [this.category.parentCid];
      });
    },
    remove(node, data) {
      console.log("remove", node, data);

      this.$confirm(`是否删除当前菜单【${data.name}】?`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          let ids = [data.catId];
          this.$http({
            url: this.$http.adornUrl(`/product/category/delete`),
            method: "post",
            data: this.$http.adornData(ids, false),
          })
            .then(({ data }) => {
              if (data.code == 0) {
                this.$message({
                  message: "菜单删除成功",
                  type: "success",
                });
                // 刷新出新的菜单
                this.getMenus();
                // 设置需要默认展开的菜单
                this.expandedKey = [node.parent.data.catId];
              } else {
                this.$message.error("菜单删除失败");
              }
            })
            .catch(() => {
              this.$message.error("菜单删除失败");
            });
        })
        .catch(() => {
          this.$message({
            type: "info",
            message: "用户取消删除",
          });
        });
    },
  },
  created() {
    this.getMenus();
  },
};
</script>
<style>
</style>