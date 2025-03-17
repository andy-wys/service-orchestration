## 《服务编排框架 Service Orchestration Framework》

> 项目地址：https://github.com/andy-wys/service-orchestration
>
> 作者：Andy Wang 



## 概述

> 在当前互联网环境中，微服务架构早已成为主流，但在实际业务中，服务间调用的接口规范和数据格式不统一，导致接口的调用转换工作浪费了很多的开发时间。开发这个框架的初衷，就是为了将不同标准的服务接口转换为标准输入输出，减少代码开发，快速响应业务需求；

> **核心功能：**
>
> 1. 服务编排，数据映射，数据转换；
> 2. 配置化方式定义和实现标准输入输出，减少编码开发；
> 3. 支持协议：HTTP (GET / POST)、RPC、local 本地代码调用、ref 服务配置文件引用、redirect 重定向等；



## XML 标签说明

> **标签层级说明：**
>
> -- merchant 标签，定义一个标准 HTTP 接口
>
> 	-- description 标签，当前接口的说明信息，无实际作用
>	
> 	-- api 标签，调用下游提供的 HTTP 接口
>	
> 		-- description 标签，为当前 api 标签作说明信息，无实际作用
>	
> 		-- req 标签，请求参数映射
>	
>    	     -- property 标签，为下游请求的接口设置报文值
>	
> 			-- header 标签，为下游请求的接口设置 header 值
>	
> 			-- cookie 标签，为下游请求的接口设置 cookie 值
>	
> 		-- resp 标签，响应报文映射
>	
>    	     -- property 标签，给上游调用方设置输出报文值
>	
> 			-- header 标签，给上游调用方请求设置 header 值
>	
> 			-- cookie 标签，给上游调用方请求设置 cookie 值
>	
> 	-- rpc 标签，调用下游提供的 RPC 接口
>	
> 		-- req 标签，定义 RPC 入参
>	
>    	     -- property 标签，定义数据转换，在 RPC 标签下可以作为数据编排使用
>	
>    	     -- param 标签，定义 RPC 方法的参数
>	
> 		-- resp 标签，定义 RPC 出参转换
>	
> 	-- local 标签，调用本地的代码方法，**本地实现类需要注入 spring 容器**
>	
> 		-- req 标签，定义本地方法入参
>	
>    	     -- property 标签，定义数据转换，可以作为数据编排使用
>	
>    	     -- param 标签，定义本地方法的参数
>	
> 		-- resp 标签，定义本地方法出参转换
>	
> 	-- redirect 标签，重定向
>	
> 		-- req 标签，定义重定向携带参数
>	
>    	     -- property 标签，参数
>	
> 	-- ref 标签，引用配置的服务，xml 服务会作为一个整体执行
>	
> 		-- req 标签，定义入参
>	
>    	     -- property 标签，定义入参
>	
> 		-- resp 标签，定义出参转换
>
> -- mock 标签，为服务节点配置请求或响应 mock 数据



### merchant 标签

```xml
<so:merchant 
    code="商户编码" 
    name="接口名称" 
    requestPath="标准接口请求路径" 
    reqDataHandle="标准接口请求报文处理器" 
    respDataHandle="标准接口响应报文处理器"
    regRequestMapping="将该接口注册到 request mapping 中，可供外部直接调用，默认为 TRUE"
    id="指定当前服务的唯一 ID，如果其他编排节点需要引用当前节点数据，则需要通过当前 ID 才能找到本节点">
</so:merchant>
```



**merchant 定义一个标准接口，该接口的定义信息为：**

- **requestPath：**[必填项] 标准接口的请求路径，该值不允许重复；

