# PayMap
PayMap是一个使用Java语言集成三方支付的小Demo，现已集成支付宝（国内、国际、移动端、PC端）、微信、银联（ACP、UPOP）、光大（网关、网页）、邮政支付，采用的技术栈为：SpringMVC+Spring+MyBatis+Shiro+RabbitMQ+Redis。

## 特性
- 支持前面提到的各种**支付
- 支付请求调用支持HTTP和异步MQ
- 控制层统一异常处理
- LogBack日志记录
- Redis缓存机制
- Shiro安全机制
- MyBatis代码自动生成
- HTTP请求日志记录
- RESTful APIs

## 说明

- 1、本文项目来自[Martin404](https://github.com/Martin404/PayMap),自己只是临摹大佬的项目。
- 2、重要的是学习过程，而不是结果。但，结果同样重要，加油。gogogo。
- 3、框架搭建就略过了。配置文件太多。遇到的时候贴出来。也收藏起来，留着备用。
- 4、Gist、[Insight.io for GitHub](https://chrome.google.com/webstore/detail/insightio-for-github/pmhfgjjhhomfplgmbalncpcohgeijonh)必备吧，[划词翻译](https://chrome.google.com/webstore/detail/ikhdkkncnoglghljlkmcimlnlhkeamad?utm_source=chrome-app-launcher-info-dialog)不懂的单词划一划。
- 在IDEA中我会注重代码规范，但是这里为了节约地方，能省的就省略了。谅解。


### 1、核心包～common

因为没有文档，只能根据自己之前的经验。先把必备的配置文件，包弄好。项目跑不起来，没关系。重要的是学习,先从核心包，common开始学习。配置文件就不贴了。需要的可以到[GitHub](https://github.com/guoxiaoxu)去找。

我们先整体看一下结构

![](https://i.imgur.com/y6XBk7r.jpg)

1、就先从异常开始吧，首先是定义，BaseException。为什么要这么定义呢？不知道大家有没有见过BaseDao、BaseAction。主要原因是为了方便扩展，父类不能实现的，在子类中加强。

回过头来我们在看下整体继承关系图，Throwable应该还有个Error错误。两者的区别在于后者是不可恢复的。这里的是运行时异常。 还有一种区分是受检查和非检查的。比如数据库连接关闭就是受检查异常，数组越界异常是非检查的。还有try{}catch{}finally{}这里不展开了。在Spring框架中可以受检查的包装成非受检查的。而且可明确提出错误信息。
![](https://i.imgur.com/RfSaAHA.jpg)

```java
public class BaseException extends RuntimeException {

    public BaseException(String message) {
        super(message,new Throwable(message));
    }

    public BaseException(Throwable cause) {
        super(cause);
    }
    public BaseException(String message,Throwable cause) {
        super(message,cause);
    }
}
```
2、接下来就是定义它的子类。

```java
/**
 * Created by guo on 3/2/2018.
 * 系统类异常
 */
public class SystemException extends BaseException {
    //实现和BaseException一样，构造方法名字换下。为了空间就不展示了。
}
---------------------------------------------------------
/**
 * 业务异常的自定义封装类
 */
public class BusinessException extends BaseException {
    //实现和BaseException一样，为了空间就不展示了。
}
---------------------------------------------------------
/**
 * 数据库异常
 */
public class DBException extends BaseException {

}
```
3、在看两个类，验证信息异常。后者估计大家用得着。我不会告诉你们Gist了。什么？你不懂？快去GItHub看看。收藏代码的好地方。
```java
/**
 * 验证异常,用于封装
 */
public class ValidationError {
    private String objectName;
    private String fieldName;
    private String defaultMessage;
  //  Constructor 、Setter 、Getter 、ToString 略。为了节约地方。
}

----------------------------------------------------------------
/**
 * Created by guo on 3/2/2018.
 * 异常返回码
 */
public enum ResultCode {
    /**
     * 成功. ErrorCode : 0
     */
    SUCCESS("0", "成功"),
    /**
     * 未知异常. ErrorCode : 01
     */
    UnknownException("01", "未知异常"),
    /**
     * 系统异常. ErrorCode : 02
     */
    SystemException("02", "系统异常"),
    /**
     * 业务错误. ErrorCode : 03
     */
    BusinessException("03", "业务错误"),
    /**
     * 提示级错误. ErrorCode : 04
     */
    InfoException("04", "提示级错误"),
    /**
     * 数据库操作异常. ErrorCode : 020001
     */
    DBException("020001", "数据库操作异常"),
    /**
     * 参数验证错误. ErrorCode : 040001
     */
    ParamException("040001", "参数验证错误"),

    SystemMaintainException("11", "系统正在维护");

    private String _code;
    private String _msg;
    //  Constructor、Getter略
    public static ResultCode getByCode(String code) {
        for (ResultCode ec : ResultCode.values()) {
            if (ec.getCode().equals(code)) {
                return ec;
            }
        }
        return null;
    }
}
```

4、接下来看一些默认设置。
```java
public class ActionConstants {
    /**
     * 默认值 - 执行时失败时ReturnContext的ReturnMsg
     */
    public static final String DEFAULT_FAILED_RETURNMSG = "执行失败";
    /**
     * 默认值key - 执行成功时ReturnContext的Returning
     */
    public static final String DEFAULT_SUCCESS_RETURNMSG ="执行成功";
}
-------------------------------------------------------------------------
/**
 * 从SpringApplicationContext中设置的系统参数
 */
public class SystemConfig {
    //系统默认的游客用户名
    private static String guestUsername = "";
    private SystemConfig() {}   //注意这里被私有化了。

    public static void setGuestUsername(String guestUsername) {
        SystemConfig.guestUsername = guestUsername;
    }
}
-------------------  -------注意bean------------------------------------
<bean id="systemConfig" class="com.guo.core.common.constant.SystemConfig">
    <property name="guestUsername">
        <value>${shiro.guest.username}</value>
    </property>
</bean>
```

5、这个也很重要，局部刷新。首先看下实现了Serializable，为什么呢？因为它要在网络中传输，所以需要序列成二进制格式的。还有当我们需要网络上的一个对象时，可以进行反序列化，在创建对象。或者想把内存中的对象保存在数据库中或者一个文件中。这里涉及ObjectOutputStream类的writeObject()方法、ObjectInputStream类的writeObject()方法。还有需要serialvUID，但不是必须的，最好加上。

```java
/**
 * AJAX调用返回对象
 */
public class AjaxResult implements Serializable {
    //请求结果是否为成功
    private String ErrorCode = ResultCode.SUCCESS.getCode();
    //请求返回信息
    private String Message = ActionConstants.DEFAULT_SUCCESS_RETURNMSG;
    //请求结果
    private Object Date = null;
   //Setter、Getter、toString....
    /**
     * 获取正确结果模板
     *                                     //标准是这样写的
     * @param message  请求返回信息
     * @param obj      请求结果
     * @return   AjaxResult
     */
    public static AjaxResult getOK(String message,Object obj) {
        AjaxResult result = new AjaxResult();
        result.setMessage(message);
        result.setDate(obj);
        return   result;
    }
    /**
     * 获取正确结果模板
     */
    public static AjaxResult getOK(Object obj) {
        AjaxResult result = new AjaxResult();
        result.setMessage(ActionConstants.DEFAULT_SUCCESS_RETURNMSG);
        result.setDate(obj);
        return  result;
    }
    /**
     * 获取正确结果模板
     */
    public static AjaxResult getOK() {
        return getOK(ActionConstants.DEFAULT_SUCCESS_RETURNMSG,null);
    }
    /**
     * 获取错误结果模板
     */
    public static AjaxResult getError(ResultCode errorCode,String message,Object obj) {
        AjaxResult result = new AjaxResult();
        result.setErrorCode(errorCode.getCode());
        result.setMessage(message);
        result.setDate(obj);
        return result;
    }
    /**
     * 获取错误结果模板
     *
     * @return AjaxResult
     */
    public static final AjaxResult getError(ResultCode resultCode) {
        AjaxResult result = new AjaxResult();
        return getError(resultCode,resultCode.getMsg(),null);
    }
}
```
### 2、核心包～util

1、接下来我们看工具类，这个几个非常有用。自己也收集了一些。先看日期吧，会拿出部分作为介绍。

(1)、首先我们看到DateUtils继承了ProperyEditorSuppert。为什么要这么做？看名字叫属性编辑支持。在Spring中我们可以使用属性编辑器来将特定的字符串转为对象：String-->Object.在JDK中用于将XML文件中字符串转为特定的类型，同时提供了一个实现类，是他是他就是PropertEditorSuppert。在Spring注入式，遇到类型不一致，则回去调用相应的属性编辑器进行转换。如：setAsText(String str)、setValue().不然getValue()方法拿不到处理后的对象。

```java
import org.joda.time.format.DateTimeFormat;      //注意包名，大佬的付出


public class DateUtils extends PropertyEditorSupport {
    public static final DateTimeFormatter standard = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter yy_MM_dd = DateTimeFormat.forPattern("yyyy-MM-dd");
    public static final DateTimeFormatter date_sdf_wz = DateTimeFormat.forPattern("yyyy年MM月dd日");
    //还有需要转换格式
    ...................................常用的，其实有很多.............................................
    /**
     * 字符串转为日期
     */
    public static Date str2Date(String str,DateTimeFormatter sdf) {
        if (str == null || "".equals(str)) {
            return null;
        }
        DateTime date;
        try {
            date = sdf.parseDateTime(str);
            return date.toDate();
        } catch (IllegalArgumentException e) {
          String errorMessage = "yyyy-MM-dd HH:mm:ss";
          if (sdf.equals(yy_MM_dd)) {
              errorMessage = "yyyy-MM-dd";
          }
          throw new BusinessException("输入的日期格式有误，因为'" + errorMessage + "'格式");  //异常直接给自定义的。
        }
    }
    /**
     * 指定日期的默认显示，具体格式为：年-月-日
     * @param date 指定的日期
     * @return 指定日期按“年-月-日”显示
     */
    public static String formatDate(Date date) {
        return standard.print(date.getTime());
    }
    /**
     * 返回UNIX时间戳-1970年至今的秒数(注意单位-不是毫秒)
     * @param str    the str
     * @param format the format
     * @return the long
     */
    public static long getUnixTimestamp(String str, DateTimeFormatter format) {
        DateTime dateTime = format.parseDateTime(str);
        return dateTime.getMillis() / 1000;
    }
    /**
     * 获取给定日期之间的日期
     */
    public static List<String> getRangeDates(Long startTime, Long endTime) {
        List<String> dateList = new ArrayList<>();
        DateTime startDt = new DateTime(startTime * 1000);
        DateTime endDt = new DateTime(endTime * 1000);
        endDt = endDt.withTimeAtStartOfDay();
        //因为查询结束时间不包含边界，特殊处理一下
        if (endTime * 1000 == endDt.getMillis()) {
            endDt = endDt.minusDays(1);
        }
        dateList.add(getTime(startTime, date_sdf_wz));
        while (endDt.isAfter(startDt)) {
            startDt = startDt.plusDays(1);
            dateList.add(getTime(startDt.getMillis() / 1000, date_sdf_wz));
        }
        return dateList;
    }
    /**
     * 返回时间间隔天数
     */
    public static int getDateDiff(Long beginTime, Long endTime) {
        DateTime dateTime1 = new DateTime(beginTime * 1000);
        DateTime dateTime2 = new DateTime(endTime * 1000);
        return Days.daysBetween(dateTime1, dateTime2).getDays();
    }
}
```

(2)接下来我们看下ID生产策略,注意静态代码块，还有这里用到了重入锁ReentrantLock()，对资源进行加锁,同一时刻只会有一个线程能够占有锁.当前锁被线程占有时,其他线程会进入挂起状态,直到该锁被释放,其他挂起的线程会被唤醒并开始新的竞争.,AtomicInteger并发条件下原子自增运算。保证一致性。[友情提示](https://www.jianshu.com/p/7d14f9dc5c1e)

```java
/**
 * ID生成策略
 */
public class IDGenerator {
    private static final DateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    private static final Random r = new Random();
    private static char[] A2Z = null;

    static {
        int j = 0;
        A2Z = new char[26];
        for (int i = 65; i < 91; i++) {
            A2Z[j] = (char)i;
            j++;
        }
    }
    public static String getTargetId() {
        char[] temp = new char[5];
        for (int i = 0; i < 5; i++) {
            temp[i] = A2Z[r.nextInt(26)];
        }
        String string = new String(temp);
        Integer max = 999999;
        Integer min = 10000;
        int s = r.nextInt(max) % (max - min + 1) + min;
        return string + s;

    }

    public static String getTranSid() {
        Lock lock = new ReentrantLock();
        lock.lock();
        String temp = null;
        AtomicInteger atomicInteger = new AtomicInteger();
        try {
            String currDate = format.format(new Date());
            Integer max = 999;
            Integer min = 100;
            int s = r.nextInt(max) % (max - min + 1) + min;
            temp = currDate + String.valueOf(s);

        } finally {
            lock.unlock();
        }
        return temp;
    }


    public static String getIcbcTimeStamp() {
        DateFormat dateFormatStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Lock lock = new ReentrantLock();
        lock.lock();
        String temp = null;
        AtomicInteger atomicInteger = new AtomicInteger();
        try {
            String currDate = dateFormatStamp.format(new Date());
            Integer max = 999999;
            Integer min = 100000;
            int s = r.nextInt(max) % (max - min + 1) + min;
            temp = currDate + "." + String.valueOf(s);
        } finally {
            lock.unlock();
        }
        return temp;
    }
}
```

(3)、看一个加载配置文件的工具类

需要注意的是这里同样用到了静态代码块，在启动的时候就加载。还有就是反射机制，在这里用的是类的加载器完成的。其他可以用getClass(),Claass.forName();

还有一点就是Properties类，继承自Hashtable，实现Map接口，可以和IO对象组合使用，实现数据的持久存储。存储键值对，根据key找Value。

```java
/**
 * Properties文件加载工具
 */
public class PropertiedsUtil {
    public static Properties properties = new Properties();
    public static List<String> configFile = Arrays.asList(
            "server_config.properties", "sys_config.properties");

    static {
        try {
            for (String fileName : configFile) {
                InputStream in = PropertiedsUtil.class.getClassLoader().getResourceAsStream(fileName);
                properties.load(in);
            }
        }catch (IOException e) {
            throw new BusinessException("读取配置文件错误");
        }
    }
    public static String getValue(String key) {
        return properties.getProperty(key, "");
    }
}

```
(4)、SqlSessionFactoryBean工具类
在 MyBatis 中，使用 SqlSessionFactoryBuilder创建SqlSessionFactory ，进而来创建 SqlSession。一旦你获得一个 session 之后,你可以使用它来执行映射语句,提交或回滚连接,最后,当不再需要它的时候, 你可以关闭 session。
```java
/**
 * Created by guo on 3/2/2018.
 * SqlSession工厂bean
 */
public class SqlSessionFactoryBeanUtil extends SqlSessionFactoryBean {
    //这里使用了日志记录，方便查看。
    private static Logger logger = LoggerFactory.getLogger(SqlSessionFactoryBeanUtil.class);

    @Override
    protected SqlSessionFactory buildSqlSessionFactory() throws IOException {
        try {
            return super.buildSqlSessionFactory();    //调用父类
        }catch (NestedIOException e) {
            logger.error("ex:{}",e.getMessage());
            throw new NestedIOException("Faild to parse mapping resource: " + e);
        }finally {
            ErrorContext.instance().reset();
        }
    }
}
```
(5)、最重要的一般压低出现。那就是字符串工具类。这个有些多，挑常用的罗列出来。

```java
/**
 * Created by guo on 3/2/2018.
 * 字符串处理及转换工具类
 */
public class StringUtil {
    private static Pattern numericPattern = Pattern.compile("^[0-9\\-]+$");
    private static Pattern numericStringPattern = Pattern.compile("^[0-9\\-\\-]+$");
    private static Pattern floatNumericPattern = Pattern.compile("^[0-9\\-\\.]+$");
    private static Pattern abcPattern = Pattern.compile("^[a-z|A-Z]+$");
    public static final String splitStrPattern = ",|，|;|；|、|\\.|。|-|_|\\(|\\)|\\[|\\]|\\{|\\}|\\\\|/| |　|\"";
    private static Logger logger = LoggerFactory.getLogger(StringUtil.class);

    /**
     * 判断是否数字表示
     * @param src 源字符串
     * @return 是否数字的标志
     */
    public static boolean isNumeric(String src) {
        boolean return_value = false;
        if (src != null && src.length() > 0) {
            Matcher m = numericPattern.matcher(src);
            if (m.find()) {
                return_value = true;
            }
        }
        return return_value;
    }
    /**
     * 判断是否纯字母组合
     * @param src 源字符串
     * @return 是否纯字母组合的标志
     */
    public static boolean isABC(String src) {
        boolean return_value = false;
        if (src != null && src.length() > 0) {
            Matcher m = abcPattern.matcher(src);
            if (m.find()) {
                return_value = true;
            }
        }
        return return_value;
    }

    /**
     * 判断是否浮点数字表示
     */
    public static boolean isFloatNumeric(String src) {}
-------------------------------截取------------------------------------------------------------
    /**
     * 把string array or list用给定的符号symbol连接成一个字符串
     */
    public static String joinString(List array, String symbol) {
        String result = "";
        if (array != null) {
            for (int i = 0; i < array.size(); i++) {
                String temp = array.get(i).toString();
                if (temp != null && temp.trim().length() > 0)
                    result += (temp + symbol);
            }
            if (result.length() > 1)
                result = result.substring(0, result.length() - 1);
        }
        return result;
    }
    /**
     * 截取字符，不转码
     */
    public static String subStrNotEncode(String subject, int size) {
        if (subject.length() > size) {
            subject = subject.substring(0, size);
        }
        return subject;
    }
    /**
        * 取得字符串的实际长度（考虑了汉字的情况）
        */
       public static int getStringLen(String SrcStr) {
           int return_value = 0;
           if (SrcStr != null) {
               char[] theChars = SrcStr.toCharArray();
               for (int i = 0; i < theChars.length; i++) {
                   return_value += (theChars[i] <= 255) ? 1 : 2;
               }
           }
           return return_value;
       }
---------------------------------分割、替换和转换-------------------------------------------
       /**
        * 根据指定的字符把源字符串分割成一个数组
        */
       public static List<String> parseString2ListByCustomerPattern(String pattern, String src) {
           if (src == null)
               return null;
           List<String> list = new ArrayList<String>();
           String[] result = src.split(pattern);
           for (int i = 0; i < result.length; i++) {
               list.add(result[i]);
           }
           return list;
       }
       /**
        * 字符串替换
        */
       public static String stringReplace(String str, String sr, String sd) {
           String regEx = sr;
           Pattern p = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
           Matcher m = p.matcher(str);
           str = m.replaceAll(sd);
           return str;
       }
       /**
        * 格式化一个float
        */
       public static String formatFloat(float f, String format) {
           DecimalFormat df = new DecimalFormat(format);
           return df.format(f);
       }
       /**
        * 把 名=值 参数表转换成字符串 (a=1,b=2 =>a=1&b=2)
        */
       public static String linkedHashMapToString(LinkedHashMap<String, String> map) {
           if (map != null && map.size() > 0) {
               String result = "";
               Iterator it = map.keySet().iterator();
               while (it.hasNext()) {
                   String name = (String) it.next();
                   String value = (String) map.get(name);
                   result += (result.equals("")) ? "" : "&";
                   result += String.format("%s=%s", name, value);
               }
               return result;
           }
           return null;
       }
       /**
        * 转换编码
        */
       public static String changCoding(String s, String fencode, String bencode) {
           String str;
           try {
               if (StringUtil.isNotEmpty(s)) {
                   str = new String(s.getBytes(fencode), bencode);
               } else {
                   str = "";
               }
               return str;
           } catch (UnsupportedEncodingException e) {
               return s;
           }
       }

```
下面的是转换，具体的用到了再说。
```java
/**
 * 将字符串转换成十六进制编码
 */
public static String toHexString(String str) throws UnsupportedEncodingException {
    // 根据默认编码获取字节数组
    String hexString = "0123456789ABCDEF";
    byte[] bytes = str.getBytes("GB2312");
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    // 将字节数组中每个字节拆解成2位16进制整数
    for (byte b : bytes) {
        sb.append(Integer.toHexString(b + 0x800).substring(1));
    }
    return sb.toString();
}
/**
 * unicode 转字符串
 */
public static String unicode2String(String unicode) {
    StringBuffer string = new StringBuffer();
    String[] hex = unicode.split("\\\\u");
    for (int i = 1; i < hex.length; i++) {
        // 转换出每一个代码点
        int data = Integer.parseInt(hex[i], 16);
        // 追加成string
        string.append((char) data);
    }
    return string.toString();
}
```

gogogo...
