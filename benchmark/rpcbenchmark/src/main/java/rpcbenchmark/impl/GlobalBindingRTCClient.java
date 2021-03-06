/*
 * Copyright (c) 2015 Cisco Systems and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package rpcbenchmark.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.yang.gen.v1.rpcbench.payload.rev150702.GlobalRpcBenchInput;
import org.opendaylight.yang.gen.v1.rpcbench.payload.rev150702.GlobalRpcBenchInputBuilder;
import org.opendaylight.yang.gen.v1.rpcbench.payload.rev150702.GlobalRpcBenchOutput;
import org.opendaylight.yang.gen.v1.rpcbench.payload.rev150702.RpcbenchPayloadService;
import org.opendaylight.yang.gen.v1.rpcbench.payload.rev150702.payload.Payload;
import org.opendaylight.yang.gen.v1.rpcbench.payload.rev150702.payload.PayloadBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalBindingRTCClient implements RTCClient {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalBindingRTCClient.class);

    private final RpcbenchPayloadService service;
    private final AtomicLong rpcOk = new AtomicLong(0);
    private final AtomicLong rpcError = new AtomicLong(0);
    private final GlobalRpcBenchInput inVal;
    private final int inSize;

    @Override
    public long getRpcOk() {
        return rpcOk.get();
    }

    @Override
    public long getRpcError() {
        return rpcError.get();
    }

    public GlobalBindingRTCClient(final RpcConsumerRegistry registry, final int inSize) {
        if (registry != null) {
            this.service = registry.getRpcService(RpcbenchPayloadService.class);
        } else {
            this.service = null;
        }

        this.inSize = inSize;
        List<Payload> listVals = new ArrayList<>();
        for (int i = 0; i < inSize; i++) {
            listVals.add(new PayloadBuilder().setId(i).build());
        }
        inVal = new GlobalRpcBenchInputBuilder().setPayload(listVals).build();
    }

    @Override
    public void runTest(final int iterations) {
        int ok = 0;
        int error = 0;

        for (int i = 0; i < iterations; i++) {
            Future<RpcResult<GlobalRpcBenchOutput>> output = service.globalRpcBench(inVal);
            try {
                RpcResult<GlobalRpcBenchOutput> rpcResult = output.get();

                if (rpcResult.isSuccessful()) {
                    List<Payload> retVal = rpcResult.getResult().getPayload();
                    if (retVal.size() == inSize) {
                        ok++;
                    }
                    else {
                        error++;
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                error++;
                LOG.error("Execution failed: ", e);
            }
        }

        rpcOk.addAndGet(ok);
        rpcError.addAndGet(error);
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

}
