package ddth.dasp.servlet.thrift.serverfactory;

import org.apache.thrift.TProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TTransportException;

import ddth.dasp.servlet.thrift.ThriftUtils;

public class ThreadedSelectorServerFactory extends AbstractServerFactory {
    private long maxReadBufferSize;

    public ThreadedSelectorServerFactory(int port, TProcessor processor,
            int clientTimeoutMillisecs, int maxFrameSize, long maxReadBufferSize) {
        super(port, processor, clientTimeoutMillisecs, maxFrameSize);
        this.maxReadBufferSize = maxReadBufferSize;
    }

    @Override
    public TServer createServer() throws TTransportException {
        TServer server = ThriftUtils.createThreadedSelectorServer(getProcessorFactory(), getPort(),
                getClientTimeoutMillisecs(), getMaxFrameSize(), maxReadBufferSize);
        return server;
    }
}
