<template>
  <div>
    <!-- banner -->
    <div class="about-banner banner">
      <h1 class="banner-title">关于我</h1>
    </div>
    <!-- 关于我内容 -->
    <v-card class="blog-container">
      <div class="my-wrapper">
        <v-avatar size="110">
          <img
            class="author-avatar"
            src="https://cymoss.oss-cn-guangzhou.aliyuncs.com/articles/1606136286232535041.jpg"
          />
        </v-avatar>
      </div>
      <div class="about-content markdown-body" v-html="aboutContent" />
    </v-card>
  </div>
</template>

<script>
export default {
  created() {
    this.getAboutContent();
  },
  data: function() {
    return {
      aboutContent: ""
    };
  },
  methods: {
    getAboutContent() {
      this.axios.get("/api/about").then(({ data }) => {
        const MarkdownIt = require("markdown-it");
        const md = new MarkdownIt();
        this.aboutContent = md.render(data.data);
        // 用户提示
        if (!data.flag) {
          this.$toast({ type: "error", message: data.message });
        }
      });
    }
  }
};
</script>

<style scoped>
.about-banner {
  /*https://cymoss.oss-cn-guangzhou.aliyuncs.com/blog/cute521.jpg*/
  background: url(../../image/cute521.jpg) center
  center / cover no-repeat;
  /*background-size: 100% auto;    /*宽度%100，高度自适应*/
  background-size:cover;
  background-color: #49b1f5;
}
.about-content {
  word-break: break-word;
  line-height: 1.8;
  font-size: 14px;
}
.my-wrapper {
  text-align: center;
}
.author-avatar {
  transition: all 0.5s;
}
.author-avatar:hover {
  transform: rotate(360deg);
}
</style>
