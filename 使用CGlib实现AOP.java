
AOP，全称为aspect oriented programming， 面向切面的编程，极大的丰富了在运行时方法的表现，可以在不修改源代码的基础上，丰富代码的功能。实现AOP的基本核心是通过动态代理，将


//Enhancer 是一个增强类
ConnectionExceptionHanldeProxy proxy = new ConnectionExceptionHandleProxy();
Enhancer enhancer = new Enhancer();
enhancer.setSuperclass(ServiceExecutor.class);
enhancer.setCallback(proxy);


//在cglib中，可以定义自己的代理类，只要实现 MethodInterceptor 这个接口，并将其实例设置在enhancer的setCallback方法中即可
//这里使用了回调的概念，回调是闭包的一种体现。真正的实体函数会作为参数传入intercept方法，并在该方法中实现拦截，处理只后，再调//用真正的实体方法。
public class ConnectionExceptionHandleProxy implements MethodInterceptor{
	
	private static Logger logger = Logger.getInstance(ConnectionExceptionHandleProxy.class);

	@Override
	//arg0 是调用函数的对象，在这里即为ServiceExecutor的object对象
	//arg1 是实体函数的method对象，每一次对函数的调用，都会被捕获到这里
	public Object intercept(Object arg0, Method arg1, Object[] arg2, MethodProxy arg3) throws Throwable  {
		
		CBTLogUtil.log(logger,LogLevel.INFO, "Start to execute "+arg1.getName()+ "....");
		try{
			return arg3.invokeSuper(arg0, arg2);
		}catch(ConnectionException e){
			CBTLogUtil.log(logger, LogLevel.ERROR, "Current Session invalid!");
			FastClass clazz = FastClass.create(ServiceExecutor.class);
			clazz.invoke("sessionTimeOutHandler", new Class[]{}, arg0, new Object[]{});
			CBTLogUtil.log(logger, LogLevel.INFO, "Retry ...");
			return arg3.invokeSuper(arg0, arg2);
		}
	}
	
}
