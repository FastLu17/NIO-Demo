package com.lxf.nio;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

/**
 * 网络NIO的简单使用、
 *
 * @author 小66
 * @create 2019-08-17 21:38
 **/
public class SocketChannelDemo {

    /**
     *  阻塞式的Socket-NIO、
     * @throws IOException
     */
    @Test
    public void client() throws IOException {
        SocketChannel socket = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9999));
        FileChannel channel = FileChannel.open(Paths.get("C:\\Users\\Administrator\\Desktop\\NIO\\1.jpg"), StandardOpenOption.READ);
        ByteBuffer buffer = ByteBuffer.allocateDirect((int) channel.size());
        channel.read(buffer);
        buffer.flip();
        socket.write(buffer);
        buffer.clear();
        channel.close();socket.close();
    }

    /**
     *  阻塞式的Socket-NIO、
     * @throws IOException
     */
    @Test
    public void server() throws IOException {
        ServerSocketChannel open = ServerSocketChannel.open();
        FileChannel outChannel = FileChannel.open(Paths.get("C:\\Users\\Administrator\\Desktop\\NIO\\6.jpg"), StandardOpenOption.WRITE,StandardOpenOption.CREATE);
        open.bind(new InetSocketAddress(9999));
        SocketChannel accept = open.accept();
        ByteBuffer buffer = ByteBuffer.allocate(2048);
        while (accept.read(buffer) != -1) {
            buffer.flip();
            outChannel.write(buffer);
            buffer.clear();
        }
        accept.close();
        outChannel.close();
    }


    /**
     *  非阻塞式的Socket-NIO、
     */
    @Test
    public void asyncClient() throws IOException {
        SocketChannel socket = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9999));

        //开始非阻塞模式、
        socket.configureBlocking(false);

        ByteBuffer buffer = ByteBuffer.allocate(1024);

        //存入数据到缓冲区、
        buffer.put(LocalDateTime.now().toString().getBytes());

        buffer.flip();

        socket.write(buffer);

        buffer.clear();

        socket.close();
    }

    /**
     *  非阻塞式的Socket-NIO、
     */
    @Test
    public void asyncServer() throws IOException {
        //获取通道
        ServerSocketChannel open = ServerSocketChannel.open();

        //开始非阻塞模式、
        open.configureBlocking(false);

        //绑定连接、
        open.bind(new InetSocketAddress(9999));

        //获取选择器、
        Selector selector = Selector.open();

        //将通道注册到选择器上、(指定监听事件)
        int register = SelectionKey.OP_WRITE | SelectionKey.OP_ACCEPT;//多个值、
        open.register(selector, register);

        /*
        *   没写完、
        * */
        while (selector.select() > 0) {
            for (SelectionKey selectedKey : selector.selectedKeys()) {
                if (selectedKey.isAcceptable()) {//判断具体是什么事件准备就绪、这里是OP_ACCEPT
                    //获取连接、
                    SocketChannel accept = open.accept();
                    //配置为非阻塞、
                    accept.configureBlocking(false);
                    //注册、
                    accept.register(selector, SelectionKey.OP_READ);
                } else if (selectedKey.isConnectable()) {

                } else if (selectedKey.isReadable()) {

                } else if (selectedKey.isWritable()) {

                }
            }
        }

    }
}
