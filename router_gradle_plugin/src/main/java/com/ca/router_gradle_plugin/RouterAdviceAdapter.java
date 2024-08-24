package com.ca.router_gradle_plugin;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * 类的作用的简要阐述
 * <p>
 * 类的作用的详细阐述
 * <p>创建时间：2024/8/22/022</p>
 *
 * @author yanxiong
 */
public class RouterAdviceAdapter extends AdviceAdapter {


    private String methodName;
    private MethodVisitor methodVisitor;
    /**
     * Constructs a new {@link AdviceAdapter}.
     *
     * @param api
     * @param methodVisitor the method visitor to which this adapter delegates calls.
     * @param access        the method's access flags
     * @param name          the method's name.
     * @param descriptor    the method's descriptor
     */
    protected RouterAdviceAdapter(int api, MethodVisitor methodVisitor, int access, String name, String descriptor) {
        super(api, methodVisitor, access, name, descriptor);
        this.methodName = name;
        this.methodVisitor = methodVisitor;
    }

    @Override
    protected void onMethodEnter() {
        enter();
    }

    private void enter() {
        if ("onCreate".equals(methodName)) {
            System.out.println("onMethodEnter");
            methodVisitor.visitLdcInsn("MainActivity");
            methodVisitor.visitLdcInsn("log from asm enter" + methodName);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "android/util/Log", "e", "(Ljava/lang/String;Ljava/lang/String;)I", false);
            methodVisitor.visitInsn(POP);
        }

    }

    @Override
    protected void onMethodExit(int opcode) {
        exit();
    }

    private void exit() {
        // 忽略构造方法
        if ("<init>".equals(methodName)) {
            return;
        }
        if ("onCreate".equals(methodName)) {
            System.out.println("onMethodExit");
            methodVisitor.visitLdcInsn("MainActivity");
            methodVisitor.visitLdcInsn("log from asm exit"+methodName);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "android/util/Log", "e", "(Ljava/lang/String;Ljava/lang/String;)I", false);
            methodVisitor.visitInsn(POP);
        }
    }
}

