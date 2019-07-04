# 自定义 PropertySource
## 背景
在 `Springboot` 的配置中大家肯定见过以下代码吧，生成指定类型的随机值，感觉使用很简洁；于是萌生了想自己实现类似的功能。恰好手边有个类似的需求，需要产生指定长度的随机字符串。
虽然 `${random.uuid}` 可以产生一个 `32` 位的小写字母和数字的字符串，但是还想扩展含有大写字母且长度可自定义。

```yml
value1: "${random.int}"
value2: "${random.long(100,200)}"
value3: "${random.uuid}"
```

## 源码查看
简单查看一下 `RandomValuePropertySource` 源码是怎么实现的

```java
public class RandomValuePropertySource extends PropertySource<Random> {

	/**
	 * Name of the random {@link PropertySource}.
	 */
	public static final String RANDOM_PROPERTY_SOURCE_NAME = "random";
    //定义前缀
	private static final String PREFIX = "random.";

	private static final Log logger = LogFactory.getLog(RandomValuePropertySource.class);
    //构造函数初始了一个 Random 对象
	public RandomValuePropertySource(String name) {
		super(name, new Random());
	}
    //默认无参的构造函数
	public RandomValuePropertySource() {
		this(RANDOM_PROPERTY_SOURCE_NAME);
	}
    //最核心的方法，以 random 为前缀配置为例，这里的 name 就是 random. 后面的值，比如int、int(10)
	@Override
	public Object getProperty(String name) {
        //不是指定的前缀直接返回空
		if (!name.startsWith(PREFIX)) {
			return null;
		}
		if (logger.isTraceEnabled()) {
			logger.trace("Generating random property for '" + name + "'");
		}
        //调用方法处理各种各种类型的随机值
		return getRandomValue(name.substring(PREFIX.length()));
	}

	private Object getRandomValue(String type) {
        //处理默认int随机值
		if (type.equals("int")) {
			return getSource().nextInt();
		}
        //处理默认long随机值
		if (type.equals("long")) {
			return getSource().nextLong();
		}
        //处理默认指定大小范围int随机值，比如int(10),int(10,100)
		String range = getRange(type, "int");
		if (range != null) {
			return getNextIntInRange(range);
		}
		range = getRange(type, "long");
		if (range != null) {
			return getNextLongInRange(range);
		}
        //生成uuid
		if (type.equals("uuid")) {
			return UUID.randomUUID().toString();
		}
		return getRandomBytes();
	}
    //解析各种类型的范围
	private String getRange(String type, String prefix) {
		if (type.startsWith(prefix)) {
			int startIndex = prefix.length() + 1;
			if (type.length() > startIndex) {
				return type.substring(startIndex, type.length() - 1);
			}
		}
		return null;
	}
    //int指定范围随机值的具体处理方法
	private int getNextIntInRange(String range) {
		String[] tokens = StringUtils.commaDelimitedListToStringArray(range);
		int start = Integer.parseInt(tokens[0]);
		if (tokens.length == 1) {
			return getSource().nextInt(start);
		}
		return start + getSource().nextInt(Integer.parseInt(tokens[1]) - start);
	}
    //long指定范围随机值的具体处理方法
	private long getNextLongInRange(String range) {
		String[] tokens = StringUtils.commaDelimitedListToStringArray(range);
		if (tokens.length == 1) {
			return Math.abs(getSource().nextLong() % Long.parseLong(tokens[0]));
		}
		long lowerBound = Long.parseLong(tokens[0]);
		long upperBound = Long.parseLong(tokens[1]) - lowerBound;
		return lowerBound + Math.abs(getSource().nextLong() % upperBound);
	}

	private Object getRandomBytes() {
		byte[] bytes = new byte[32];
		getSource().nextBytes(bytes);
		return DigestUtils.md5DigestAsHex(bytes);
	}
    //添加到当前的环境，实际自定义的时候可能不会用到此方法
	public static void addToEnvironment(ConfigurableEnvironment environment) {
		environment.getPropertySources().addAfter(
				StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
				new RandomValuePropertySource(RANDOM_PROPERTY_SOURCE_NAME));
		logger.trace("RandomValuePropertySource add to Environment");
	}
}
```

