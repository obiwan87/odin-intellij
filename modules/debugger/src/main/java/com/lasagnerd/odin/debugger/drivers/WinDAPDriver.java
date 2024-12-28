/*
 * Copyright 2023-2024 FalsePattern
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lasagnerd.odin.debugger.drivers;

import com.intellij.execution.ExecutionException;
import com.intellij.util.system.CpuArch;
import com.jetbrains.cidr.ArchitectureType;
import com.lasagnerd.odin.debugger.drivers.dap.DAPDebuggerDriverConfiguration;
import com.lasagnerd.odin.debugger.drivers.dap.DAPDriver;
import com.lasagnerd.odin.debugger.drivers.dap.WrappedDebugServer;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.lsp4j.debug.Capabilities;
import org.eclipse.lsp4j.debug.OutputEventArguments;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.eclipse.lsp4j.debug.util.ToStringBuilder;
import org.eclipse.lsp4j.jsonrpc.JsonRpcException;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.MessageIssueException;
import org.eclipse.lsp4j.jsonrpc.debug.messages.DebugResponseMessage;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class WinDAPDriver extends DAPDriver<
        IDebugProtocolServer, WrappedDebugServer<IDebugProtocolServer>,
        WinDAPDriver.WinDAPDebuggerClient
        > {
    private final CompletableFuture<HandshakeResponse> handshakeFuture = new CompletableFuture<>();
    private final HandshakeStrategy handshakeStrategy;

    public WinDAPDriver(HandshakeStrategy handshakeStrategy,
                        @NotNull Handler handler,
                        DAPDebuggerDriverConfiguration config,
                        DebuggerLanguage language) throws ExecutionException {
        super(handler, config, language);
        this.handshakeStrategy = handshakeStrategy;
        DAPDriver$postConstructor();
    }

    @Override
    protected @NotNull String getClientName() {
        return "Odin";
    }

    @Override
    protected @NotNull String getClientID() {
        return "win-odin";
    }

    @Override
    public void DAPDriver$postConstructor$invoke() {

    }

    @Override
    protected MessageConsumer wrapMessageConsumer(MessageConsumer messageConsumer) {
        return new MessageConsumer() {
            private boolean verifyHandshake = true;

            @Override
            public void consume(Message message) throws MessageIssueException, JsonRpcException {
                if (verifyHandshake && message instanceof DebugResponseMessage res && res.getMethod().equals("handshake")) {
                    verifyHandshake = false;
                    res.setResponseId(1);
                }
                messageConsumer.consume(message);
            }
        };
    }

    @Override
    protected Class<IDebugProtocolServer> getServerInterface() {
        return IDebugProtocolServer.class;
    }

    @Override
    protected WrappedDebugServer<IDebugProtocolServer> wrapDebugServer(IDebugProtocolServer remoteProxy) {
        return new WrappedDebugServer<>(remoteProxy);
    }

    @Override
    protected @NotNull String getType() {
        return "cppvsdbg";
    }

    @Override
    protected void addArgsForLaunch(Map<String, Object> args) {

        args.put("console", "internalConsole");
        args.put("logging", Map.of("moduleLoad", true));
        args.put("__configurationTarget", 2);
    }

    @Override
    protected WinDAPDebuggerClient createDebuggerClient() {
        return this.new WinDAPDebuggerClient();
    }

    @Override
    protected CompletableFuture<?> wrapInitialize(CompletableFuture<Capabilities> capabilitiesCompletableFuture) {
        return capabilitiesCompletableFuture.thenCombine(handshakeFuture, (res, hs) -> res);
    }


    @Override
    protected @NotNull String functionParser(String name) {
        return name.substring(name.indexOf('!') + 1, name.indexOf('('));
    }

    protected class WinDAPDebuggerClient extends DAPDebuggerClient {
        @Override
        public void output(OutputEventArguments args) {
            if ("telemetry".equals(args.getCategory())) {
                return;
            }
            super.output(args);
        }


        @JsonRequest
        public CompletableFuture<HandshakeResponse> handshake(HandshakeRequest request) {
            return CompletableFuture
                    .supplyAsync(() -> handshakeStrategy.handshake(request))
                    .thenApply(handshakeResponse -> {
                        handshakeFuture.complete(handshakeResponse);
                        return handshakeResponse;
                    });
        }
    }


    @Override
    public @Nullable String getArchitecture() {
        return ArchitectureType.forVmCpuArch(CpuArch.CURRENT).getId();
    }

    @Data
    @NoArgsConstructor
    public static class HandshakeRequest {
        @NonNull
        private String value;

        @Override
        public String toString() {
            ToStringBuilder b = new ToStringBuilder(this);
            b.add("value", this.value);
            return b.toString();
        }
    }

    @Data
    @NoArgsConstructor
    @RequiredArgsConstructor
    public static class HandshakeResponse {
        @NonNull
        private String signature;

        @Override
        public String toString() {
            ToStringBuilder b = new ToStringBuilder(this);
            b.add("signature", this.signature);
            return b.toString();
        }
    }

    @FunctionalInterface
    public interface HandshakeStrategy {
        WinDAPDriver.HandshakeResponse handshake(WinDAPDriver.HandshakeRequest request);
    }

}