- **code：**[选填项] 商户的编码，默认为 *，即所有商户可调用；
- **name：**[选填项] 商户的名称，作为一个商户的标识；
- **reqDataHandle：**[选填项] 对标准请求报文做处理，该处理器会优先执行，可以是多个，用英文逗号(",")分隔；
- **respDataHandle：**[选填项] 对标准输出报文做处理，该处理器会在最后执行，可以是多个，用英文逗号(",")分隔；
- **regRequestMapping：**[选填项] 将该接口注册到 request mapping 中，可供外部直接调用，默认为 TRUE；
- **id：**[选填项] 指定当前服务的唯一 ID，如果其他编排节点需要引用当前节点数据，则需要通过当前 ID 才能找到本节点；



### mock 标签

> 定义 mock 数据，在任意服务节点均可使用；
> 
> mock 总开关配置 so.merchant.mock-enable=true，默认 false（不开启）；
>
> mock 数据文件目录配置 so.merchant.mock-data-dir，默认 classpath:api-mock/；
>


**mock 的标签属性：**

- **enable：**[选填项] 当前 mock 节点是否开启，只有在 so.merchant.mock-enable=true 时才会生效；
- **type：**[选填项] 指定当前是 mock 请求数据（REQ）还是响应数据（RESP）；
- **file：**[选填项] 指定 mock 数据的 JSON 文件，文件包含内容 data、header、cookie、condition 四个对象；


**mock 数据文件示例：**
```
{
	"data": {mock 数据},
    "header": {mock http header},
    "cookie": {mock http cookie},
    "condition": true / false, mock condition 标签配置
}
```



### api 标签

> 定义调用的下游服务的 http 接口信息，该标签只能在 merchant 下使用；

**api 的标签属性：**

- **id：**[选填项] 该接口的身份标识，如果该接口要被其他字段引用，则需要填写 ID；
- **url：**[必填项] 该接口的调用地址；
- **order：**[选填项] 该接口的执行顺序，默认为 0；
- **method：** [选填项] 该接口的请求方式，默认 POST；
- **contentType：** [选填项] 该接口的数据格式，默认 JSON；
- **charset：** [选填项] 该接口的字符编码，默认 UTF8；
- **blocked：** [选填项] 该接口的中断类型，即该接口调用异常时如何处理，默认抛出异常 EXCEPTION；
- **timeout：** [选填项] 该接口的超时时间，默认 15000 毫秒；



### rpc 标签

> RPC 注册和定义需遵循 RPC 规范，框架之后引用 RPC 的 consumer 的 bean id

**rpc 的标签属性：**

- **consumerId：**[必填项] RPC消费者 ID；

- **method：**[必填项] RPC 方法；

- **order：**[选填项] 该接口的执行顺序，默认为 0；

- **blocked：** [选填项] 该接口的中断类型，即该接口调用异常时如何处理，默认抛出异常 EXCEPTION；

  

### local 标签

> local 定义一个本地方法调用

**local 的标签属性：**

- **beanId：**[必填项] 本地方法 bean id，该 bean 需要注入到 spring 容器中；
- **method：**[必填项] 本地调用方法名称；
- **order：**[选填项] 该接口的执行顺序，默认为 0；
- **blocked：** [选填项] 该接口的中断类型，即该接口调用异常时如何处理，默认抛出异常 EXCEPTION；

  

### ref 标签

> ref 引用本地 xml 配置的服务

**ref 的标签属性：**

- **requestPath：**[必填项] 对应本地 xml 配置的 merchant 标签 requestPath 值；
- **merchantCode：**[选填项] 商户编码；
- **order：**[选填项] 该接口的执行顺序，默认为 0；
- **blocked：** [选填项] 该接口的中断类型，即该接口调用异常时如何处理，默认抛出异常 EXCEPTION；

  

### redirect 标签

> redirect 重定向

**redirect 的标签属性：**

- **url：**[必填项] 重定向地址，如果为空，则会从 req 的 $.url 属性中获取；
- **charset：**[选填项] 数据编码；
- **order：**[选填项] 该接口的执行顺序，默认为 0；
- **blocked：** [选填项] 该接口的中断类型，即该接口调用异常时如何处理，默认抛出异常 EXCEPTION；



### condition 标签

