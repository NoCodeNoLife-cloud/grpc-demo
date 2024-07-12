package code;

// Copyright (c) 2024, NoCodeNoLife-cloud. All rights reserved.
// Author: NoCodeNoLife-cloud
// stay hungry，stay foolish
import io.grpc.GreeterGrpc;
import io.grpc.Hello;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * GRpcClient is a class that represents a client for a gRPC service.
 * It uses the ManagedChannel from the io.grpc package to communicate with the server.
 */
@Slf4j
@Getter
public class GRpcClientLauncher {
	private final ManagedChannel managedChannel;
	private final String name;
	private final int port;
	private final GreeterGrpc.GreeterBlockingStub greeterBlockingStub;
	private final GreeterGrpc.GreeterStub greeterFutureStub;

	/**
	 * Constructs a new GRpcClient instance with the specified server name and port.
	 *
	 * @param name The name of the server to connect to.
	 * @param port The port number to connect to the server.
	 *
	 * @throws InterruptedException If there is an error creating the managed channel.
	 */
	public GRpcClientLauncher(String name, int port) throws InterruptedException {
		// Set the server name
		this.name = name;

		// Set the port number
		this.port = port;

		// Create a new ManagedChannel with the specified server address and port number
		// The channel is configured to use plaintext
		this.managedChannel = ManagedChannelBuilder.forAddress(this.name, this.port).usePlaintext().build();

		// Create a new blocking-style stub for the Greeter service
		greeterBlockingStub = GreeterGrpc.newBlockingStub(managedChannel);

		// Create a new stub for the Greeter service
		greeterFutureStub = GreeterGrpc.newStub(managedChannel);
	}

	/**
	 * Constructs a new GRpcClient instance with the specified port number.
	 *
	 * @param port The port number to connect to the server.
	 *
	 * @throws IOException If there is an error creating the managed channel.
	 */
	public GRpcClientLauncher(int port) {
		// Set the server name to "127.0.0.1"
		this.name = "127.0.0.1";

		// Set the port number
		this.port = port;

		// Create a new ManagedChannel with the specified server address and port number
		// The channel is configured to use plaintext
		this.managedChannel = ManagedChannelBuilder.forAddress(this.name, this.port).usePlaintext().build();

		// Create a new blocking-style stub for the Greeter service
		greeterBlockingStub = GreeterGrpc.newBlockingStub(managedChannel);

		// Create a new stub for the Greeter service
		greeterFutureStub = GreeterGrpc.newStub(managedChannel);
	}

	/**
	 * Waits for the channel to terminate within the given time limit.
	 *
	 * @param time the time to wait for the channel to terminate
	 * @param unit the time unit for the time parameter
	 *
	 * @throws InterruptedException if the current thread is interrupted while waiting
	 */
	public void awaitTermination(int time, TimeUnit unit) throws InterruptedException {
		// Waits for the channel to terminate within the given time limit.
		managedChannel.awaitTermination(time, unit);
	}

	/**
	 * Entry point of the application.
	 *
	 * @param args The command line arguments.
	 */
	public static void main(String[] args) throws ExecutionException, InterruptedException {
		// Simple RPC
		log.info("Simple RPC");
		GRpcClientLauncher client = new GRpcClientLauncher(50051);
		Hello.HelloRequest request = Hello.HelloRequest.newBuilder().setName("hello").setSex("man").build();
		Hello.HelloReply reply = client.greeterBlockingStub.sayHello(request);
		log.info("Greeting: " + reply.getMessage());

		TimeUnit.SECONDS.sleep(1);

		// Client streaming rpc
		log.info("Client streaming rpc");
		StreamObserver<Hello.HelloReply> helloReplyStreamObserver = new StreamObserver<Hello.HelloReply>() {
			@Override
			public void onNext(Hello.HelloReply helloReply) {
				log.info("Client receive: " + helloReply.getMessage());
			}

			@Override
			public void onError(Throwable throwable) {
				log.error(throwable.toString());
			}

			@Override
			public void onCompleted() {
				log.info("Client completed");
			}
		};
		StreamObserver<Hello.HelloRequest> helloRequestStreamObserver = client.greeterFutureStub.sayHello2(helloReplyStreamObserver);
		for (int i = 0; i < 10; ++i) {
			helloRequestStreamObserver.onNext(Hello.HelloRequest.newBuilder().setName("Hello#" + i).setSex("man").build());
		}
		helloRequestStreamObserver.onCompleted();

		TimeUnit.SECONDS.sleep(1);

		// Server streaming rpc
		log.info("Server streaming rpc");
		Iterator<Hello.HelloReply> helloReplyIterator = client.greeterBlockingStub.sayHello3(Hello.HelloRequest.newBuilder().setName("hello").setSex("man").build());
		while (helloReplyIterator.hasNext()) {
			Hello.HelloReply helloReply = helloReplyIterator.next();
			log.info("Greeting: " + helloReply.getMessage());
		}
		log.info("Client receive 100 - 109");

		TimeUnit.SECONDS.sleep(1);

		// Bi-directional rpc
		log.info("Bi-directional rpc");
		StreamObserver<Hello.HelloReply> helloReplyStreamObserver2 = new StreamObserver<Hello.HelloReply>() {
			@Override
			public void onNext(Hello.HelloReply helloReply) {
				log.info("Client receive: " + helloReply.getMessage());
			}

			@Override
			public void onError(Throwable throwable) {
				log.error(throwable.toString());
			}

			@Override
			public void onCompleted() {
				log.info("Client completed");
			}
		};
		StreamObserver<Hello.HelloRequest> helloRequestStreamObserver2 = client.greeterFutureStub.sayHello4(helloReplyStreamObserver2);
		for (int i = 300; i < 310; ++i) {
			helloRequestStreamObserver2.onNext(Hello.HelloRequest.newBuilder().setName("Hello#" + i).setSex("man").build());
		}
		helloRequestStreamObserver2.onCompleted();

		TimeUnit.SECONDS.sleep(1);
	}
}
