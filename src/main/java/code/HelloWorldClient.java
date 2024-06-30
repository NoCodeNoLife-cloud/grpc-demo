package code;

import io.grpc.*;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HelloWorldClient {
	private static final Logger logger = Logger.getLogger(HelloWorldClient.class.getName());
	private final ManagedChannel channel;
	private final GreeterGrpc.GreeterBlockingStub blockingStub;

	/** Construct client connecting to HelloWorld server at {@code host:port}. */
	public HelloWorldClient(String host, int port) {

		channel = ManagedChannelBuilder.forAddress(host, port)
				.usePlaintext()
				.build();
		blockingStub = GreeterGrpc.newBlockingStub(channel);
	}

	public void shutdown() throws InterruptedException {
		channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
	}

	/** Say hello to server. */
	public void greet(String name) {
		logger.info("Will try to greet " + name + " ...");
		Hello.HelloRequest request = Hello.HelloRequest.newBuilder().setName(name).setSex(" 女").build();
		Hello.HelloReply response;
		try {
			response = blockingStub.sayHello(request);
		} catch (StatusRuntimeException e) {
			logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
			return;
		}
		logger.info("Greeting: " + response.getMessage());
	}

	/**
	 * Greet server. If provided, the first element of {@code args} is the name to use in the
	 * greeting.
	 */
	public static void main(String[] args) throws Exception {
		HelloWorldClient client = new HelloWorldClient("localhost", 50051);
		try {

			String user = "world";
			if (args.length > 0) {
				user = args[0];
			}
			client.greet(user);
		} finally {
			client.shutdown();
		}
	}
}