> condition 标签定义一组执行条件；
>
> 不配置则默认 TRUE 执行；
>
> 作为 api、rpc、local、ref、redirect 的子标签使用；

**condition 组由 compare 标签组成，compare 标签属性：**

- **sourceKey：**[必填项] 条件数据定义；
- **sourceFrom：**[选填项] 条件数据来源；
- **refApiId：**[选填项] 条件数据来源标签 id；
- **compareValue：**[选填项] 比较值；
- **compareType：**[选填项] 比较类型，默认 equal；
- **union：**[选填项] 条件连接符，默认 AND；
- **compareHandle：**[选填项] 条件处理器，需要返回一个**布尔值**；



### req 标签

> req 标签定义请求数据或本地方法参数；
>
> 不配置则默认透传；
>
> 作为 api、rpc、local、ref、redirect 的子标签使用；

**req 的标签属性：**

- **dataHandle：**[选填项] req 整体数据处理器，可以是多个，用英文逗号分隔；



### resp 标签

> resp 标签定义响应数据或本地方法返回；
>
> 不配置则默认透传；
>
> 只能作为 api、rpc、local、ref、redirect 的子标签使用

**resp 的标签属性：**

- **dataHandle：**[选填项] resp 整体数据处理器，可以是多个，用英文逗号分隔；



### property、param 标签

> property 标签定义数据转换映射关系；
>
> param 标签定义 local 或 RPC 方法的参数；
>
> 只能作为 req、resp 的子标签使用；



**property、param 的标签属性：**

- **targetKey：** [必填项] 定义目标字段名，可以通过JSON path 的方式设置；
- **sourceKey：** [选填项] 源数据的字段名，会从源数据中找这个字段对应的值，通过 JSON path 设置；
- **defaultValue：**[选填项] 默认值，当从源数据中无法找到 sourceKey 对应的值时会使用默认值；
- **dataHandle：**[选填项] 配置该数据的处理器，可以配置多个，以英文逗号分隔；
- **sourceFrom：**[选填项] 源数据来源，可选项有 REQ，RESP，HEADER，COOKIE；默认请求到请求，响应到响应；
- **refApiId：**[选填项] 源数据是从哪个接口来，对应的是 api 的 id，默认是从标准请求报文中取；
- **dataType:** [必填项，仅 param 标签使用] ，定义数据类型，java 原生类型可以简写；

> **targetKey 数组配置说明：**
> 
> 数组元素需要用中括号 '[]' 标识
> 
> **添加元素[*]:** $.xxx.yyy[*].zzz
> 
> **遍历赋值[..]:** $.xxx.yyy[..].zzz
> 
>   a. 如果源数据是数组，则会遍历源数据，按下标对应赋值；
> 
>   b. 如果源数据非数组，则会将该值赋值给每一个元素；
> 
> **指定下标赋值[x]:** 给执行下标 x 赋值
> 



### mock 标签

> mock 标签为当前服务设置模拟请求/响应数据；
>
> mock 标签可在 merchant 标签或者服务标签下使用；


**mock 的标签属性：**

- **type：** [必填项] 定义 mock 的是请求数据还是响应数据（REQ/RESP）；
- **enable：** [选填项] 当前 mock 功能是否开启，默认开启（true）；
- **file：**[选填项] 指定 mock 数据文件；
- **dataHandle：**[选填项] 为 mock 数据设置一个数据处理器；



## 标签概览

