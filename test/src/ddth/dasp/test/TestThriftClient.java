package ddth.dasp.test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import ddth.dasp.servlet.thrift.DaspJsonService;
import ddth.dasp.servlet.thrift.DaspJsonService.Client;
import ddth.dasp.test.utils.Benchmark;
import ddth.dasp.test.utils.BenchmarkResult;
import ddth.dasp.test.utils.Operation;

public class TestThriftClient {
    static class ClientFactory implements PoolableObjectFactory<DaspJsonService.Client> {
        @Override
        public void activateObject(Client client) throws Exception {
            // client.getInputProtocol().getTransport().open();
        }

        @Override
        public void destroyObject(Client client) throws Exception {
            System.out.println("Destroying object...");
            client.getInputProtocol().getTransport().close();
        }

        @Override
        public Client makeObject() throws Exception {
            System.out.println("Creating new object...");
            // TTransport trans = new TFramedTransport(new TSocket("localhost",
            // 9090));
            TTransport trans = new TFramedTransport(new TSocket("10.199.5.127", 9090));
            TProtocol proto = new TBinaryProtocol(trans);
            DaspJsonService.Client client = new DaspJsonService.Client(proto);
            trans.open();
            return client;
        }

        @Override
        public void passivateObject(Client client) throws Exception {
            // client.getInputProtocol().getTransport().close();
        }

        @Override
        public boolean validateObject(Client client) {
            return true;
        }
    }

    public static void main(String[] args) throws Exception {
        int numSamples = 10000;
        int numThreads = 4;
        if (args.length > 0) {
            numSamples = Integer.parseInt(args[0]);
        }
        if (args.length > 1) {
            numThreads = Integer.parseInt(args[1]);
        }

        System.out.println("Num Samples: " + numSamples);
        System.out.println("Num Threads: " + numThreads);

        final ObjectPool<DaspJsonService.Client> pool = new GenericObjectPool<DaspJsonService.Client>(
                new ClientFactory(), 32);
        final Map<Object, Boolean> map = new ConcurrentHashMap<Object, Boolean>();

        BenchmarkResult result = new Benchmark(new Operation() {
            @Override
            public void run(int runId) {
                String moduleName = "ids";
                String functionName = "id64";
                String jsonEncodedInput = null;
                String authKey = null;
                try {
                    DaspJsonService.Client client = pool.borrowObject();
                    try {
                        Object result = client.callApi(moduleName, functionName, jsonEncodedInput,
                                authKey);
                        // if (map.containsKey(result)) {
                        // System.out.println("Was generated: " + result);
                        // } else {
                        // map.put(result, Boolean.TRUE);
                        // }
                    } finally {
                        pool.returnObject(client);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, numSamples, numThreads).run();
        System.out.println(result.summarize());
        pool.close();
    }
}