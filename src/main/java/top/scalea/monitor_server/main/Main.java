package top.scalea.monitor_server.main;

import org.apache.commons.io.FileUtils;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;

public class Main {
    /**
     * 目标{@link URI}
     */
    public static final URI[] uris;
    /**
     * 入口对象
     */
    public static final SystemInfo si = new SystemInfo();
    /**
     * 内存对象
     */
    public static final GlobalMemory memory = si.getHardware().getMemory();
    /**
     * 总内存(G)
     */
    public static final float mx = (float) memory.getTotal() / 1024 / 1024 / 1024;
    /**
     * 总内存(bit)
     */
    public static final long mxl = memory.getTotal();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("已启动");
        while (true) {
            inspect();
            Thread.sleep(1000);
        }
    }

    /**
     * 进行一次检测
     */
    public static void inspect() {
        String msg;
        //剩余可用内存(bit)
        long syl = memory.getAvailable();
        //内存使用率
        int sl = (int) ((double) (mxl - syl) / mxl * 100);
        //
        if (sl == 100) {
            msg = "内存到达警戒100%！！！";
        } else if (sl >= 99) {
            msg = "内存到达警戒：99%！！！";
        } else if (sl >= 98) {
            msg = "内存到达警戒：98%！！！";
        } else if (sl >= 95) {
            msg = "内存到达警戒：95%！！";
        } else if (sl >= 80) {
            msg = "内存到达警戒：90%！";
        } else return;
        System.out.println(msg);
        //剩余可用内存(G)
        float sy = (float) syl / 1024 / 1024 / 1024;
        //已用内存(G)
        float no = (float) (mxl - syl) / 1024 / 1024 / 1024;
        //发送
        for (URI uri : uris) {
            send(uri, msg, mx, sy, no, sl);
        }
    }

    /**
     * 发送到Webhook
     *
     * @param uri Webhook URI
     * @param msg 消息
     * @param mx  总内存
     * @param sy  剩余内存
     * @param no  已用内存
     * @param sl  内存使用率
     */
    public static void send(URI uri, String msg, float mx, float sy, float no, int sl) {
        //http请求体
        HttpRequest hr = HttpRequest.newBuilder(uri)
                //设置请求头
                .header("Accept", "application/json")
                //设置请求正文
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {
                        	"msg": "%s",
                        	"mx": %f,
                        	"no": %f,
                        	"sy": %f,
                        	"sl": %d
                        }
                        """.formatted(msg, mx, no, sy, sl))).build();
        try {
            //发送http请求
            HttpClient.newHttpClient().send(hr, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static {
        //原始URI字符串组
        String[] suris;
        try {
            //原始URI.txt
            String su = FileUtils.readFileToString(new File("uri.txt"), StandardCharsets.UTF_8);
            if (su.isBlank()) {
                throw new RuntimeException("No URI, please check URI.txt");
            } else {
                suris = su.split("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (suris.length == 0) {
            throw new RuntimeException("No URI, please check URI.txt");
        }
        URI[] temp = new URI[suris.length];
        int l = 0;
        //转换为URI对象
        for (String s : suris) {
            try {
                temp[l] = new URI(s);
                l++;
                //出错代表URI无效，忽略
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        if (l == 0) {
            throw new RuntimeException("No URI, please check URI.txt");
        }
        URI[] to = new URI[l];
        //复制有效数据
        System.arraycopy(temp, 0, to, 0, l);
        uris = to;
    }


}
