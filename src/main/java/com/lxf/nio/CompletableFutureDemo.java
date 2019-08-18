package com.lxf.nio;

import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * @author 小66
 * @create 2019-08-18 15:05
 **/
public class CompletableFutureDemo {
    private static Random rand = new Random();
    private static long t = System.currentTimeMillis();

    static int getMoreData() {
        System.out.println("begin to start compute");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("end to start compute. passed " + (System.currentTimeMillis() - t) / 1000 + " seconds");
        return rand.nextInt(1000);
    }

    //main方法中,线程池不会自动结束、但是在@Test方法下,线程池会自动结束、
    public static void main(String[] args) throws Exception {
        /**
         *  TODO: 这样使用、整个main线程不会结束、为什么？--->线程池未关闭、
         */
        ExecutorService pool = Executors.newCachedThreadPool();

        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(CompletableFutureDemo::getMoreData, pool);

        pool.execute(() -> future.whenComplete((v, e) -> {
            if (e == null) {
                try {
                    FileChannel channel = FileChannel.open(Paths.get("C:\\Users\\Administrator\\Desktop\\NIO\\async.txt"), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
                    ByteBuffer buffer = ByteBuffer.wrap("Hello".getBytes());
                    channel.write(buffer);
                    channel.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                System.out.println("v = " + v);//这里可以对getMoreData()的返回值进行相关的业务操作、
                pool.shutdown();//TODO: 不需要主动关闭线程池、
            } else {
                throw new RuntimeException(e);
            }
        }));

        /**
         *  TODO: 这样使用,main会结束、而且看不到异步输出的结果。  -->是否是获取CompletableFuture对象时,需要指定线程池、
         */
//        future.whenCompleteAsync((integer, throwable) -> {//是执行把 whenCompleteAsync 这个任务继续提交给线程池来进行执行。;
//            if (throwable == null)
//                System.out.println("v = " + integer);//这里可以对getMoreData()的返回值进行相关的业务操作、
//            else
//                throw new RuntimeException(throwable);
//        });

//        Executors.newCachedThreadPool().execute(CompletableFutureDemo::getMoreData);//虽然可以异步调用,但是无法获取到getMoreData()方法的返回值,进行后续的操作、

        System.out.println("main....");

        //如果主动执行shutdown()、同样看不到异步输出的结果、
//        pool.shutdown();

        System.out.println("pool.isShutdown() = " + pool.isShutdown());

    }


    @Test
    public void processFuture() throws InterruptedException {
        /*
         *   TODO: 不可以使用线程池的方式来获取CompletableFuture对象、  Future对象是顶级父类、会出现ClassCastException
         * */
//        Future<String> stringFuture = Executors.newCachedThreadPool().submit(() -> {
//            TimeUnit.SECONDS.sleep(3);
//            return "AAA";
//        });
//        CompletableFuture<String> completableFuture = (CompletableFuture<String>) supplyAsync; // ClassCastException

        CompletableFuture<String> supplyAsync = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "AAA";
        });
        //线程池在@Test方法下,会自动执行shutdown()方法、
        Executors.newCachedThreadPool().execute(() -> {
            supplyAsync.whenCompleteAsync((s, throwable) -> {
                if (throwable == null) {
                    try {
                        FileChannel channel = FileChannel.open(Paths.get("C:\\Users\\Administrator\\Desktop\\NIO\\async.txt"), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
                        ByteBuffer buffer = ByteBuffer.wrap("Hello".getBytes());
                        channel.write(buffer);
                        channel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });

        //TODO: 如果不暂停一段时间,主线程直接结束,  异步任务不能正常完成、  为什么？？？-->线程池shutdown了？
//        Thread.sleep(5000);
    }

    /**
     * 监听当前CompletableFuture对象是否计算完成、(监听异步任务是否完成)
     * 可以处理异常、
     *
     * @throws Exception
     */
    @Test
    public void whenComplete() throws Exception {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            double random = Math.random();
            if (random < 0.5) {
                int i = 12 / 0;
            }
            System.out.println("run end ...");
        });

        future.whenComplete((integer, throwable) -> {
            if (throwable == null) {
                System.out.println("v = " + integer);
            } else {
                System.out.println("throwable = " + throwable);
                throw new RuntimeException(throwable);
            }
        });

        future.exceptionally(t -> {
            System.out.println("执行失败！" + t.getMessage());
            return null;
        });

        Thread.sleep(2000);
    }

    /**
     * 当一个线程依赖另一个线程时，可以使用 thenApply 方法来把这两个线程串行化。
     * <p>
     * thenApply()依赖上一个CompletableFuture的返回值、
     */
    @Test
    public void thenApply() throws Exception {
        CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
            long result = new Random().nextInt(100);
            System.out.println("result1=" + result);
            return result;
        }).thenApply(t -> {
            long result = t * 5;
            System.out.println("result2=" + result);
            return result;
        });

        long result = future.get();
        System.out.println(result);
    }

    /**
     * handle()方法：接收上一个CompletableFuture的返回值, 并且可以处理上一个CompletableFuture的异常、
     * <p>
     * handle 是在任务完成后再执行，可以处理异常的任务。
     * thenApply 只可以执行正常的任务，任务出现异常则不执行 thenApply 方法。
     */
    @Test
    public void handle() throws Exception {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            int i = 10 / 0;
            return i;
        }).handle((param, throwable) -> {
            int result;
            if (throwable == null) {
                result = param * 2;
            } else {
                result = 0;
                System.out.println(throwable.getMessage());
            }
            return result;
        });
        System.out.println(future.get());
    }

    /**
     * 接收上一个CompletableFuture返回值(计算结果)，并消费处理，无返回结果。
     *
     * @throws Exception
     */
    @Test
    public void thenAccept() throws Exception {
        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> new Random().nextInt(10))
                .thenAccept(integer -> {
                    System.out.println("integer = " + integer);
                    System.out.println("integer*3 = " + integer * 3);
                });

        Void aVoid = future.get();
        System.out.println("aVoid = " + aVoid);
    }


    /**
     * thenRun()：不关心计算结果,只要上一个CompletableFuture执行完成,就开始执行thenAccept。
     * <p>
     * public CompletionStage<Void
     *
     * @throws Exception
     */
    @Test
    public void thenRun() throws Exception {
        CompletableFuture<Integer> supplyAsync = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(5000);
                System.out.println("supplyAsync ...");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return new Random().nextInt(10);
        });

        //thenRun()：会在上一个CompletableFuture异步任务执行完成后,才开始执行、
        supplyAsync.thenRun(() -> {
            try {
                System.out.println("supplyAsync.get() = " + supplyAsync.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("thenRun ...");
        });

        Thread.sleep(6000);
    }

    /**
     * thenCombine(合并任务): 会把 两个 CompletionStage 的任务都执行完成后，把两个任务的结果一块交给 thenCombine() 来处理。
     *
     * @throws Exception
     */
    @Test
    public void thenCombine() throws Exception {
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> "Hello");
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> "Future");

        future1.thenCombine(future2, (t, u) -> t + "-" + u)
                .thenAccept(s -> System.out.println("s = " + s));
    }

    /**
     * thenAcceptBoth：同时消耗两个资源(返回结果)
     * <p>
     * 当两个CompletionStage都执行完成后，把结果一块交给thenAcceptBoth来进行消耗
     *
     * @throws Exception
     */
    @Test
    public void thenAcceptBoth() throws Exception {
        CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(() -> {
            int t = new Random().nextInt(3);
            try {
                TimeUnit.SECONDS.sleep(t);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("f1=" + t);
            return t;
        });

        CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(() -> {
            int t = new Random().nextInt(3);
            try {
                TimeUnit.SECONDS.sleep(t);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("f2=" + t);
            return t;
        });

        f1.thenAcceptBoth(f2, new BiConsumer<Integer, Integer>() {
            @Override
            public void accept(Integer t, Integer u) {
                System.out.println("f1=" + t + ";f2=" + u + ";");
            }
        });

        TimeUnit.SECONDS.sleep(5);
    }
}
