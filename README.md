# 智谱Ai大模型开放SDK - By 小傅哥版本

为了让研发伙伴更快，更方便的接入使用智谱Ai大模型。从而开发的 chatglm-sdk-java 也欢迎👏🏻大家基于智谱API接口补充需要的功能。

此SDK设计，以 Session 会话模型，提供工厂🏭创建服务。代码非常清晰，易于扩展、易于维护。你的PR/ISSUE贡献💐会让AI更加璀璨，[感谢智谱AI团队](https://www.zhipuai.cn/)。

---

>**作者**：小傅哥 - 个人博客 [**bugstack.cn**](https://bugstack.cn/)，互联网大厂架构师，《重学Java设计模式》、《手写MyBatis：渐进式源码实践》图书作者。`欢迎百度搜索：小傅哥bugstack`

## 👣目录

1. 组件配置
2. 功能测试
   1. 代码执行 - `使用：代码的方式主要用于程序接入`
   2. 脚本测试 - `测试：生成Token，直接通过HTTP访问Ai服务`
3. 程序接入

## 1. 组件配置

- 申请ApiKey：[https://open.bigmodel.cn/usercenter/apikeys](https://open.bigmodel.cn/usercenter/apikeys) - 注册申请开通，即可获得 ApiKey
- 运行环境：JDK 1.8+
- maven pom - `暂时测试阶段，未推送到Maven中央仓库，需要下载代码本地 install 后使用`

```pom
<dependency>
    <groupId>cn.bugstack</groupId>
    <artifactId>chatglm-sdk-java</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

- 源码(Github)：[https://github.com/fuzhengwei/chatglm-sdk-java](https://github.com/fuzhengwei/chatglm-sdk-java)
- 源码(Gitee)：[https://gitee.com/fustack/chatglm-sdk-java](https://gitee.com/fustack/chatglm-sdk-java)
- 源码(Gitcode)：[https://gitcode.net/KnowledgePlanet/road-map/chatglm-sdk-java](https://gitcode.net/KnowledgePlanet/road-map/chatglm-sdk-java)

## 2. 功能测试

### 2.1 代码执行

```java
@Slf4j
public class ApiTest {

    private OpenAiSession openAiSession;

    @Before
    public void test_OpenAiSessionFactory() {
        // 1. 配置文件
        Configuration configuration = new Configuration();
        configuration.setApiHost("https://open.bigmodel.cn/");
        configuration.setApiSecretKey("4e087e4135306ef4a676f0cce3cee560.sgP2*****");
        // 2. 会话工厂
        OpenAiSessionFactory factory = new DefaultOpenAiSessionFactory(configuration);
        // 3. 开启会话
        this.openAiSession = factory.openSession();
    }

    /**
     * 流式对话
     */
    @Test
    public void test_completions() throws JsonProcessingException, InterruptedException {
        // 入参；模型、请求信息
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel(Model.CHATGLM_LITE); // chatGLM_6b_SSE、chatglm_lite、chatglm_lite_32k、chatglm_std、chatglm_pro
        request.setPrompt(new ArrayList<ChatCompletionRequest.Prompt>() {
            private static final long serialVersionUID = -7988151926241837899L;

            {
                add(ChatCompletionRequest.Prompt.builder()
                        .role(Role.user.getCode())
                        .content("写个java冒泡排序")
                        .build());
            }
        });

        // 请求
        openAiSession.completions(request, new EventSourceListener() {
            @Override
            public void onEvent(EventSource eventSource, @Nullable String id, @Nullable String type, String data) {
                ChatCompletionResponse response = JSON.parseObject(data, ChatCompletionResponse.class);
                log.info("测试结果 onEvent：{}", response.getData());
                // type 消息类型，add 增量，finish 结束，error 错误，interrupted 中断
                if (EventType.finish.getCode().equals(type)) {
                    ChatCompletionResponse.Meta meta = JSON.parseObject(response.getMeta(), ChatCompletionResponse.Meta.class);
                    log.info("[输出结束] Tokens {}", JSON.toJSONString(meta));
                }
            }

            @Override
            public void onClosed(EventSource eventSource) {
                log.info("对话完成");
            }
        });

        // 等待
        new CountDownLatch(1).await();
    }

}
```

- 这是一个单元测试类，也是最常使用的流式对话模式。

### 2.2 脚本测试

```java
@Test
public void test_curl() {
    // 1. 配置文件
    Configuration configuration = new Configuration();
    configuration.setApiHost("https://open.bigmodel.cn/");
    configuration.setApiSecretKey("4e087e4135306ef4a676f0cce3cee560.sgP2D****");
    // 2. 获取Token
    String token = BearerTokenUtils.getToken(configuration.getApiKey(), configuration.getApiSecret());
    log.info("1. 在智谱Ai官网，申请 ApiSeretKey 配置到此测试类中，替换 setApiSecretKey 值。 https://open.bigmodel.cn/usercenter/apikeys");
    log.info("2. 运行 test_curl 获取 token：{}", token);
    log.info("3. 将获得的 token 值，复制到 curl.sh 中，填写到 Authorization: Bearer 后面");
    log.info("4. 执行完步骤3以后，可以复制直接运行 curl.sh 文件，或者复制 curl.sh 文件内容到控制台/终端/ApiPost中运行");
}
```

```java
curl -X POST \
        -H "Authorization: Bearer <把获得的Token填写这，并去掉两个尖括号>" \
        -H "Content-Type: application/json" \
        -H "User-Agent: Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)" \
        -H "Accept: text/event-stream" \
        -d '{
        "top_p": 0.7,
        "sseFormat": "data",
        "temperature": 0.9,
        "incremental": true,
        "request_id": "xfg-1696992276607",
        "prompt": [
        {
        "role": "user",
        "content": "写个java冒泡排序"
        }
        ]
        }' \
  http://open.bigmodel.cn/api/paas/v3/model-api/chatglm_lite/sse-invoke
```

- 运行后你会获得一个 Token 信息，之后在 curl.sh 中替换  Authorization: Bearer 后面的值。就可以执行测试了。

## 3. 程序接入

SpringBoot 配置类

```java
@Configuration
@EnableConfigurationProperties(ChatGLMSDKConfigProperties.class)
public class ChatGLMSDKConfig {

    @Bean
    @ConditionalOnProperty(value = "chatglm.sdk.config.enabled", havingValue = "true", matchIfMissing = false)
    public OpenAiSession openAiSession(ChatGLMSDKConfigProperties properties) {
        // 1. 配置文件
        cn.bugstack.chatglm.session.Configuration configuration = new cn.bugstack.chatglm.session.Configuration();
        configuration.setApiHost(properties.getApiHost());
        configuration.setApiSecretKey(properties.getApiSecretKey());

        // 2. 会话工厂
        OpenAiSessionFactory factory = new DefaultOpenAiSessionFactory(configuration);

        // 3. 开启会话
        return factory.openSession();
    }

}

@Data
@ConfigurationProperties(prefix = "chatglm.sdk.config", ignoreInvalidFields = true)
public class ChatGLMSDKConfigProperties {

    /** 状态；open = 开启、close 关闭 */
    private boolean enable;
    /** 转发地址 */
    private String apiHost;
    /** 可以申请 sk-*** */
    private String apiSecretKey;

}
```

```java
@Autowired(required = false)
private OpenAiSession openAiSession;
```

- 注意：如果你在服务中配置了关闭启动 ChatGLM SDK 那么注入 openAiSession 为 null

yml 配置

```pom
# ChatGLM SDK Config
chatglm:
  sdk:
    config:
      # 状态；true = 开启、false 关闭
      enabled: false
      # 官网地址 
      api-host: https://open.bigmodel.cn/
      # 官网申请 https://open.bigmodel.cn/usercenter/apikeys
      api-secret-key: 4e087e4135306ef4a676f0cce3cee560.sgP2DUs*****
```

---

- 👨‍💻 成长：[关于我，从小白到架构师的成长经历](https://www.bilibili.com/video/BV1FF41137q5)
- 🚌 作品：[`CodeGuide | 程序员编码指南`](https://github.com/fuzhengwei/CodeGuide) | [`RoadMap 编程路书`](https://github.com/fuzhengwei/RoadMap) | [`Java 数据结构和算法`](https://github.com/fuzhengwei/java-algorithms) | [`IM 仿微信`](https://github.com/fuzhengwei/NaiveChat) | [`Java 面经手册`](https://github.com/fuzhengwei/interview) | [`IntelliJ IDEA 插件开发`](https://github.com/fuzhengwei/guide-idea-plugin) | [`Lottery 抽奖系统 - 基于领域驱动设计的四层架构实践`](https://github.com/fuzhengwei/Lottery) | [`API网关`](https://github.com/fuzhengwei/api-gateway) | [`手写MyBatis`](https://github.com/fuzhengwei/small-mybatis) | [`重学Java设计模式`](https://github.com/fuzhengwei/itstack-demo-design) | [`Netty 实战案例`](https://github.com/fuzhengwei/itstack-demo-netty) | [`字节码编程`](https://github.com/fuzhengwei/itstack-demo-bytecode) | [`ChatGPT AI 问答助手`](https://github.com/fuzhengwei/chatbot-api) | [更多搜索...](https://github.com/fuzhengwei?tab=repositories)
- 🌱 干货：[公众号『 bugstack虫洞栈 』](https://bugstack.cn/images/personal/qrcode.png)
- 📝 博客：[bugstack.cn](https://bugstack.cn/) - 足够硬核，内容老狠了！
- 📺 视频：[B站 小傅哥の码场](https://space.bilibili.com/15637440)
- 💌 微信：[fustack](https://bugstack.cn/images/personal/fustack.png) - 备注来意
- 🐾 我的编程知识星球：[实战生产级项目、手写框架级源码，可以向我 1对1 提问，解答技术/职场/规划问题](https://bugstack.cn/md/zsxq/introduce.html)