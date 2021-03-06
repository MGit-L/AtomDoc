<center>InterruptedException异常</center>
###InterruptedException异常
当阻塞方法收到中断请求的时候就会抛出InterruptedException异常。
###线程的状态
线程在一定的条件下会发生状态的改变，下面是线程的一些[状态](./picture/java-IE-1.jpg)
<strong>初始(NEW)：</strong>新建一个线程的对象，还未调用start方法
<strong>运行(RUNNABLE)：</strong>java线程中将已经准备就绪(Ready)和正在运行中(Running)的两种状态都统称为“Runnable”。准备就绪的线程会被放在线程池中等待被调用
<strong>阻塞(BLOCKED)：</strong>是因为某种的原因而放弃了CPU的使用权，暂时的停止了运行。直到线程进入准备就绪(Ready)状态才会有机会转到运行状态
<strong>等待(WAITING)：</strong>该状态的线程需要等待其他线程做出一些特定的动作（通知或者是中断）
<strong>超时等待(TIME_WAITING)：</strong>该状态和上面的等待不同，他可以在指定的时间内自行返回
<strong>终止(TERMINATED)：</strong>线程任务执行完毕
###线程阻塞
线程阻塞通常是指一个线程在执行过程中暂停，以等待某个条件的触发。
<strong>等待阻塞：</strong>运行的线程执行wait()方法，该线程会释放占用的所有资源，JVM会把该线程放入“等待池”中。进入这个状态后，是不能自动唤醒的，必须依靠其他线程调用notify()或notifyAll()方法才能被唤醒
<strong>同步阻塞：</strong>运行的线程在获取对象的同步锁时，若该同步锁被别的线程占用，则JVM会把该线程放入“锁池”中
<strong>其他阻塞：</strong>运行的线程执行sleep()或join()方法，或者发出了I/O请求时，JVM会把该线程置为阻塞状态。当sleep()状态超时、join()等待线程终止或者超时、或者I/O处理完毕时，线程重新转入就绪状态
###线程中断
若我们有一个运行中的软件，例如是杀毒软件正在全盘查杀病毒，此时我们不想让他杀毒，这时候点击取消，那么就是正在中断一个运行的线程。
每一个线程都有一个boolean类型的标志，此标志意思是当前的请求是否请求中断，默认为false。当一个线程A调用了线程B的interrupt方法时，那么线程B的是否请求的中断标志变为true。而线程B可以调用方法检测到此标志的变化。
<strong>阻塞方法：</strong>如果线程B调用了阻塞方法，如果是否请求中断标志变为了true，那么它会抛出InterruptedException异常。抛出异常的同时它会将线程B的是否请求中断标志置为false
<strong>非阻塞方法：</strong>可以通过线程B的isInterrupted方法进行检测是否请求中断标志为true还是false，另外还有一个静态的方法interrupted方法也可以检测标志。但是静态方法它检测完以后会自动的将是否请求中断标志位置为false。例如线程A调用了线程B的interrupt的方法，那么如果此时线程B中用静态interrupted方法进行检测标志位的变化的话，那么第一次为true，第二次就为false。
下面为具体的例子：
```
/**
 * @program: Test
 * @description:
 * @author: live
 * @create: 2020-03-25 10:43:00
 **/
public class InterrupTest implements Runnable{

    public void run(){
            try {
                while (true) {
                    Boolean a = Thread.currentThread().isInterrupted();
                    System.out.println("in run() - about to sleep for 20 seconds-------" + a);
                    Thread.sleep(20000);
                    System.out.println("in run() - woke up");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();//如果不加上这一句，那么cd将会都是false，因为在捕捉到InterruptedException异常的时候就会自动的中断标志置为了false
                Boolean c=Thread.interrupted();
                Boolean d=Thread.interrupted();
                System.out.println("c="+c);
                System.out.println("d="+d);
            }
    }
    public static void main(String[] args) {
        InterrupTest si = new InterrupTest();
        Thread t = new Thread(si);
        t.start();
        //主线程休眠2秒，从而确保刚才启动的线程有机会执行一段时间
        try {
            Thread.sleep(2000);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        System.out.println("in main() - interrupting other thread");
        //中断线程t
        t.interrupt();
        System.out.println("in main() - leaving");
    }
}
```
打印的参数如下：
```
in run() - about to sleep for 20 seconds-------false
in main() - interrupting other thread
in main() - leaving
c=true
d=false
```
线程可以检测到自身的标志位的变化，但是他只是一个标志，如果线程本身不处理的话，那么程序还是会执行下去。因此interrupt() 方法并不能立即中断线程，该方法仅仅告诉线程外部已经有中断请求，至于是否中断还取决于线程自己。
###InterruptedException异常的处理
有时候阻塞的方法抛出InterruptedException异常并不合适，例如在Runnable中调用了可中断的方法，因为你的程序是实现了Runnable接口，然后在<font color=red>重写Runnable接口的run方法的时候，那么子类抛出的异常要小于等于父类的异常</font>。
而在Runnable中run方法是没有抛异常的。所以此时是不能抛出InterruptedException异常。如果此时你只是记录日志的话，那么就是一个不负责任的做法，因为在捕获InterruptedException异常的时候自动的将是否请求中断标志置为了false。
至少在捕获了InterruptedException异常之后，如果你什么也不想做，那么就将标志重新置为true，以便栈中更高层的代码能知道中断，并且对中断作出响应。
捕获到InterruptedException异常后恢复中断状态
```
public class TaskRunner implements Runnable {
    private BlockingQueue<Task> queue;

    public TaskRunner(BlockingQueue<Task> queue) {
        this.queue = queue;
    }

    public void run() {
        try {
             while (true) {
                 Task task = queue.take(10, TimeUnit.SECONDS);
                 task.execute();
             }
         }
         catch (InterruptedException e) {
             // Restore the interrupted status
             Thread.currentThread().interrupt();
         }
    }
}
```
