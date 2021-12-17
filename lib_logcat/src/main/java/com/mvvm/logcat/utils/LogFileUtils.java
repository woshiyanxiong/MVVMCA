package com.mvvm.logcat.utils;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by yan_x
 *
 * @date 2021/12/17/017 16:41
 * @description
 */
public class LogFileUtils {
    private static final String TAG = LogFileUtils.class.getSimpleName();

    /**
     * 文件转字节
     */
    public static byte[] file2Bytes(String filePath) {
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                Log.w(TAG, filePath + " file is not exist!");
                return null;
            }
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    /**
     * 字节转文件
     */
    public static File bytes2File(byte[] bfile, String filePath) {
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        File file = null;
        try {
            file = new File(filePath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if(!file.exists()){
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        Log.i(TAG, "file : " + file);
        return file;
    }

    /**
     * 大文件转字节（推荐，性能较好）
     *
     * @param filePath 文件的绝对路径
     * @return 转换后的字节数组
     */
    public static byte[] bigFile2Bytes(String filePath) {
        byte[] result = null;
        FileChannel fc = null;
        try {
            fc = new RandomAccessFile(filePath, "rw").getChannel();
            MappedByteBuffer byteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0,
                    fc.size()).load();
            Log.i(TAG, "byteBuffer isLoaded :" + byteBuffer.isLoaded());
            result = new byte[(int) fc.size()];
            if (byteBuffer.remaining() > 0) {
                byteBuffer.get(result, 0, byteBuffer.remaining());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fc != null)
                    fc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.i(TAG, "bigFile2Bytes result: " + result);
        return result;
    }

    /**
     * Bitmap保存到文件
     *
     * @param bitmap   位图
     * @param filePath 保存到的绝对路径
     * @return 是否保存成功
     */
    public static boolean saveBitmap2File(Bitmap bitmap, String filePath) {
        boolean state = false;
        if (null == bitmap) {
            Log.e(TAG, " bitmap is null !");
            return state;
        }
        if (TextUtils.isEmpty(filePath)) {
            Log.e(TAG, " filePath is null !");
            return state;
        }
        File file = new File(filePath);
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if(!file.exists()){
                file.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            Log.i(TAG, "file has save: " + filePath);
            state = true;
        } catch (Exception e) {
            e.printStackTrace();
            state = false;
        }
        return state;
    }




}
