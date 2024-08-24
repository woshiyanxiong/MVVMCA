package com.ca.router_gradle_plugin;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.android.tools.r8.graph.S;
import com.android.utils.FileUtils;
import com.ca.router_gradle_plugin.utils.RouterMapUtils;

import org.apache.commons.codec.digest.DigestUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * 类的作用的简要阐述
 * <p>
 * 类的作用的详细阐述
 * <p>创建时间：2024/8/19/019</p>
 *
 * @author yanxiong
 */
public class TestTransform extends Transform {

    private List<String> routerList = new ArrayList<String>();

    @Override
    public String getName() {
        return "caMapRouter";
    }

    private File changeInput;

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        Collection<TransformInput> inputs = transformInvocation.getInputs();
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();
        for (TransformInput input : inputs) {
            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                File dest = outputProvider.getContentLocation(directoryInput.getName(), directoryInput.getContentTypes(), directoryInput.getScopes(), Format.DIRECTORY);
                // Traverse the directory inputs
                traverseDirectory(directoryInput.getFile());
                FileUtils.copyDirectory(directoryInput.getFile(), dest);
            }
            for (JarInput jarInput : input.getJarInputs()) {
                String destName = jarInput.getName();
                // rename jar files
                String hexName = DigestUtils.md5Hex(jarInput.getFile().getAbsolutePath());
                if (destName.endsWith(".jar")) {
                    destName = destName.substring(0, destName.length() - 4);
                }
                // input file
                File src = jarInput.getFile();
                // output file
                File dest = outputProvider.getContentLocation(destName + "_" + hexName, jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR);

                // Traverse the jar inputs
                traverseJar(jarInput.getFile(),dest);
                FileUtils.copyFile(src, dest);
            }
        }
        if (changeInput!=null){
            System.out.println("插装地址: " + changeInput.getAbsolutePath());
            try {
                RouterMapUtils.Test(changeInput,routerList);
            }catch (Exception e){
            }
//            routerList.forEach(s -> {
//
//
//            });

        }
        System.out.println("查找出来的class地址: " + changeInput.getAbsolutePath());
    }


    private void traverseDirectory(File directory) {
        // Use ASM or other bytecode library to check class files
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                traverseDirectory(file); // 递归调用以遍历子目录
            } else if (file.getName().endsWith(".class")) {
                checkClassFile(file); // 检查.class文件
            }
        }
    }

    private void traverseJar(File jarFile,File destFile) {
        // Use ASM or other bytecode library to check class files inside the jar
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (!entry.isDirectory() && entryName.endsWith(".class")) {
                    System.out.println("找到要插入的类 " + entryName);
                    if (entryName.equals("com/ca/router_compiler/RouterMap.class")) {
                        System.out.println("找到要插入的类 " + entryName);
                        changeInput = destFile;
                    }
                    try (InputStream inputStream = jar.getInputStream(entry)) {
                        // You may want to create a temporary file if you need to pass a File object to checkClassFile
                        // Here we'll just pass the inputStream directly
                        checkClassFile(inputStream, entryName);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkClassFile(InputStream inputStream, String className) {
        try {
            ClassReader classReader = new ClassReader(inputStream);
            ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM7) {
                @Override
                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                    // Check if any of the interfaces match IRouterMapRoot
                    for (String anInterface : interfaces) {
                        if ("com/ca/router_annotation/IRouterMapRoot".equals(anInterface)) {
                            System.out.println("Found implementing class: " + className);
                            routerList.add(className);
                        }
                    }
                    super.visit(version, access, name, signature, superName, interfaces);
                }
            };
            classReader.accept(classVisitor, ClassReader.SKIP_DEBUG);
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkClassFile(File classFile) {
        try (InputStream inputStream = new FileInputStream(classFile)) {
            ClassReader classReader = new ClassReader(inputStream);
            ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM7) {
                @Override
                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                    // Check if any of the interfaces match IRouterMapRoot
                    for (String anInterface : interfaces) {
                        if ("com/ca/router_annotation/IRouterMapRoot".equals(anInterface)) {
                            // Found a class that implements IRouterMapRoot
                            System.out.println("Found implementing class: " + name);
                            routerList.add(name);
                        }
                    }
                    super.visit(version, access, name, signature, superName, interfaces);
                }
            };
            classReader.accept(classVisitor, ClassReader.SKIP_DEBUG);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }
}
