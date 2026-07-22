# LLDB-DAP diagnostics

Throwaway, standalone Java harness for diagnosing LLDB-DAP startup and protocol failures. It is an
independent Maven project and is deliberately not included in the Odin plugin's Gradle build.

## Build

Use JDK 21 or newer:

```powershell
mvn package
```

## Run

Test stdio initialization:

```powershell
java -jar target\lldb-dap-diagnostics-1.0-SNAPSHOT.jar `
  --adapter "D:\opt\llvm-project-llvmorg-22.1.8-build\bin\lldb-dap.exe"
```

Test TCP initialization:

```powershell
java -jar target\lldb-dap-diagnostics-1.0-SNAPSHOT.jar `
  --adapter "D:\opt\llvm-project-llvmorg-22.1.8-build\bin\lldb-dap.exe" `
  --transport tcp --port 47191
```

Test initialization through LSP4J 0.23.0 instead of the custom DAP codec:

```powershell
java -jar target\lldb-dap-diagnostics-1.0-SNAPSHOT.jar `
  --adapter "D:\opt\llvm-project-llvmorg-22.1.8-build\bin\lldb-dap.exe" `
  --lsp4j-enabled
```

Test the complete launch handshake. The harness sends `launch` asynchronously, waits for the
`initialized` event, sends `configurationDone`, and then awaits the delayed `launch` response:

```powershell
java -jar target\lldb-dap-diagnostics-1.0-SNAPSHOT.jar `
  --adapter "D:\opt\llvm-project-llvmorg-22.1.8-build\bin\lldb-dap.exe" `
  --lsp4j-enabled `
  --program "C:\Users\Simone\CLionProjects\untitled2\bin\untitled2.exe" `
  --cwd "C:\Users\Simone\CLionProjects\untitled2"
```

Keep a hung adapter alive for Visual Studio attachment:

```powershell
java -jar target\lldb-dap-diagnostics-1.0-SNAPSHOT.jar `
  --adapter "D:\opt\llvm-project-llvmorg-22.1.8-build\bin\lldb-dap.exe" `
  --timeout 10 --keep-alive
```

The harness prints the adapter PID and writes exact protocol bytes plus adapter stdout/stderr under
`logs/<timestamp>/`.
