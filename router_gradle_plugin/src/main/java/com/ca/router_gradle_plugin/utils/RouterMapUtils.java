package com.ca.router_gradle_plugin.utils;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.RETURN;

import com.ca.router_gradle_plugin.ClassVisitorAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import groovyjarjarasm.asm.Opcodes;

/**
 * 类的作用的简要阐述
 * <p>
 * 类的作用的详细阐述
 * <p>创建时间：2024/8/22/022</p>
 *
 * @author yanxiong
 */
public class RouterMapUtils {
    private static final String GENERATE_TO_CLASS_FILE_NAME = "com/ca/router_compiler/RouterMap.class";

    static final String GENERATE_TO_CLASS_NAME = "com/ca/router_compiler/RouterMap";
    private static final String GENERATE_TO_METHOD_NAME = "loadRouterMapPlugin";

    static final String REGISTER_METHOD_NAME = "register";

    public static void Test(File jarFile, List<String> className) throws IOException {
        if (jarFile.getName().endsWith(".jar")) {
            if (jarFile != null) {
                File optJar = new File(jarFile.getParent(), jarFile.getName() + ".opt");
                if (optJar.exists()) {
                    optJar.delete();
                }
                JarFile file = new JarFile(jarFile);
                Enumeration enumeration = file.entries();
                JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(optJar));

                while (enumeration.hasMoreElements()) {
                    JarEntry jarEntry = (JarEntry) enumeration.nextElement();
                    String entryName = jarEntry.getName();
                    ZipEntry zipEntry = new ZipEntry(entryName);
                    InputStream inputStream = file.getInputStream(jarEntry);
                    jarOutputStream.putNextEntry(zipEntry);
                    if (GENERATE_TO_CLASS_FILE_NAME.equals(entryName)) {
                        byte[] bytes = referHackWhenInit(inputStream,className);
                        jarOutputStream.write(bytes);
                    } else {
                        jarOutputStream.write(IOUtils.toByteArray(inputStream));
                    }
                    inputStream.close();
                    jarOutputStream.closeEntry();
                }
                jarOutputStream.close();
                file.close();

                if (jarFile.exists()) {
                    jarFile.delete();
                }
                optJar.renameTo(jarFile);
            }
        }
    }

    private static byte[] referHackWhenInit(InputStream inputStream,  List<String> className) throws IOException {
        ClassReader cr = new ClassReader(inputStream);
        ClassWriter cw = new ClassWriter(cr, 0);
        ClassVisitor cv = new ClassVisitor(Opcodes.ASM5, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                //generate code into this method
                System.out.println("当前方法名: " + name);
                if (name.equals(GENERATE_TO_METHOD_NAME)) {
                    mv = new MethodVisitor(Opcodes.ASM5, mv) {
                        @Override
                        public void visitInsn(int opcode) {
                            if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)) {
                                System.out.println("插入的地址: " + className);
                                className.forEach(s -> {
                                    String name1 =s.replace('/', '.').replace(".class", "");
                                    mv.visitVarInsn(ALOAD, 0);
                                    mv.visitLdcInsn(name1);
                                    // generate invoke register method into LogisticsCenter.loadRouterMap()
                                    mv.visitMethodInsn(Opcodes.INVOKESPECIAL
                                            , GENERATE_TO_CLASS_NAME
                                            , REGISTER_METHOD_NAME
                                            , "(Ljava/lang/String;)V"
                                            , false);
                                });

                            }
                            super.visitInsn(opcode);
                        }

                        @Override
                        public void visitMaxs(int maxStack, int maxLocals) {
                            super.visitMaxs(maxStack + 4, maxLocals);
                        }
                    };
                }
                return mv;
            }
        };
        cr.accept(cv, ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }
}
