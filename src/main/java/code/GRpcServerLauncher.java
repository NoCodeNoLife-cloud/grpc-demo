package code;

// Copyright (c) 2024, NoCodeNoLife-cloud. All rights reserved.
// Author: NoCodeNoLife-cloud
// stay hungry，stay foolish
import io.grpc.*;
import io.grpc.stub.StreamObserver;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * This class represents a gRPC server. It initializes a server instance and provides methods to start and await termination.
 */
@Getter
@Slf4j
public class GRpcServerLauncher {
	private final Server server;
	private final int port;

	public GRpcServerLauncher(int port, BindableService bindableService) {
		// Initialize the server instance with the given port and service definition
		this.port = port;
		this.server = ServerBuilder.forPort(this.port).addService(bindableService).build();
	}

	/**
	 * A method to start the server.
	 *
	 * @return The server instance that has been started
	 */
	public Server start() throws IOException {
		return server.start();
	}

	/**
	 * Awaits the termination of the server within the specified time limit.
	 *
	 * @param time the time limit for the server to terminate
	 * @param unit the time unit for the time limit
	 *
	 * @throws InterruptedException if the current thread is interrupted while waiting
	 */
	public void awaitTermination(int time, TimeUnit unit) throws InterruptedException {
		server.awaitTermination(time, unit);
	}

	/**
	 * Awaits the termination of the server.
	 *
	 * @throws InterruptedException if the current thread is interrupted while waiting
	 */
	public void blockUntilShutdown() throws InterruptedException {
		if (server != null) {
			server.awaitTermination();
		}
	}

	/**
	 * A description of the entire Java function.
	 */
	public void stopNow() {
		if (server != null) {
			server.shutdown();
		}
	}

	// Add the implemented service class here
	private static class GreeterServiceImpl extends GreeterGrpc.GreeterImplBase {
		@Override
		public void sayHello(Hello.HelloRequest request, StreamObserver<Hello.HelloReply> responseObserver) {
			String greeting = new StringBuilder().append("Hello, ").append(request.getName()).append(" ").append(request.getSex()).toString();
			Hello.HelloReply helloReply = Hello.HelloReply.newBuilder().setMessage(greeting).build();

			responseObserver.onNext(helloReply);
			responseObserver.onCompleted();
		}

		@Override
		public StreamObserver<Hello.HelloRequest> sayHello2(StreamObserver<Hello.HelloReply> responseObserver) {
			return new StreamObserver<Hello.HelloRequest>() {
				private int count = 0;

				@Override
				public void onNext(Hello.HelloRequest value) {
					count++;
					log.info("Server receive: " + value.getName() + " " + value.getSex() + " " + count);
				}

				@Override
				public void onError(Throwable throwable) {
					log.error(throwable.toString());
				}

				@Override
				public void onCompleted() {
					log.info("Server completed");
					Hello.HelloReply helloReply = Hello.HelloReply.newBuilder().setMessage("have get " + count + " messages").build();

					responseObserver.onNext(helloReply);
					responseObserver.onCompleted();
					log.info("Server send: " + helloReply.getMessage());
				}
			};
		}

		@Override
		public void sayHello3(Hello.HelloRequest request, StreamObserver<Hello.HelloReply> responseObserver) {
			for (int i = 100; i < 110; i++) {
				responseObserver.onNext(Hello.HelloReply.newBuilder().setMessage("Hello#" + i).build());
			}
			responseObserver.onCompleted();
			log.info("Server send 100 - 109");
		}

		@Override
		public StreamObserver<Hello.HelloRequest> sayHello4(StreamObserver<Hello.HelloReply> responseObserver) {
			return new StreamObserver<Hello.HelloRequest>() {
				private int count = 200;

				@Override
				public void onNext(Hello.HelloRequest value) {
					log.info("Server receive: " + value.getName() + " " + value.getSex());
					responseObserver.onNext(Hello.HelloReply.newBuilder().setMessage("Hello#" + count).build());
					count++;
				}

				@Override
				public void onError(Throwable throwable) {
					log.error(throwable.toString());
				}

				@Override
				public void onCompleted() {
					responseObserver.onCompleted();
					log.info("Server completed");
				}
			};
		}
	}

	/**
	 * Entry point of the application.
	 *
	 * @param args The command line arguments.
	 */
	public static void main(String[] args) throws InterruptedException, IOException {
		GRpcServerLauncher server = new GRpcServerLauncher(50051, new GreeterServiceImpl());
		server.start();
		server.awaitTermination(100, TimeUnit.SECONDS);
	}
}