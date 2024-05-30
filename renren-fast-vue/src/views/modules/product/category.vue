<template>
  <div>
    <el-tree
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
    <el-dialog title="添加菜单" :visible.sync="dialogFormVisible" width="30%">
      <el-form :model="category">
        <el-form-item label="分类名称">
          <el-input v-model="category.name" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitCategory">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      category: { name: "", parentCid: 0, catLevel: 0, showStatus: 1, sort: 0 },
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
    getMenus() {
      this.$http({
        url: this.$http.adornUrl("/product/category/list/tree"),
        method: "get",
      }).then(({ data }) => {
        // console.log(data.data);
        this.menus = data.data;
      });
    },
    append(data) {
      console.log("append:", data);
      this.dialogFormVisible = true;
      this.category.parentCid = data.catId;
      this.category.catLevel = data.catLevel * 1 + 1; // *1是为了防止是字符串
    },
    submitCategory() {
      // console.log(this.category);
      this.dialogFormVisible = false;
      this.$http({
        url: this.$http.adornUrl("/product/category/save"),
        method: "post",
        data: this.$http.adornData(this.category, false),
      }).then(({ data }) => {
        this.$message({
          message: "菜单保存成功",
          type: "success",
        });
        // 刷新出新的菜单
        this.getMenus();
        // 设置需要默认展开的菜单
        this.expandedKey = [this.category.parentCid];
        // 初始化this.category
        this.category = {
          name: "",
          parentCid: 0,
          catLevel: 0,
          showStatus: 1,
          sort: 0,
        };
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