

## 视频演示

- [哔哩哔哩](https://space.bilibili.com/1765486559/channel/seriesdetail?sid=2359281)


## 配置方法

- 修改本地数据库连接地址、账号、密码等信息：`mybatis-config.xml`
- 修改要查询的数据表名、输出生成文件目录、根路径等等信息：`generator.properties`
- 定制 Controller 类模板信息：`YapiController.java.vm`
- 定制 RequestParam 类模板信息：`ReadYapiJsonToJavaClassMain.java`
- 定制 ResponseDTO 类模板信息：`ReadYapiJsonToJavaClassMain.java`
- 说明：在 velocity 中不好处理递归，所以要生成支持各种子属性的 RequestParam、ResponseDTO 就采用字符串拼接处理，生成文件后自己进行样式格式化，方便查看


## 使用方法

- 查询数据库表生成 YApi JSON 文件触发 Main 方法在这里：`QueryDbTableToYapiJsonMain.java`
- 读取本地 YApi JSON 文件生成 Java Class 触发 Main 方法在这里：`ReadYapiJsonToJavaClassMain.java`
