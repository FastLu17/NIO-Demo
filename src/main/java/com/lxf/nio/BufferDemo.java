package com.lxf.nio;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

/**
 * Buffer：缓冲区、7种基本类型的缓冲区(Boolean除外)-->负责数据的存储。缓冲区就是数组、
 * 通过静态方法:allocate()获取缓冲区、
 * Buffer的三个关键属性：position、limit、capacity
 * mark()可以标记position的位置,使用reset()可以回到mark的position、
 * <p>
 * 0 <= mark <= position <= limit <= capacity
 *
 * NIO：可以创建直接缓冲区-->比非直接缓冲区少一步copy的过程、但是初始化更消耗资源、
 *      非直接缓冲区：allocate()分配缓冲区、将缓冲区建立在JVM基础上,
 *      直接缓冲区：allocateDirect()分配,将缓冲区建立在物理内存上,效率更高。
 *                  fileChannel.map(),可以返回一个MappedByteBuffer,也是直接缓冲区。
 * @author 小66
 * @create 2019-08-17 9:25
 **/
public class BufferDemo {

    @Test
    public void method() {
        //allocate()：分配缓冲区的容量、capacity
        CharBuffer buffer = CharBuffer.allocate(1024);

        System.out.println("buffer.position() = " + buffer.position());
        System.out.println("buffer.limit() = " + buffer.limit());//默认情况下,limit等于capacity
        System.out.println("buffer.capacity() = " + buffer.capacity());
        //put()：存入数据到缓冲区、
        buffer.put("Hello NIO");//put()数据后,此时position位置发生改变、
        System.out.println("buffer.position() after put() = " + buffer.position());

        //flip()：切换为读模式、(limit和position数据会改变、)
        buffer.flip();
        System.out.println("buffer.position() after flip() = " + buffer.position());
        System.out.println("buffer.limit() after flip() = " + buffer.limit());

        //获取缓冲区的数据、
        char c = buffer.get();
        System.out.println("c = " + c);
        System.out.println("buffer.position() after get() = " + buffer.position());
        System.out.println("buffer.limit() after get() = " + buffer.limit());

        //rewind()：position重新回到0、数据可以重新获取、
        buffer.rewind();
        System.out.println("buffer.get() after rewind() = " + buffer.get());
        System.out.println("buffer.position() after rewind() = " + buffer.position());
        System.out.println("buffer.limit() after rewind() = " + buffer.limit());
        System.out.println("buffer.capacity() after rewind() = " + buffer.capacity());

        //清空缓冲区、limit改变为和capacity相同、position变为0、-->数据不会清空、依然可以获取,处于"被遗忘"状态。
        buffer.clear();//TODO: 注意 compact()和clear()的区别
        System.out.println("buffer.get() after clear() = " + buffer.get());//还有数据可以获取、
        System.out.println("buffer.position() after clear() = " + buffer.position());
        System.out.println("buffer.limit() after clear() = " + buffer.limit());
        System.out.println("buffer.capacity() after clear() = " + buffer.capacity());

        /*
        *   如果Buffer中仍有未读的数据，且后续还需要这些数据，但是此时想要先写些数据，那么使用compact()方法。
            compact()方法将所有未读的数据拷贝到Buffer起始处。然后将position设到最后一个未读元素正后面。
            limit属性依然像clear()方法一样，设置成capacity。现在Buffer准备好写数据了，但是不会覆盖未读的数据。。
        * */

    }

    @Test
    public void method2() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        byteBuffer.put("abcde".getBytes());
        byteBuffer.flip();
        byte[] bytes = new byte[byteBuffer.limit()];
        byteBuffer.get(bytes, 0, 2);
        //mark()：标记位置、
        System.out.println("byteBuffer.position() = " + byteBuffer.position());
        byteBuffer.mark();

        byteBuffer.get(bytes, 2, 2);
        System.out.println("byteBuffer.position() before reset() = " + byteBuffer.position());
        //使position 回到mark()的位置、
        byteBuffer.reset();

        System.out.println("byteBuffer.position() after reset() = " + byteBuffer.position());
    }
}
