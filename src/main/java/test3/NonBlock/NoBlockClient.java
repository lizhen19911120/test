package test3.NonBlock;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

/**
 * Created by lizhen on 2018/11/23.
 */
public class NoBlockClient {

    public static void main(String[] args)throws IOException {

        // 1. 获取通道
        Selector selector;
        try (SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 6666))) {
            // 1.1切换成非阻塞模式
            socketChannel.configureBlocking(false);
            // 1.2获取选择器
            selector = Selector.open();
            // 1.3将通道注册到选择器中，获取服务端返回的数据
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
            // 2. 发送一张图片给服务端吧
            try (FileChannel fileChannel = FileChannel.open(Paths.get("D:\\star.jpg"), StandardOpenOption.READ)) {

                // 3.要使用NIO，有了Channel，就必然要有Buffer，Buffer是与数据打交道的呢
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                int num;
                // 4.读取本地文件(图片)，发送到服务器
                while ((num = fileChannel.read(buffer)) >0) {

                    // 在读之前都要切换成读模式
                    buffer.flip();

                    socketChannel.write(buffer);

                    // 读完切换成写模式，能让管道继续读取文件的数据
                    buffer.clear();
                }

                if (num == -1) {
                    fileChannel.close();
                    socketChannel.shutdownOutput();
                    socketChannel.register(selector, SelectionKey.OP_READ);
                }
            }

            while(selector.select() >0) {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                 //7. 获取已“就绪”的事件，(不同的事件做不同的事)
                while(iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    iterator.remove();
                    // 8. 读事件就绪
                    if(selectionKey.isReadable()) {

                        SocketChannel channel = (SocketChannel) selectionKey.channel();

                        ByteBuffer responseBuffer = ByteBuffer.allocate(1024);

                    // 9. 知道服务端要返回响应的数据给客户端，客户端在这里接收
                        int readBytes = channel.read(responseBuffer);
                        if(readBytes >0) {
                            // 切换读模式
                            responseBuffer.flip();
                            System.out.println(new String(responseBuffer.array(),0, readBytes));
                        }
                    }
                }
            }

        }


    }
}