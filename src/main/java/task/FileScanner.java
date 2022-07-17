package task;

import app.FileMeta;
import callback.FileScannerCallback;
import lombok.Getter;

import java.io.File;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class FileScanner {

    // 被扫描目录下文件的个数
    private AtomicInteger fileNum = new AtomicInteger();

    // 被扫描目录下(包括被扫描目录)的文件夹个数
    private AtomicInteger dirNum = new AtomicInteger(1);

    // 所有在扫描文件的子线程个数，只有当子线程为0时，主线程才恢复执行
    private AtomicInteger threadCount = new AtomicInteger();

    // 当最后一个子线程执行完任务之后，再调用countDown方法唤醒主线程
    private CountDownLatch latch = new CountDownLatch(1);

    // 这里设置为0个可用资源
    private Semaphore semaphore = new Semaphore(0);

    // 获取当前电脑的CPU个数
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    // 使用线程池创建对象
    private ThreadPoolExecutor pool = new ThreadPoolExecutor(CPU_COUNT, CPU_COUNT * 2, 10,
            TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.AbortPolicy());

    // 文件扫描回调对象
    private FileScannerCallback callback;

    public FileScanner(FileScannerCallback callback) {
        this.callback = callback;
    }

    // 文件扫描的结果集，存放被扫描目录下的所有文件(包括目录)
    //List<FileMeta> fileMetas = new ArrayList<>();

    /**
     * 传入目录文件，扫描目录下的所有文件
     * @param dir
     * 选择要扫描的菜单之后，第一个要执行的方法，所有文件夹和文件的具体扫描工作交给子线程
     * 主线程即scan方法需要等待所有子线程全部扫描结束之后再恢复执行
     */
    public void scan(File dir) {
        System.out.println("开始扫描文件，根目录为：" + dir);
        long start = System.nanoTime();
        // 将具体的扫描任务交给子线程处理
        scanInternal(dir);
        // 统计根目录扫描的线程
//        threadCount.incrementAndGet();
        try {
            //latch.await();

            // 可用资源为0，只要调用acquire就会阻塞，否则获取到资源之后，就会直接执行下面代码，关闭线程池了
            semaphore.acquire();
        } catch (InterruptedException e) {
            System.out.println("扫描任务中断，根目录为：" + dir);
        }finally {
            System.out.println("关闭线程池...");
            // 当所有线程执行结束走到这里，就是正常的关闭线程
            // 当线程被中断后走到这里，需要立即停止正在扫描的所有线程
            pool.shutdownNow();
        }
        long end = System.nanoTime();
        System.out.println("扫描任务结束，共耗时：" + (end - start) * 1.0 / 100_0000 + "ms");
        System.out.println("扫描任务结束，根目录为：" + dir);
        System.out.println("共扫描到" + fileNum + "个文件");
        System.out.println("共扫描到" + dirNum + "个文件夹");
    }

    /**
     * 具体扫描任务的递归方法
     * @param dir
     */
    private void scanInternal(File dir) {
        if(dir == null) {
            return;
        }
        threadCount.incrementAndGet();
        pool.submit(() -> {
            // 使用回调函数，将当前目录下的所有内容保存到指定终端
            this.callback.callback(dir);
            // 先将当前这一级目录下的file对象获取出来
            File[] files = dir.listFiles();
            // 遍历files下的所有文件
            for(File file : files) {
                FileMeta fileMeta = new FileMeta();
                if(file.isDirectory()) {
                    // 如果文件是个目录，就继续递归扫描目录
                    dirNum.incrementAndGet();// 等同于++i
                    // 将子文件夹的任务交给新线程处理
//                    threadCount.incrementAndGet();
                    scanInternal(file);
                }else {
                    // 是普通文件
                    fileNum.incrementAndGet();
                }
            }
            // 走完for循环，当前目录下的文件扫描和保存已经完成
            System.out.println(Thread.currentThread().getName() + "扫描：" + dir + "任务结束");
            // 当前线程结束之后，让threadCount减1
            threadCount.decrementAndGet();

            // 每个线程结束任务之后判断一下当前线程数量
            // 如果为0，说明其他线程都已经扫描完成了，当前线程时最后一个线程
            if(threadCount.get() == 0) {
                System.out.println("所有扫描任务结束");
                // 调用countDown方法唤醒主线程
                //latch.countDown();

                // 调用release方法释放资源
                semaphore.release();
            }
        });
    }
}