## 自定义
先看看我们自定义的使用方法：

```yml
value1: ${randomKey.key}
value2: ${randomKey.key(128)}
```

以上源码也比较简单，搞清楚大概功能后，我们照葫芦画瓢自定义我们如下：

```java
public class RandomKeyPropertySource extends PropertySource<Random> {
    public static final String RANDOM_PROPERTY_SOURCE_NAME = "randomKey";

    private static final String PREFIX = "randomKey.";

    private static final Log logger = LogFactory.getLog(RandomKeyPropertySource.class);
    //生成长度小于字符串的随机数字从而生成随机字符串
    private static String CONSTANT_STRING = "0123456789abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public RandomKeyPropertySource(String name) {
        super(name, new Random());
    }

    public RandomKeyPropertySource(){
        this(RANDOM_PROPERTY_SOURCE_NAME);
    }

    @Override
    public Object getProperty(String name) {
        if (!name.startsWith(PREFIX)) {
            return null;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Generating random property for '" + name + "'");
        }
        return getRandomValue(name.substring(PREFIX.length()));
    }
    private String getRandomValue(String type) {
        //默认生成64位的随机字符串
        if (type.equals("key")) {
            return randomString(64);
        }
        //指定了字符串长度，生成指定长度字符串
        String range = getRange(type, "key");
        if (range != null) {
            return getNextKeyRange(range);
        }
        return null;
    }
    //拷贝源码方法
    private String getRange(String type, String prefix) {
        if (type.startsWith(prefix)) {
            int startIndex = prefix.length() + 1;
            if (type.length() > startIndex) {
                return type.substring(startIndex, type.length() - 1);
            }
        }
        return null;
    }
    //做了简单修改，实际上只能范围只能指定一个固定的值，而不能是一个区间
    private String getNextKeyRange(String range) {
        String[] tokens = StringUtils.commaDelimitedListToStringArray(range);
        int start = Integer.parseInt(tokens[0]);
        if (tokens.length == 1) {
            return randomString(start);
        }
        return null;
    }
    //实际生成随机字符串的方法
    private String randomString(int length){
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < length; i++){
            int number = getSource().nextInt(CONSTANT_STRING.length());
            sb.append(CONSTANT_STRING.charAt(number));
        }
        return sb.toString();
    }
    //这个方法应该没用，也照葫芦画瓢保留了
    public static void addToEnvironment(ConfigurableEnvironment environment) {
        environment.getPropertySources().addAfter(
                StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                new RandomValuePropertySource(RANDOM_PROPERTY_SOURCE_NAME));
        logger.trace("RandomValuePropertySource add to Environment");
    }
}
```

## 配置加载

```java
@Configuration
public class PropertySourceConfig {
    @Autowired
    private ConfigurableEnvironment env;

    @PostConstruct
    public void init() throws Exception {
        env.getPropertySources().addFirst(new RandomKeyPropertySource());
    }
}
```

## 使用
在 `application.yml` 中直接使用就可以了

```
testKey: ${randomKey.key}
```

### Demo
[Demo Github](https://github.com/zealzhangz/custom-properties-random-key)

### 实际运行结果
在浏览器访问：`http://127.0.0.1:8080/test`

```json
{
    "value2": "fFlgx90qS58F6ljaP5WY4p4F8hdq71396SJiY5WwC0103906PiiQbKBl13tt8an906T082Mrw5177cH04hyB80168leY9ScYa4E8Jo5j519xakXAjlmWTI2o9K49FBd9",
    "value1": "DP7eg8i27633zvNxzFUvD3hzum5LDJJPY3p77vHV28II2Y1S090dSRbK2S57EcmF"
}
```

# 注意事项
我本次使用的场景是微服务的场景，本来是想在 `Spring Config Server` 模块生成一个统一的 `JWT Token` 签名的密钥，但是发现实际上在 `Spring Config Server` 定义改功能后，各个子模块其实也是取不到值的，必须把这个功能分别配置在各个模块，各个模块实际使用的时候各自调用生成各自的随机值。

因此这也我的需求不符，无论如何也 `Get` 到了一个新技能