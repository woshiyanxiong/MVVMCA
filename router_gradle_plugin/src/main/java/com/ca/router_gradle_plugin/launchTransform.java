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
import com.android.utils.FileUtils;

import org.apache.commons.codec.digest.DigestUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 类的作用的简要阐述
 * <p>
 * 类的作用的详细阐述
 * <p>创建时间：2024/8/21/021</p>
 *
 * @author yanxiong
 */
public class launchTransform extends Transform {
    private static final List<String> CLASS_FILE_IGNORE = Arrays.asList("R.class", "R$", "Manifest", "BuildConfig.class"); // 忽略这些文件
    @Override
    public String getName() {
        return "launchRou";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS; // 其他类型，可以集成查看
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return true;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        TransformOutputProvider provider = transformInvocation.getOutputProvider();
        for (TransformInput input : transformInvocation.getInputs()) {
            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                // directoryInput 的输入目录是：app/build/intermediates/javac/debug/classes
                doDirInputTransform(directoryInput);
                copyQualifiedContent(provider, directoryInput, null, Format.DIRECTORY);
            }
            for (JarInput jarInput : input.getJarInputs()) {
                copyQualifiedContent(provider, jarInput, getUniqueName(jarInput.getFile()), Format.JAR);
            }
        }
    }
    //根据输入的目录，遍历要插桩的class文件
    private void doDirInputTransform(DirectoryInput input) {
        List<File> files = new ArrayList<>();
        listFiles(files, input.getFile());
        for (File file : files) {
            try {
                doAsm(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //核心在这里，读出来二进制文件，访问方法，插入字节码，再写回去
    private void doAsm(File file) throws IOException {
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = new FileInputStream(file);
            ClassReader reader = new ClassReader(is);
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            ClassVisitor visitor = new ClassVisitorAdapter(Opcodes.ASM5, writer);
            reader.accept(visitor, ClassReader.EXPAND_FRAMES);
            byte[] code = writer.toByteArray();
            fos = new FileOutputStream(file.getAbsoluteFile());
            fos.write(code);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                is.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

    // 列出所有的文件
    private void listFiles(List<File> files, File file) {
        if (file == null) {
            return;
        }

        if (file.isDirectory()) {
            File[] fl = file.listFiles();
            if (fl == null || fl.length == 0) {
                return;
            }
            for (File f : fl) {
                listFiles(files, f);
            }
        } else if (needTrack(file.getName())) {
            files.add(file);
        }
    }

    private boolean needTrack(String name) {
        boolean ret = false;
        if (name.endsWith(".class")) {
            ret = !CLASS_FILE_IGNORE.contains(name);
        }
        return ret;
    }

    private void copyQualifiedContent(TransformOutputProvider provider, QualifiedContent file, String fileName, Format format) throws IOException {
        boolean useDefaultName = fileName == null;
        File dest = provider.getContentLocation(useDefaultName ? file.getName() : fileName, file.getContentTypes(), file.getScopes(), format);
        if (!dest.exists()) {
            dest.mkdirs();
            dest.createNewFile();
        }
        if (useDefaultName) {
            FileUtils.copyDirectory(file.getFile(), dest);
        } else {
            FileUtils.copyFile(file.getFile(), dest);
        }
    }

    private String getUniqueName(File jar) {
        String name = jar.getName();
        String suffix = "";
        if (name.lastIndexOf(".") > 0) {
            suffix = name.substring(name.lastIndexOf("."));
            name = name.substring(0, name.lastIndexOf("."));
        }
        String hexName = DigestUtils.md5Hex(jar.getAbsolutePath());
        return String.format("%s_%s%s", name, hexName, suffix);
    }
}
