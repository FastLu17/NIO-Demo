package com.lxf.nio;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;

/**
 * Channel 本身不存储数据,需要配合Buffer使用、
 * Channel 在NIO中负责缓冲区中的数据传输、
 *
 * @author 小66
 * @create 2019-08-17 17:42
 **/
public class ChannelDemo {
    /**
     * Channel的主要实现类:
     *      1、FileChanel：针对File、  **** 不具备异步的能力、但是AsynchronousChannel(子类有AsyncFileChannel)
     *      2、SocketChannel：针对TCP
     *      3、ServerSocketChannel：监听TCP
     *      4、DatagramChannel：针对UDP
     *
     *      5、***** AsynchronousChannel：异步Channel、
     */

    /*
     *   获取Channel的方式：
     *       1、FileInputStream/FileOutPutStream/RandomAccessFile/Socket/ServerSocket/DatagramSocket可以通过getChannel获取、
     *       2、使用Channel的静态方法open()可以获取、
     *       3、使用Files工具类的 newByteChannel()获取、
     *
     *
     *   通道之间的数据传输方式完成文件复制、  Channel对象的transferFrom()和transferTo()方法、 -->这种方式也是使用直接缓冲区、
     *
     *
     *   通道的分散(Scatter)和聚集(Gather):-->类似多线程、
     *
     *        分散读取：从Channel中读取的数据"分散"到多个Buffer中、  按照缓冲区的顺序,从Channel中读取的数据 依次 将Buffer填满、
     *        聚集写入：将多个缓冲区的数据聚集到Channel中、
     * */

    /*
        //NIO中的Charset对象、

        Charset charset = Charset.forName("UTF-8");//创建字符集、
        System.out.println("charset.name() = " + charset.name());
        ByteBuffer encode = charset.encode("你好");
        CharBuffer decode = charset.decode(encode);
    */

    /**
     * 利用Channel完成文件复制、--->非直接缓冲区
     */
    @Test
    public void method() throws Exception {
        String BASE_PATH = "C:\\Users\\Administrator\\Desktop\\NIO\\";
        FileInputStream inputStream = new FileInputStream(BASE_PATH + "1.jpg");
        FileOutputStream outputStream = new FileOutputStream(BASE_PATH + "2.jpg");
        //1、通过IO流对象获取Channel
        FileChannel inChannel = inputStream.getChannel();
        FileChannel outChannel = outputStream.getChannel();
        //2、分配缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        //3、将Channel中的数据,读取到缓冲区、
        while (inChannel.read(buffer) != -1) {//channel中无法读取到数据,就返回-1;

            buffer.flip();//切换为读取数据的模式、
            //4、将缓冲区中的数据写入Channel、
            outChannel.write(buffer);
            buffer.clear();//清空缓冲区、
        }
        outChannel.close();
        inChannel.close();
        outputStream.close();
        inputStream.close();
    }

    /**
     * 利用直接缓冲区完成文件复制、
     */
    @Test
    public void method2() throws Exception {
        String BASE_PATH = "C:\\Users\\Administrator\\Desktop\\NIO\\";
        //1、通过open()方法获取Channel、
        /*
         *   Paths：与Files都是NIO的工具类、
         * */
        FileChannel inChannel = FileChannel.open(Paths.get(BASE_PATH, "1.jpg"));//拼接路径、也可以直接给全路径、
        FileChannel outChannel = FileChannel.open(Paths.get(BASE_PATH + "3.jpg"), StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);//拼接路径、也可以直接给全路径、

        //2、分配缓冲区-->直接缓冲区(内存映射文件的方式获取)
        MappedByteBuffer inBuffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());//inChannel大小的直接缓冲区、
        //outChannel需要对应写的模式、只有READ_WRITE---> TODO:如果outChannel没有正确配置OpenOption、抛出异常。(至少需要配置、READ,WRITE,CREATE)
        MappedByteBuffer outBuffer = outChannel.map(FileChannel.MapMode.READ_WRITE, 0, inChannel.size());

        //3、直接对缓冲区进行数据读写操作
        byte[] bytes = new byte[inBuffer.limit()];
        inBuffer.get(bytes);
        outBuffer.put(bytes);

