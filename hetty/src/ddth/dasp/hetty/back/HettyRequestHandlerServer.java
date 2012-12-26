package ddth.dasp.hetty.back;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddth.dasp.common.DaspGlobal;
import ddth.dasp.common.osgi.IOsgiBootstrap;
import ddth.dasp.hetty.framework.IRequestActionHandler;
import ddth.dasp.hetty.message.HettyProtoBuf;
import ddth.dasp.hetty.message.IRequestParser;
import ddth.dasp.hetty.message.ResponseUtils;
import ddth.dasp.hetty.qnt.IQueueReader;
import ddth.dasp.hetty.qnt.ITopicPublisher;

public class HettyRequestHandlerServer {

    private final Logger LOGGER = LoggerFactory.getLogger(HettyRequestHandlerServer.class);

    private IQueueReader queueReader;
    private ITopicPublisher topicPublisher;
    private long readTimeoutMillisecs = 10000, writeTimeoutMillisecs = 10000;
    private IRequestParser requestParser;
    private int numWorkers = Runtime.getRuntime().availableProcessors();
    private Thread[] workerThreads;

    public HettyRequestHandlerServer() {
    }

    public IQueueReader getQueueReader() {
        return queueReader;
    }

    public HettyRequestHandlerServer setQueueReader(IQueueReader queueReader) {
        this.queueReader = queueReader;
        return this;
    }

    public ITopicPublisher getTopicPublisher() {
        return topicPublisher;
    }

    public HettyRequestHandlerServer setTopicPublisher(ITopicPublisher topicPublisher) {
        this.topicPublisher = topicPublisher;
        return this;
    }

    public long getReadTimeoutMillisecs() {
        return readTimeoutMillisecs;
    }

    public HettyRequestHandlerServer setReadTimeoutMillisecs(long readTimeoutMillisecs) {
        this.readTimeoutMillisecs = readTimeoutMillisecs;
        return this;
    }

    public long getWriteTimeoutMillisecs() {
        return writeTimeoutMillisecs;
    }

    public HettyRequestHandlerServer setWriteTimeoutMillisecs(long writeTimeoutMillisecs) {
        this.writeTimeoutMillisecs = writeTimeoutMillisecs;
        return this;
    }

    public IRequestParser getRequestParser() {
        return requestParser;
    }

    public HettyRequestHandlerServer setRequestParser(IRequestParser requestParser) {
        this.requestParser = requestParser;
        return this;
    }

    public int getNumWorkers() {
        return numWorkers;
    }

    public HettyRequestHandlerServer setNumWorkers(int numWorkers) {
        this.numWorkers = numWorkers;
        return this;
    }

    protected void handleRequest(HettyProtoBuf.Request requestProtobuf) {
        String module = requestParser.getModule(requestProtobuf);
        String action = requestParser.getAction(requestProtobuf);
        Map<String, String> filter = new HashMap<String, String>();
        filter.put(IRequestActionHandler.FILTER_KEY_MODULE, module != null ? module : "");
        filter.put(IRequestActionHandler.FILTER_KEY_ACTION, action != null ? action : "");
        IOsgiBootstrap osgiBootstrap = DaspGlobal.getOsgiBootstrap();
        IRequestActionHandler handler = osgiBootstrap.getService(IRequestActionHandler.class,
                filter);
        if (handler == null) {
            filter.remove(IRequestActionHandler.FILTER_KEY_ACTION);
            handler = osgiBootstrap.getService(IRequestActionHandler.class, filter);
        }
        if (handler != null) {
            handler.handleRequest(requestProtobuf, topicPublisher);
        } else {
            HettyProtoBuf.Response responseProtobuf = ResponseUtils.response404(requestProtobuf);
            topicPublisher.publishToTopic(responseProtobuf, writeTimeoutMillisecs,
                    TimeUnit.MILLISECONDS);
        }

        // QueryStringDecoder queryStringDecoder = new
        // QueryStringDecoder(requestProtobuf.getUri());
        // Map<String, List<String>> urlParams =
        // queryStringDecoder.getParameters();
        // String path = queryStringDecoder.getPath();
        // String[] tokens = queryStringDecoder.getPath().replaceAll("^\\/+",
        // "")
        // .replaceAll("\\/+$", "").split("\\/");
        //
        // HettyProtoBuf.Response.Builder responseBuilder =
        // HettyProtoBuf.Response.newBuilder();
        // responseBuilder.setRequestId(requestProtobuf.getId());
        // responseBuilder.setDuration(System.currentTimeMillis() -
        // requestProtobuf.getTimestamp());
        // responseBuilder.setChannelId(requestProtobuf.getChannelId());
        // responseBuilder.setContent(ByteString.copyFromUtf8(requestProtobuf.toString()));
        // return responseBuilder.build();
    }

    public void destroy() {
        for (Thread workerThread : workerThreads) {
            try {
                workerThread.interrupt();
            } catch (Exception e) {
            }
        }
    }

    public void start() {
        workerThreads = new Thread[numWorkers];
        for (int i = 1; i <= numWorkers; i++) {
            Thread t = new Thread(HettyRequestHandlerServer.class.getName() + " - " + i) {
                public void run() {
                    while (!isInterrupted()) {
                        Object obj = queueReader.readFromQueue(readTimeoutMillisecs,
                                TimeUnit.MILLISECONDS);
                        if (obj != null && obj instanceof byte[]) {
                            try {
                                HettyProtoBuf.Request requestProtobuf = HettyProtoBuf.Request
                                        .parseFrom((byte[]) obj);
                                handleRequest(requestProtobuf);
                            } catch (Exception e) {
                                LOGGER.error(e.getMessage(), e);
                            }
                        }
                    }
                }
            };
            t.setDaemon(true);
            t.start();
            workerThreads[i - 1] = t;
        }
    }
}