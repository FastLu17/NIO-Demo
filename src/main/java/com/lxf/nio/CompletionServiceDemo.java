package com.lxf.nio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * 使用CompletionService获取多线程返回值、
 *
 * @author 小66
 * @create 2019-08-19 8:54
 **/
public class CompletionServiceDemo {

    private static final int TIMEOUT = 300000;

    private static final TimeUnit UNIT = TimeUnit.MILLISECONDS;

    private static List<Long> sleepTime = new ArrayList<>();

    /**
     * CompletionService-->无序 获取异步任务的返回结果(先完成的任务先返回、)
     */
    private static void getResultAsync(List<String> strList) {
        long start = System.currentTimeMillis();
        // 1、创建线程池
        ExecutorService executor = Executors.newCachedThreadPool();
        // 2、创建多线程任务执行器
        CompletionService<String> completionService = new ExecutorCompletionService<>(executor);
        // 3、提交任务
        for (int i = 0; i < strList.size(); i++) {
            int finalI = i;
            completionService.submit(() -> {
                UNIT.sleep(sleepTime.get(finalI));
                return strList.get(finalI).trim().toUpperCase();
            });
        }
        // 4、获取数据
        try {
            for (int taskCount = 0; taskCount < strList.size(); taskCount++) {
                /*
                 *   CompletionService的两个方法：
                 *       take()：阻塞、
                 *       poll()：非阻塞、
                 * */
                System.out.println("future result async: " + completionService.take().get());
            }
            System.out.println("totalTime = " + (System.currentTimeMillis() - start));
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            // 5、关闭线程池
            try {
                executor.shutdownNow();
                executor.awaitTermination(TIMEOUT, UNIT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 有序 获取异步任务的返回结果、(先创建的任务,先返回)
     */
    private static void getResultSync(List<String> strList) {
        long start = System.currentTimeMillis();
        // 1、创建线程池
        ExecutorService executor = Executors.newCachedThreadPool();
        // 2、创建多线程返回值的List、
        List<Future<String>> futureList = new ArrayList<>();
        // 3、提交任务
        for (int i = 0; i < strList.size(); i++) {
            int finalI = i;
            Future<String> future = executor.submit(() -> {
                UNIT.sleep(sleepTime.get(finalI));
                return strList.get(finalI).trim().toUpperCase();
            });
            futureList.add(future);
        }
        // 4、获取数据
        try {
            for (Future<String> stringFuture : futureList) {
                while (!stringFuture.isDone() && !stringFuture.isCancelled()) {
                    UNIT.sleep(50);
                }
                System.out.println("future result sync: " + stringFuture.get());
            }
            System.out.println("totalTime = " + (System.currentTimeMillis() - start));
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            // 5、关闭线程池
            try {
                executor.shutdownNow();
                executor.awaitTermination(TIMEOUT, UNIT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        String[] arr = {"aa", "bb", "cc", "dd", "ee", "ff", "gg", "hh", "ii", "jj", "kk", "ll", "mm"};
        for (int i = 0; i < arr.length; i++) {
            long timeout = (long) (Math.random() * 3000);
            sleepTime.add(timeout);
        }
        getResultAsync(Arrays.asList(arr));
        getResultSync(Arrays.asList(arr));
    }
}