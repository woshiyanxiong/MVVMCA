package com.ca.router_gradle_plugin;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * 类的作用的简要阐述
 * <p>
 * 类的作用的详细阐述
 * <p>创建时间：2024/8/21/021</p>
 *
 * @author yanxiong
 */
public class ClassVisitorAdapter extends ClassVisitor {


    public ClassVisitorAdapter(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = cv.visitMethod(access, name, descriptor, signature, exceptions);
        return new MethodAdviceAdapter(api, methodVisitor, access, name, descriptor);
    }

}

