package com.guosen.zebra.core.grpc.client.stream;

import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.stub.ClientResponseObserver;
import io.grpc.stub.StreamObserver;

public class StreamObserverToCallListenerAdapter<Request, Response> extends ClientCall.Listener<Response> {
	private final StreamObserver<Response> observer;
	private final CallToStreamObserverAdapter<Request, Response> adapter;
	private final boolean streamingResponse;
	private boolean firstResponseReceived;

	StreamObserverToCallListenerAdapter(StreamObserver<Response> observer,
			CallToStreamObserverAdapter<Request, Response> adapter, boolean streamingResponse) {
		this.observer = observer;
		this.streamingResponse = streamingResponse;
		this.adapter = adapter;
		if (observer instanceof ClientResponseObserver) {
			@SuppressWarnings("unchecked")
			ClientResponseObserver<Request, Response> clientResponseObserver = (ClientResponseObserver<Request, Response>) observer;
			clientResponseObserver.beforeStart(adapter);
		}
		adapter.freeze();
	}

	@Override
	public void onHeaders(Metadata headers) {
	}

	@Override
	public void onMessage(Response message) {
		if (firstResponseReceived && !streamingResponse) {
			throw Status.INTERNAL.withDescription("More than one responses received for unary or client-streaming call")
					.asRuntimeException();
		}
		firstResponseReceived = true;
		observer.onNext(message);
		if (streamingResponse && adapter.isAutoFlowControlEnabled()) {
			adapter.request(1);
		}
	}

	@Override
	public void onClose(Status status, Metadata trailers) {
		if (status.isOk()) {
			observer.onCompleted();
		} else {
			observer.onError(status.asRuntimeException(trailers));
		}
	}

	@Override
	public void onReady() {
		Runnable runnable = adapter.getOnReadyHandler();
		if (runnable != null) {
			runnable.run();
		}
	}
}
