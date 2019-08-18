package com.lxf.nio;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


public class AsynchronousFileChannelTest {
    static final String BASE_PATH = "D:\\Java视频\\千锋Java教程：微信支付\\1-7\\";

    @Test
    public void asyncRead() throws Exception {
        Path path = Paths.get(BASE_PATH, "3.千锋Java教程：14微信支付基础3.mp4");
        AsynchronousFileChannel afc = AsynchronousFileChannel.open(path, StandardOpenOption.READ);

        Path outPath = Paths.get(BASE_PATH, "3.千锋Java教程：14微信支付基础3-copy.mp4");
        AsynchronousFileChannel outChannel = AsynchronousFileChannel.open(outPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        //非直接缓冲区(allocate):无法正常写出数据、(感觉和源文件的大小有关系、)
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect((int) afc.size());//此处的capacity需要与inChannel的size()相同、否则无法进行循环读取、

        afc.read(byteBuffer, 0, byteBuffer, new CompletionHandler<Integer, ByteBuffer>() {

            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                System.out.println("read attachment.limit() = " + attachment.limit());
                System.out.println("read completed = " + result);
//                    System.out.println("attachment = " + new String(attachment.array(),0,result));
                try {
                    asyncWrite(outChannel, attachment);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                System.out.println("read failed = " + exc);
            }

        });

        //TODO: 不间隔一段时间,通过main方法的方式调用asyncWrite()方法不会正常执行(没有数据写出)、但是也不抛出异常
//        Thread.sleep(5000);
        afc.close();
    }

    public void asyncWrite(AsynchronousFileChannel outChannel, ByteBuffer byteBuffer) throws Exception {

        byteBuffer.flip();
        outChannel.write(byteBuffer, 0, byteBuffer, new CompletionHandler<Integer, ByteBuffer>() {

            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                System.out.println("write attachment.limit() = " + attachment.limit());
                System.out.println("write completed = " + result);
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                System.out.println("write failed = " + exc);
            }

        });

        outChannel.close();
    }

    public static void main(String[] args) throws Exception {
        new AsynchronousFileChannelTest().asyncRead();
    }
}
