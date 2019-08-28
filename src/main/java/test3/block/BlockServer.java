package test3.block;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Created by lizhen on 2018/11/23.
 */
public class BlockServer {

    public static void main(String[] args) throws IOException {

        // 1.获取通道
        try (ServerSocketChannel server = ServerSocketChannel.open()) {
            // 2. 绑定链接
            server.bind(new InetSocketAddress(6666));

            // 3.得到文件通道，将客户端传递过来的图片写到本地项目下(写模式、没有则创建)
            SocketChannel client;
            try (FileChannel outChannel = FileChannel.open(Paths.get("D:\\star_src_1_600_400_1.jpg"), StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {

                // 4. 获取客户端的连接(阻塞的)
                client = server.accept();

                // 5. 要使用NIO，有了Channel，就必然要有Buffer，Buffer是与数据打交道的呢
                ByteBuffer buffer = ByteBuffer.allocate(1024);

                // 6.将客户端传递过来的图片保存在本地中
                while (client.read(buffer) != -1) {

                    // 在读之前都要切换成读模式
                    buffer.flip();

                    outChannel.write(buffer);

                    // 读完切换成写模式，能让管道继续读取文件的数据
                    buffer.clear();
                }
            }

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            buffer.put("img has been recieved succeccfully!".getBytes("UTF-8"));
            buffer.flip();
            client.write(buffer);
            client.shutdownOutput();
        }

    }
}