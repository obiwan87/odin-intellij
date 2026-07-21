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

package com.lasagnerd.odin.debugger.dapDrivers;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.PtyCommandLine;
import com.intellij.execution.process.AnsiEscapeDecoder;
import com.intellij.execution.process.KillableColoredProcessHandler;
import com.intellij.openapi.util.Key;
import com.lasagnerd.odin.utils.StringUtil;
import com.pty4j.PtyProcess;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;

public class DAPProcessHandler extends KillableColoredProcessHandler implements AnsiEscapeDecoder.ColoredTextAcceptor {
    private static final String VISUAL_STUDIO_DEBUG_CONSOLE_TITLE = "Visual Studio Debug Console:";
    private String incompleteOscSequence = "";

    public DAPProcessHandler(@NotNull GeneralCommandLine commandLine) throws ExecutionException {
        super(commandLine);
        setHasPty(commandLine instanceof PtyCommandLine);
        setShouldDestroyProcessRecursively(!hasPty());
    }

    public DAPProcessHandler(@NotNull Process process, String commandLine, @NotNull Charset charset) {
        super(process, commandLine, charset);
        setHasPty(process instanceof PtyProcess);
        setShouldDestroyProcessRecursively(!hasPty());
    }

    @Override
    public void coloredTextAvailable(@NotNull String text, @NotNull Key attributes) {
        super.coloredTextAvailable(StringUtil.translateVT100Escapes(text), attributes);
    }

    /**
     * Removes terminal window-title sequences emitted by VsDebugConsole while preserving ANSI styling sequences.
     * The method keeps incomplete OSC sequences between output chunks because PTY reads may split them anywhere.
     */
    public synchronized @NotNull String prepareTerminalOutput(@NotNull String text) {
        String input = incompleteOscSequence + text;
        incompleteOscSequence = "";
        StringBuilder result = new StringBuilder(input.length());

        for (int offset = 0; offset < input.length(); ) {
            int oscStart = input.indexOf("\u001B]", offset);
            if (oscStart < 0) {
                int inputEnd = input.length();
                if (inputEnd > offset && input.charAt(inputEnd - 1) == '\u001B') {
                    result.append(input, offset, inputEnd - 1);
                    incompleteOscSequence = "\u001B";
                } else {
                    result.append(input, offset, inputEnd);
                }
                break;
            }

            result.append(input, offset, oscStart);
            int contentStart = oscStart + 2;
            int bellEnd = input.indexOf('\u0007', contentStart);
            int stringTerminatorEnd = input.indexOf("\u001B\\", contentStart);
            int sequenceEnd;
            int terminatorLength;
            if (bellEnd >= 0 && (stringTerminatorEnd < 0 || bellEnd < stringTerminatorEnd)) {
                sequenceEnd = bellEnd;
                terminatorLength = 1;
            } else if (stringTerminatorEnd >= 0) {
                sequenceEnd = stringTerminatorEnd;
                terminatorLength = 2;
            } else {
                incompleteOscSequence = input.substring(oscStart);
                break;
            }

            String titleSequence = input.substring(contentStart, sequenceEnd);
            if (titleSequence.contains(VISUAL_STUDIO_DEBUG_CONSOLE_TITLE)) {
                result.append(System.lineSeparator());
            }
            offset = sequenceEnd + terminatorLength;
        }

        return result.toString();
    }
}