```xml
<so:merchant requestPath="demo_path">
    <so:description>联合登录</so:description>
    <so:api url="">
        <so:description>http 服务说明</so:description>
        <so:condition>
            <so:compare/>
        </so:condition>
        <so:req>
            <so:property targetKey=""/>
            <so:header targetKey=""/>
            <so:cookie targetKey=""/>
        </so:req>
        <so:resp>
            <so:property targetKey=""/>
            <so:header targetKey=""/>
            <so:cookie targetKey=""/>
        </so:resp>
    </so:api>
    <so:rpc consumerId="" method="">
        <so:description>RPC 服务说明</so:description>
        <so:condition>
            <so:compare/>
        </so:condition>
        <so:req>
            <so:property targetKey=""/>
            <so:param dataType="" targetKey=""/>
        </so:req>
        <so:resp>
            <so:property targetKey=""/>
            <so:header targetKey=""/>
            <so:cookie targetKey=""/>
        </so:resp>
    </so:rpc>
    <so:local beanId="" method="">
        <so:description>本地代码调用</so:description>
        <so:condition>
            <so:compare/>
        </so:condition>
        <so:req>
            <so:property targetKey=""/>
            <so:param dataType="" targetKey=""/>
        </so:req>
        <so:resp>
            <so:property targetKey=""/>
            <so:header targetKey=""/>
            <so:cookie targetKey=""/>
        </so:resp>
    </so:local>
    <so:ref requestPath="">
        <so:description>XML 配置服务引用</so:description>
        <so:condition>
            <so:compare/>
        </so:condition>
        <so:req>
            <so:property targetKey=""/>
            <so:header targetKey=""/>
            <so:cookie targetKey=""/>
        </so:req>
        <so:resp>
            <so:property targetKey=""/>
            <so:header targetKey=""/>
            <so:cookie targetKey=""/>
        </so:resp>
    </so:ref>
    <so:redirect url="">
        <so:description>重定向</so:description>
        <so:condition>
            <so:compare/>
        </so:condition>
        <so:req>
            <so:property targetKey=""/>
            <so:header targetKey=""/>
            <so:cookie targetKey=""/>
        </so:req>
    </so:redirect>
</so:merchant>
```



## springboot 使用示例

> 建议项目使用 springboot 架构；

### step 1：引入pom依赖

```xml
<dependency>
    <groupId>io.github.andy-wys</groupId>
    <artifactId>service-orchestration-springboot-starter</artifactId>
    <version>框架版本</version>
</dependency>
```



### step 2：创建接口文件

> 在 resource 目录下创建接口 xml 配置文件；
>
> 可以通过 so.merchant.api-file 指定接口配置目录；


- **引入网关配置 schema 文件**

  > https://github.com/andy-wys/service-orchestration  https://github.com/andy-wys/service-orchestration/so-api.xsd

```xml
<?xml version="1.0" encoding="UTF-8"?>
<so:merchant 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:so="https://github.com/andy-wys/service-orchestration"
	xsi:schemaLocation="https://github.com/andy-wys/service-orchestration 
		https://github.com/andy-wys/service-orchestration/so-api.xsd"
	requestPath="对外暴露的接口地址">
  
  </beans>
```

- **配置商户接口**

  

