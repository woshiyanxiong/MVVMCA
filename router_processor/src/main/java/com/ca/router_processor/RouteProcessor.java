package com.ca.router_processor;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.ca.router_annotation.IRouterMapRoot;
import com.ca.router_annotation.OldRoute;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * <p>

 * <p>创建时间：2023/12/21/021</p>
 *
 * @author yanxiong
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("com.ca.router_annotation.OldRoute")
public class RouteProcessor extends BaseProcessor {
    Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Route.class);
        messager.printMessage(Diagnostic.Kind.NOTE,
                "获取到的注解数量"+elements.size());
        if (elements.isEmpty()) return false;
        Map<String, String> map = new HashMap<String, String>();
        for (Element element : elements) {
            OldRoute oderRoute = element.getAnnotation(OldRoute.class);
            if (oderRoute == null) break;
            Route route = element.getAnnotation(Route.class);
            if (StringUtils.isNotEmpty(oderRoute.path()) && StringUtils.isNotEmpty(route.path())) {
                map.put(oderRoute.path(), route.path());
            }
        }
        if (map.isEmpty()){
            return false;
        }

        String groupFileName = "$$" + moduleName + "$$";
        MethodSpec.Builder loadTestMethod = MethodSpec.methodBuilder("loadRouterMap");
        loadTestMethod.addModifiers(Modifier.PUBLIC);
        loadTestMethod.returns(void.class);
        loadTestMethod.addAnnotation(Override.class);
        loadTestMethod.addParameter(ParameterizedTypeName.get(ClassName.get(Map.class), ClassName.get(String.class), ClassName.get(String.class)), "map");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            loadTestMethod.addStatement("map.put($S, $S)",
                    entry.getKey(),
                    entry.getValue());
        }

        // 创建类 Test，并添加方法 loadTest
        TypeSpec testClass = TypeSpec.classBuilder(groupFileName)
                .addModifiers(Modifier.PUBLIC, Modifier.PUBLIC)
                .addSuperinterface(IRouterMapRoot.class)
                .addMethod(loadTestMethod.build())
                .build();

        // 创建文件
        try {
            JavaFile.builder("com.ca.map.router", testClass)
                    .build()
                    .writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }


        return true;
    }


}
