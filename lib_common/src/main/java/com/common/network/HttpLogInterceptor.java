package com.common.network;
import android.text.TextUtils;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URLDecoder;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
/**
 * Created by yan_x
 *
 * @date 2021/11/15/015 16:19
 * @description
 */
public class HttpLogInterceptor implements Interceptor {
    private static final String TAG = "HttpLogInterceptor";

    public HttpLogInterceptor() {
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        //添加到责任链中
        Request request = chain.request();
        logForRequest(request);
        Response response = chain.proceed(request);
        return logForResponse(response);
    }

    /**
     * 打印响应日志
     * @param response
     * @return
     */
    private Response logForResponse(Response response) {
        Response.Builder builder = response.newBuilder();
        Response clone = builder.build();
        StringBuffer buffer=new StringBuffer();
        buffer.append("响应url:"+ clone.request().url()+
                "\n code:" + clone.code()+
                "\n message:" + clone.message().toString());
        ResponseBody body = clone.body();
        if (body != null) {
            MediaType mediaType = body.contentType();
            if (mediaType != null) {
                if (isText(mediaType)) {
                    String resp = null;
                    try {
                        resp = body.string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    buffer.append("\n 返回结果------\n"+
                            resp+"\n"+
                            "\n********响应日志结束********");
                    logLong(buffer.toString());
                    body = ResponseBody.create(mediaType, resp);
                    return response.newBuilder().body(body).build();
                } else {
                    Log.e(TAG, "响应内容 : " + "发生错误-非文本类型");
                }
            }
        }
        Log.e(TAG, buffer.toString());
        return response;
    }

    private boolean isText(MediaType mediaType) {
        if (mediaType.type() != null && mediaType.type().equals("text")) {
            return true;
        }
        if (mediaType.subtype() != null) {
            if (mediaType.subtype().equals("json")
                    || mediaType.subtype().equals("xml")
                    || mediaType.subtype().equals("html")
                    || mediaType.subtype().equals("webviewhtml")
                    || mediaType.subtype().equals("x-www-form-urlencoded")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 打印请求日志
     * @param request
     */
    private void logForRequest(Request request) {
        String url = request.url().toString();
        StringBuffer buffer=new StringBuffer();
        buffer.append("========网络日志开始=======\n" +
                "请求方式"+ request.method()+
                "\nurl :"+url+
                "\nheaders :"+request.headers());
        RequestBody requestBody = request.body();
        if (requestBody != null) {
            MediaType mediaType = requestBody.contentType();
            if (mediaType != null) {
                Log.d(TAG, "请求内容类别 : " + mediaType.toString());
                if (isText(mediaType)) {
                    Log.d(TAG, "请求内容 : " + bodyToString(request));
                } else {
                    Log.d(TAG, "请求内容 : " + " 无法识别。");
                }
            }
        }
        Log.e(TAG, buffer.toString());
    }

    private String bodyToString(Request request) {
        Request req = request.newBuilder().build();
        String urlSub = null;
        Buffer buffer = new Buffer();
        try {
            req.body().writeTo(buffer);
            String message = buffer.readUtf8();
            urlSub = URLDecoder.decode(message, "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
            return "在解析请求内容时候发生了异常-非字符串";
        }
        return urlSub;
    }
    private void logLong(String log) {
        if (log.length() > 4000) {
            for (int i = 0; i < log.length(); i += 4000) {
                if (i + 4000 < log.length())
                    Log.e(TAG, log.substring(i, i + 4000));
                else
                    Log.e(TAG, log.substring(i, log.length()));
            }
        } else {
            Log.e(TAG, log);
        }
    }



}