- **配置示例：**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<so:merchant 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:so="https://github.com/andy-wys/service-orchestration"
	xsi:schemaLocation="https://github.com/andy-wys/service-orchestration 
		https://github.com/andy-wys/service-orchestration/so-api.xsd"
	code="insure" name="保险" requestPath="multi_api" reqDataHandle="自定义处理器" respDataHandle="自定义处理器">  
      <!--
          1. 为保险公司定义一个组合接口，这个接口的请求路径为 http://xxx/multi_api；
          2. 这个标准接口会调用下游的2个服务去分别请求，并从这三个请求中分别取值，组合成一个标准响应报文；
          3. 这2个下游服务分别是 so:api 标签定义的服务； 
      -->
     
      <so:description>为当前商户作说明，无实际作用</so:description>
      <!-- 
      接口一：
          请求地址：${so.merchant.host}/demo_convert，其中 ${so.merchant.host} 从配置文件中取值
          中断处理：RETURN 表示将已经处理完的数据直接返回，不再执行下面的其他接口
          该接口的ID为：demo_api_id_convert
          请求方式为：POST
          数据格式为：JSON
          字符编码为：UTF8
          执行顺序为：0，会先执行
          超时时间为：15000 毫秒
      -->
      <so:api url="${so.merchant.host}/demo_convert" blocked="RETURN" id="demo_api_id_convert" 
              method="POST" contentType="JSON" charset="UTF8" order="0" timeout="15000">
          <so:description>当前 api 说明信息，无实际作用</so:description>
          <so:req>
              <!-- 请求报文透传 -->
              <so:property targetKey="$" sourceKey="$"/>
          </so:req>
          <so:resp>
              <!-- 响应报文透传 -->
              <so:property targetKey="$" sourceKey="$"/>
          </so:resp>
      </so:api>
      <!--
      接口二：
          请求地址：https://v0.yiketianqi.com/api
          该接口的ID为：demo_api_id_convert
          请求方式为：GET
  -->
      <so:api url="https://v0.yiketianqi.com/api" method="GET">
          <so:description>公网天气查询接口</so:description>
          <so:req>
              <!-- unescape = 1 -->
              <so:property targetKey="$.unescape" defaultValue="1"/>
              <!-- 从请求数据的 data 对象下的 version对应的值，赋值给 version，没有取到则使用默认值 v61 -->
              <so:property targetKey="$.version" sourceKey="$.data.version" defaultValue="v61"/>
              <!-- 从 demo_api_id_convert 接口的响应报文中取 data.appid 的值，设置给 appid -->
              <so:property targetKey="$.appid" sourceKey="$.data.appid" sourceFrom="RESP" refApiId="demo_api_id_convert"/>
          </so:req>
          <so:resp>
              <!-- 从这个api的响应报文中取到 city 对应的值，并执行 URL_ENCODE 处理器，然后设置给 data.weather.aqi.myCityName -->
              <so:property targetKey="$.data.weather.aqi.myCityName" sourceKey="$.city" dataHandle="URL_ENCODE"/>
          </so:resp>
      </so:api>
  </so:merchant>
  
```



### step 3：配置XML路径

> 在配置文件中增加 XML 的扫描路径，项目启动时会从该路径下扫描 XML 文件

```properties
# 接口配置文件
so.merchant.api-file=classpath*:api/*.xml
```



### step 4：启动项目测试

----

## WEB 项目使用示例

> 建议项目使用 springboot 架构；
> 
> WEB 项目需要自己使用 XML 或注解方式注入框架实现的 bean；

### step 1：引入pom依赖

```xml
<dependency>
    <groupId>io.github.andy-wys</groupId>
    <artifactId>service-orchestration-core</artifactId>
    <version>框架版本</version>
</dependency>
```

### step 2：注入框架服务实现 bean

#### 2.1 注入全局配置类
```xml
<!-- 网关全局属性配置类，bean id 必须为 soMerchantProperty -->
<bean id="soMerchantProperty" class="com.jd.so.core.SoMerchantProperty" primary="false">
  <property name="name" value="[可选，全局默认商户服务名称]"/>
  <property name="code" value="[可选，全局默认商户编码，此处配置则在对应的 XML 中则可省略 code 配置]"/>
  <property name="apiFile">
    <!-- 可选，商户接口配置文件，默认 classpath:api/*.xml -->
    <list>
      <value>classpath:api/*.xml</value>
    </list>
  </property>
  <property name="apiRootPath" value="[可选，商户接口访问根路径]"/>
  <property name="mockDataDir" value="[可选，mock 数据文件存放目录，默认 classpath:api-mock/]"/>
  <!-- 否开启 mock 数据，开启后 mock 功能才会生效，默认 false -->
  <property name="mockEnable" value="false"/>
</bean>

```
#### 2.2 注入框架默认实现
**如果都使用默认实现，则直接引入 classpath 下的 service-orchestration-beans.xml 默认配置即可：**
```xml
<import resource="classpath*:service-orchestration-beans.xml"/>
```

**如果要自定义注入，则按以下配置注入 bean：**
参考 service-orchestration-beans.xml 文件


### step 3：创建接口文件，同 springboot 方式

### step 4：启动项目测试
