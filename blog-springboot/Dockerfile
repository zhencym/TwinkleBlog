FROM java:8
EXPOSE 8666

# 声明作者是cym
MAINTAINER cym

# 把docker的logs的调整到和服务器时间相同
ENV TZ=Asia/Shanghai
RUN ln -sf /usr/share/zoneinfo/{TZ} /etc/localtime && echo "{TZ}" > /etc/timezone

ADD blog.jar /blog.jar
RUN bash -c 'touch /blog.jar'
ENTRYPOINT ["java","-jar","/blog.jar"]