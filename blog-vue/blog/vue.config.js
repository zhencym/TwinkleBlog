module.exports = {
  //nginx配置
  assetsDir: 'static', //静态文件的前一级目录，可以不设置
  parallel: false,
  publicPath: './',

  transpileDependencies: ["vuetify"],
  //devServer 就是vuecli在开发环境给我们提供的一个代理服务器
  devServer: {
    proxy: {
      "/api": {
        //target: "http://localhost:8666",
        target: "http://localhost:8777",
        //target: "http://43.139.142.79:8088",
        //target: "http://192.168.95.137:8666",
        //target: "http://43.139.142.79:8777",
        changeOrigin: true,
        pathRewrite: {
          "^/api": "" //把以/api开头的请求，除去/api，再拼接到target的请求地址，才发送请求
        }
      }
    },
    disableHostCheck: true
  }
};
