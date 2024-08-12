package com.ca.router_processor;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * 类的作用的简要阐述
 * <p>
 * 类的作用的详细阐述
 * <p>创建时间：2023/12/21/021</p>
 *
 * @author yanxiong
 */
public class BaseProcessor extends AbstractProcessor {
    // 操作Element工具类(类、函数、属性都是Element)
    Elements elementUtils;

    // type(信息类)工具类 包含用于操作TypeMirror的工具方法
    Types typeUtils;

    // 用来输出警告、错误等日志
    Messager messager;

    // 文件生成器 类/资源，Filter用来创建新的类文件，class文件以及辅助文件
    Filer filer;

    // 模块名，由模块在 build.gradle 中通过配置 annotationProcessorOptions 传进来
    String moduleName;

    public static final String PACKAGE_NAME_ACTIVITY = "android.app.Activity";
    public static final String PACKAGE_NAME_IPROVIDER = "com.demo.arouter.api.core.IProvider";

    public static final String KEY_MODULE_NAME = "AROUTER_MODULE_NAME";

    public static final CharSequence NO_MODULE_NAME_TIPS = "These no module name, at 'build.gradle', like :\n" +
            "android {\n" +
            "    defaultConfig {\n" +
            "        ...\n" +
            "        javaCompileOptions {\n" +
            "            annotationProcessorOptions {\n" +
            "                arguments = [AROUTER_MODULE_NAME: project.getName()]\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
        // 获取模块名
        Map<String, String> options = processingEnv.getOptions();
        if (options != null) {
            moduleName = options.get(KEY_MODULE_NAME);
        }
        messager.printMessage(Diagnostic.Kind.NOTE,"开始加载路由映射");

        if (StringUtils.isNotEmpty(moduleName)) {
            // 将模块名中的字符清理掉，并输出编译信息
            moduleName = moduleName.replaceAll("[^0-9a-zA-Z_]+", "");
            messager.printMessage(Diagnostic.Kind.NOTE,
                    "The user has configuration the module name, it was [\" + moduleName + \"]}"+moduleName);
        } else {
            // 如果没有配置模块名，就输出错误信息并抛异常。
            // messager 输出类型为 ERROR 的话编译会直接停掉。
            messager.printMessage(Diagnostic.Kind.ERROR, NO_MODULE_NAME_TIPS);
            throw new RuntimeException("\"ARouter::Compiler >>> No module name, for more information, look at gradle log.\"");

        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedOptions() {
        HashSet<String> hashSet = new HashSet<>();
        hashSet.add(KEY_MODULE_NAME);
        return hashSet;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }
}
