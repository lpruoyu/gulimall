import Vue from 'vue'
import Router from 'vue-router'
import HelloWorld from '@/components/HelloWorld'
import My01 from '@/components/My01'
import MyTable from '@/components/MyTable'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'HelloWorld',
      component: HelloWorld
    },
    {
      path: '/my01',
      name: 'My01',
      component: My01
    },
    {
      path: '/mytable',
      name: 'MyTable',
      component: MyTable
    }
  ]
})
