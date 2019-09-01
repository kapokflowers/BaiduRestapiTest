package com.baidu.speech.restapi.ttsdemo;

import com.baidu.speech.restapi.common.DemoException;
import com.baidu.speech.restapi.common.ConnUtil;
import com.baidu.speech.restapi.common.FileUtil;
import com.baidu.speech.restapi.common.TokenHolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TtsMain {

    public static void main(String[] args) throws IOException, DemoException {
        // filePathList 文件内放着所有测试文件的 txt 地址
        List<String> filePathList = FileUtil.readToList("/Users/gongmw/Documents/baiduTest/test.txt");
        // 为每一个 txt 文件生成语音文件
        for(String i : filePathList){
            (new TtsMain()).run(i);
        }
    }

    //  填写网页上申请的appkey 如 $apiKey="g8eBUMSokVB1BHGmgxxxxxx"
    private final String appKey = "AkGEWXO4GO0ncaEnYfkluhnh";

    // 填写网页上申请的APP SECRET 如 $secretKey="94dc99566550d87f8fa8ece112xxxxx"
    private final String secretKey = "VfRPD4PHr789T33ckEplSnD9Fe9Xdkmg";

    // text 的内容为"欢迎使用百度语音合成"的urlencode,utf-8 编码
    // 可以百度搜索"urlencode"
    //private final String text = "凌凤，\n您好，欢迎使用百度语音";

    // 发音人选择, 基础音库：0为度小美，1为度小宇，3为度逍遥，4为度丫丫，
    // 精品音库：5为度小娇，103为度米朵，106为度博文，110为度小童，111为度小萌，默认为度小美
    private final int per = 0;
    // 语速，取值0-15，默认为5中语速
    private final int spd = 5;
    // 音调，取值0-15，默认为5中语调
    private final int pit = 5;
    // 音量，取值0-9，默认为5中音量
    private final int vol = 5;

    // 下载的文件格式, 3：mp3(default) 4： pcm-16k 5： pcm-8k 6. wav
    private final int aue = 3;

    public final String url = "http://tsn.baidu.com/text2audio"; // 可以使用https

    private String cuid = "1234567JAVA";

    private void run(String filePath) throws IOException, DemoException {
        TokenHolder holder = new TokenHolder(appKey, secretKey, TokenHolder.ASR_SCOPE);
        holder.refresh();
        String token = holder.getToken();
        String text = FileUtil.readFile(filePath);

        // 此处2次urlencode， 确保特殊字符被正确编码
        String params = "tex=" + ConnUtil.urlEncode(ConnUtil.urlEncode(text));
        params += "&per=" + per;
        params += "&spd=" + spd;
        params += "&pit=" + pit;
        params += "&vol=" + vol;
        params += "&cuid=" + cuid;
        params += "&tok=" + token;
        params += "&aue=" + aue;
        params += "&lan=zh&ctp=1";
        System.out.println(url + "?" + params); // 反馈请带上此url，浏览器上可以测试
        // 发送 HTTP
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(true);
        // 连接超时时间为 5 s
        conn.setConnectTimeout(5000);
        // 获取连接返回内容
        PrintWriter printWriter = new PrintWriter(conn.getOutputStream());
        printWriter.write(params);
        printWriter.close();
        String contentType = conn.getContentType();
        // 如果返回内容有语音
        if (contentType.contains("audio/")) {
            // 获取语音内容
            byte[] bytes = ConnUtil.getResponseBytes(conn);
            // 获取语音内容格式
            String format = getFormat(aue);
            // 设置保存文件位置
            File file = new File(filePath.replace(".txt","."+format)); // 打开mp3文件即可播放
            // System.out.println( file.getAbsolutePath());
            // 获取语音输出流
            FileOutputStream os = new FileOutputStream(file);
            // 保存语音文件
            os.write(bytes);
            os.close();
            System.out.println("audio file write to " + file.getAbsolutePath());
        } else {
            System.err.println("ERROR: content-type= " + contentType);
            String res = ConnUtil.getResponseString(conn);
            System.err.println(res);
        }
    }

    // 下载的文件格式, 3：mp3(default) 4： pcm-16k 5： pcm-8k 6. wav
    private String getFormat(int aue) {
        String[] formats = {"mp3", "pcm", "pcm", "wav"};
        return formats[aue - 3];
    }
}
