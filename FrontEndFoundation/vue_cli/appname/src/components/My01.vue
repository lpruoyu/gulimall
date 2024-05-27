<template>
  <div class="hello">
    <router-link to="/">去首页</router-link>
    <h1>{{ msg }}</h1>
    <h2>my01</h2>

    <el-radio v-model="radio" label="1">备选项</el-radio>
    <el-radio v-model="radio" label="2">备选项</el-radio>
  </div>
</template>

<script>
export default {
  data() {
    return {
      msg: "LP",
      radio: "1"
    };
  },
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
h1 {
  color: #b9424e;
}
h2 {
  color: #bdde39;
}
</style>
