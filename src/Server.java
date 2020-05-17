
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLEncoder;


public class Server {
    //监听端口
    private static final int PORT = 8888;

    /**
     * 入口
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        Socket socket = null;
        try {
            //建立服务器的Socket，并设定一个监听的端口PORT
            serverSocket = new ServerSocket(PORT);
            //由于需要进行循环监听，因此获取消息的操作应放在一个while大循环中
            while (true) {
                try {
                    //建立跟客户端的连接
                    socket = serverSocket.accept();
                    System.out.println("连接成功");
                } catch (Exception e) {
                    System.out.println("建立与客户端的连接出现异常");
                    e.printStackTrace();
                }
                ServerThread thread = new ServerThread(socket);
                thread.start();
            }
        } catch (Exception e) {
            System.out.println("端口被占用");
            e.printStackTrace();
        } finally {
            serverSocket.close();
        }
    }
}

//服务端线程类
//继承Thread类的话，必须重写run方法，在run方法中定义需要执行的任务。
class ServerThread extends Thread {
    public static final String key = "6ad54d3dacbffe828a7491a56723942f";
    private Socket socket;
    InputStream inputStream;
    OutputStream outputStream;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            while (true) {
                //接收客户端的消息并打印
                System.out.println(socket);
                inputStream = socket.getInputStream();
                //定义字节数组
                byte[] bytes = new byte[65534];
                inputStream.read(bytes);
                //将十六进制字节转为十六进制字符串
                String string = new String(bytes);

                //将16进制存进文件
                File file = new File("C:\\Java_jar\\file\\666.txt");
                //if file doesnt exists, then create it
                if (!file.exists()) {
                    file.createNewFile();
                }
                //true = append file
                FileWriter fileWritter = new FileWriter(file, true);
                fileWritter.write(string.trim());
                fileWritter.close();

                String sr = "";
                String k = "";
                //如果字符串包含 8888,读取文件内容赋值给字符串
                if (string.trim().indexOf("8888") != -1) {
                    //读取文件
                    String str = FileUtils.readFile("C:\\Java_jar\\file\\666.txt");
                    System.out.println("str:" + str.replace("8888", ""));
                    //获取垃圾类别
                    sr = out(str.replace("8888", ""));
                    System.out.println(sr);
                    //解析json字符串
                    k = JsonArr(sr);
                    //清空文件内容
                    clearInfoForFile("C:\\Java_jar\\file\\666.txt");
                }

                //向客户端发送消息
                outputStream = socket.getOutputStream();
                outputStream.write(k.getBytes());
                System.out.println(k);
            }
        } catch (Exception e) {
            System.out.println("客户端主动断开连接了");
            e.printStackTrace();
        }
        //操作结束，关闭socket
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("关闭连接出现异常");
            e.printStackTrace();
        }
    }

    /**
     * 将十六进制字符串转图片，再识别所属分类
     *
     * @param string 从串口获得的字符串
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String out(String string) throws UnsupportedEncodingException {
        //将十六进制字符串转为图片
        Hex2Image.saveToImgFile(string.replace(" ", "").trim().toUpperCase(), "C:\\Java_jar\\static\\2.jpg");
        System.out.println("将十六进制字符串转为图片");
        //将图片转为base64
        String base64Code = "";
        try {
            base64Code = Connect.encodeBase64File("C:\\Java_jar\\static\\2.jpg");
            System.out.println("将图片转为base64");
        } catch (Exception e) {
            e.printStackTrace();
        }
        String encode = URLEncoder.encode(base64Code, "utf-8");
        //发送 POST 请求
        String sr = Connect.sendPost("http://api.tianapi.com/txapi/imglajifenlei/index", "key=" + key + "&img=" + encode);
        //解析Json字符串

        return sr;
    }

    /**
     * 清空文件内容
     *
     * @param fileName
     */
    public static void clearInfoForFile(String fileName) {
        File file = new File(fileName);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析json字符串
     *
     * @param sr
     * @return
     */
    public static String JsonArr(String sr) {
        //	垃圾分类，0为可回收、1为有害、2为厨余(湿)、3为其他(干)、8重新拍摄识别
        String back = "";
        JSONObject jsonObject = JSONObject.parseObject(sr);
        System.out.println(jsonObject.getString("msg"));
        //获取data数据，然后读取data数据中datastreams关键字对应的数组（有[]标示的为数组）
        JSONArray jsonArray = jsonObject.getJSONArray("newslist");
        JSONObject obj = jsonArray.getJSONObject(0);
        System.out.println(obj.getString("lajitip"));

        if (obj.getString("lajitip").indexOf("可回收垃圾") != -1) {
            back = "1!";
        } else if (obj.getString("lajitip").indexOf("有害垃圾") != -1) {
            back = "2!";
        } else if (obj.getString("lajitip").indexOf("厨余垃圾") != -1) {
            back = "3!";
        } else if (obj.getString("lajitip").indexOf("干垃圾") != -1) {
            back = "4!";
        } else {
            back = "5!";
        }
        return back;
    }

}