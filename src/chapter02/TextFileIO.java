package chapter02;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class TextFileIO {
    private PrintWriter pw = null;
    private Scanner sc = null;

    // 保存到文件
    public void append(String msg, File file) {
        if (file == null) {
            return;
        }
        //以追加模式utf-8的编码模式写到文件中
        try {
            pw = new PrintWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(file, true), StandardCharsets.UTF_8));
            pw.println(msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pw.close();
        }

    };

    // 输出到窗口
    public String load(File file) {
        if (file == null) //用户放弃操作则返回
        {
            System.out.println("file null out");
            return null;
        }
        StringBuilder sb = new StringBuilder();
        try {
            // 读和写的编码要注意保持一致
            // 读取文件
            sc = new Scanner(file, "utf-8");
            while (sc.hasNext()) {
                sb.append(sc.nextLine()).append("\n"); //补上行读取的行末尾回车
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sc.close();
        }
        return sb.toString();
    }
}