        inChannel.close();
        outChannel.close();
    }

    /**
     * 通道之间的数据传输方式完成文件复制、  Channel对象的transferFrom()和transferTo()方法、
     * <p>
     * 这种方式也是使用的直接缓冲区、
     *
     * @throws Exception
     */
    @Test
    public void method3() throws Exception {
        String BASE_PATH = "C:\\Users\\Administrator\\Desktop\\NIO\\";
        //1、通过open()方法获取Channel、
        /*
         *   Paths：与Files都是NIO的工具类、
         * */
        FileChannel inChannel = FileChannel.open(Paths.get(BASE_PATH, "1.jpg"));//拼接路径、也可以直接给全路径、
        FileChannel outChannel = FileChannel.open(Paths.get(BASE_PATH + "5.jpg"), StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);//拼接路径、也可以直接给全路径、

        //使用transferTo()和transferFrom()的方式 --->更简单、
//        inChannel.transferTo(0, inChannel.size(), outChannel);
        outChannel.transferFrom(inChannel, 0, inChannel.size());

        inChannel.close();
        outChannel.close();

    }

    /**
     * 分散读取和聚集写入、(都是针对Buffer数组操作)
     *
     * @throws Exception
     */
    @Test
    public void method4() throws Exception {
        String BASE_PATH = "C:\\Users\\Administrator\\Desktop\\NIO\\";
        RandomAccessFile accessFile = new RandomAccessFile(BASE_PATH + "卢先锋-Java简历.doc", "rw");
        FileChannel channel = accessFile.getChannel();

        //分配多个缓冲区
        ByteBuffer buffer1 = ByteBuffer.allocate(100);
        ByteBuffer buffer2 = ByteBuffer.allocate(1024);


        ByteBuffer[] buffers = {buffer1, buffer2};//多个Buffer对象、


        //分散读取
        channel.read(buffers);

        System.out.println(new String(buffers[0].array(), 0, buffers[0].limit(), StandardCharsets.UTF_8));
        System.out.println(" ======== ");
        System.out.println(new String(buffers[1].array(), 0, buffers[1].limit(), StandardCharsets.UTF_8));

        //写之前需要执行flip()方法、
        for (ByteBuffer buffer : buffers) {
            buffer.flip();
        }

        RandomAccessFile outFile = new RandomAccessFile(BASE_PATH + "Java简历.doc", "rw");
        FileChannel outFileChannel = outFile.getChannel();
        //聚集写入、
        outFileChannel.write(buffers);
    }

    /**
     * 测试 AsyncChannel的使用、TODO: 同时执行两次、第一次成功,第二次失败.不知道为什么。
     */
    @Test
    public void method5() throws IOException {
        System.out.println("start LocalTime.now() = " + LocalTime.now());
        String BASE_PATH = "D:\\Java视频\\千锋Java教程：微信支付\\1-7\\";
        AsynchronousFileChannel asyncChannel = AsynchronousFileChannel.open(Paths.get(BASE_PATH, "1.千锋Java教程：14微信支付基础1.mp4"));

        AsynchronousFileChannel asyncOutChannel = AsynchronousFileChannel.open(Paths.get(BASE_PATH, "1.千锋Java教程：14微信支付基础1-copy.mp4"),StandardOpenOption.WRITE,StandardOpenOption.READ,StandardOpenOption.CREATE);

        ByteBuffer buffer = ByteBuffer.allocateDirect((int) asyncChannel.size());//如果使用非直接缓冲区、completed()和failed都不会执行、

//        Future<Integer> integerFuture = asyncChannel.read(buffer, 0); 两种异步的方式、

        asyncChannel.read(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                System.out.println("read LocalTime.now() = " + LocalTime.now());

                attachment.flip();
//                buffer.flip();
                /*
                *   TODO: 注意：此处write数据时、不可以使用buffer、需要使用attachment,否则写数据失败、
                * */
//                asyncOutChannel.write(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                asyncOutChannel.write(attachment, 0, attachment, new CompletionHandler<Integer, ByteBuffer>() {
                    @Override
                    public void completed(Integer result, ByteBuffer attachment) {
                        System.out.println("write LocalTime.now() = " + LocalTime.now());
                        System.out.println("write completed ");
                        attachment.clear();
                    }

                    @Override
                    public void failed(Throwable exc, ByteBuffer attachment) {
                        System.out.println("write failed LocalTime.now() = " + LocalTime.now());
                    }
                });
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                System.out.println("read failed LocalTime.now() = " + LocalTime.now());
            }
        });

        System.out.println("main LocalTime.now() = " + LocalTime.now());
        asyncChannel.close();
        asyncOutChannel.close();
    }

}
