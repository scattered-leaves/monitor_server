package top.scalea.monitor_server.main;

import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Main {
    public static void main(String[] args) {
        System.out.println("已启动");
        SystemInfo si = new SystemInfo();
        GlobalMemory memory = si.getHardware().getMemory();
        float mx = (float) memory.getTotal() / 1024 / 1024 / 1024;
        float sy = (float) memory.getAvailable() / 1024 / 1024 / 1024;
        float no = (float) (memory.getTotal() - memory.getAvailable()) / 1024 / 1024 / 1024;
        long sl = (long) ((double) (memory.getTotal() - memory.getAvailable()) / memory.getTotal() * 100);
        try {
            HttpClient.newHttpClient().send(HttpRequest.newBuilder(new URI(args[0])).POST(HttpRequest.BodyPublishers.ofString("""
                    {
                    "msg":"sb",
                    "mx":%f,
                    "no":%f,
                    "sy":%f,
                    "sl":%d
                    }
                    """.formatted(mx, no, sy, sl))).build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
