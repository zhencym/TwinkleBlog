module.exports = {
  //部署到服务器配置
  assetsDir: 'static',
  parallel: false,
  publicPath: './',

  devServer: {
    proxy: {
      "/api": {
        //target: "http://localhost:8666",
        //target: "http://43.139.142.79:8088",
        //target: "http://192.168.95.137:8666",
        target: "http://43.139.142.79:8777",
        changeOrigin: true,
        pathRewrite: {
          "^/api": ""
        }
      }
    },
    disableHostCheck: true
  },
  chainWebpack: config => {
    config.resolve.alias.set("@", resolve("src"));
  }
};

const path = require("path");
function resolve(dir) {
  return path.join(__dirname, dir);
}
