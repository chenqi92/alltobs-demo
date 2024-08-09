## 这是配套alltobs-oss的使用说明文档

### minio测试环境配置

测试机使用docker安装了minio，其他腾讯云、阿里云等云服务商的对象存储服务只要支持S3协议的理论也可以使用，只需要修改配置文件即可。

```yml
version: '3'

services:
  minio:
    image: minio/minio
    container_name: minio
    restart: always
    ports:
      - "9000:9000"
      - "9001:9001" # 控制台端口
    environment:
      MINIO_ROOT_USER: "miniouser"      # 设置你的访问密钥
      MINIO_ROOT_PASSWORD: "123456789"  # 设置你的秘密密钥
    volumes:
      - /mnt/minio/files:/data                   # 将容器中的 /data 文件夹挂载到主机上的指定文件夹 /mnt/minio/files
    command: server /data --console-address ":9001"
```

### demo从环境配置

```yml
spring:
  application:
    name: alltobs-oss-demo
server:
  port: 7777

oss:
  # minio服务地址
  endpoint: http://192.168.0.120:9000
  # 账户名
  access-key: miniouser
  # 密钥
  secret-key: 123456789
  # 基础储存的文件夹，可以不设置，主要是为了不同项目文件储存在不同文件夹下
  bucket-name: test
  # 地区
  region: cn-north-1
  expiring-buckets:
    expire-bucket-1: 1 # 1天后文件自动删除

```
