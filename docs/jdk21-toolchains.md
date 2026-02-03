# 本工程单独使用 JDK 21（与老工程 JDK 8 共存）

本工程要求 JDK 21；你当前机器可能默认是 JDK 1.8。可以**只在本工程**里指定使用 JDK 21，不影响老工程。

---

## 方式一：Maven Toolchains（推荐，命令行编译/运行用）

Maven 会从「工具链」里取 JDK 21，只在本项目生效。

### 1. 配置一次：在用户目录建工具链文件

- **Windows**：`C:\Users\你的用户名\.m2\toolchains.xml`
- **Mac/Linux**：`~/.m2/toolchains.xml`

若已存在 `toolchains.xml`，在 `<toolchains>` 里追加一个 `<toolchain>` 即可。

### 2. 内容示例（把 `jdkHome` 改成你本机 JDK 21 的安装路径）

```xml
<?xml version="1.0" encoding="UTF-8"?>
<toolchains xmlns="http://maven.apache.org/TOOLCHAINS/1.1.0"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://maven.apache.org/TOOLCHAINS/1.1.0 http://maven.apache.org/xsd/toolchains-1.1.0.xsd">
  <toolchain>
    <type>jdk</type>
    <provides>
      <version>21</version>
    </provides>
    <configuration>
      <!-- 本机 JDK 21 安装路径（GraalVM 示例） -->
      <jdkHome>C:\Program Files\Java\graalvm-jdk-21.0.6+8.1</jdkHome>
    </configuration>
  </toolchain>
</toolchains>
```

### 3. 在本工程目录用 Maven

在 **attendance-backend** 目录下执行：

```bash
mvn clean compile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Maven 会使用上面配置的 JDK 21，不会用系统默认的 JDK 8。

---

## 方式二：IDE（Cursor / VS Code）仅本工程用 JDK 21

打开的是 **qianyi** 或 **attendance-backend** 这个文件夹时，让 Java 扩展只用 JDK 21。

1. 在项目根目录（qianyi）下建 `.vscode/settings.json`（没有 `.vscode` 就新建）。
2. 把下面的 `java.jdt.ls.java.home` 改成你本机 **JDK 21** 的安装路径（注意 Windows 下反斜杠要写成 `\\` 或用 `/`）：

```json
{
  "java.configuration.runtimes": [
    { "name": "JavaSE-21", "path": "C:\\Program Files\\Java\\graalvm-jdk-21.0.6+8.1", "default": true }
  ],
  "java.jdt.ls.java.home": "C:\\Program Files\\Java\\graalvm-jdk-21.0.6+8.1"
}
```

3. 重新打开该文件夹或重启 Java Language Server，本工程就会用 JDK 21 做编译、运行、调试；其它打开老工程的工作区仍可用 JDK 8。

---

## 方式三：临时用环境变量（不推荐长期用）

不改任何配置文件，只在当前终端用 JDK 21 跑 Maven：

- **Windows（PowerShell）**：
  ```powershell
  $env:JAVA_HOME = "C:\Program Files\Java\graalvm-jdk-21.0.6+8.1"
  mvn clean compile
  ```
- **Windows（CMD）**：
  ```cmd
  set JAVA_HOME=C:\Program Files\Java\graalvm-jdk-21.0.6+8.1
  mvn clean compile
  ```

这样只影响当前窗口，老工程用默认 JDK 8 不受影响。

---

总结：**本工程可以单独指定 JDK 21 路径**：用方式一指定 Maven 用的 JDK，用方式二指定 IDE 用的 JDK，老工程继续用 JDK 1.8 即可。
