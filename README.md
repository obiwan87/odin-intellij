# Odin Support for IntelliJ

This plugin adds support for the [Odin programming language](https://www.odin-lang.org) to IntelliJ IDEA.
You can add it to your JetBrains IDE using the "Plugin" settings panel or by visiting
the [Plugin Page](https://plugins.jetbrains.com/plugin/22933-odin-lang-support).

## Debugger Setup

### LLDB-DAP

Download the LLVM 18.1.x binaries for your machine. In the Odin settings
page, find the `bin` directory and there select the `lldb-dap` executable.

> **IMPORTANT NOTE**
>
> LLDB-DAP needs python 3.10 to run, so make sure it is available on your machine
> and findable by LLDB-DAP. On Windows, the easiest way is to download embeddable python 3.10 and extract
> its contents to the LLVM `bin` directory.

### Windows Debugger

Click on the download button. Once the download is finished, the plugin
will automatically set up the path to the debugger executable.

### LLDB (only Linux and macOS)

LLDB will work out-of-the-box. No additional actions required.

## Resources

### Odin

Odin is in constant development. Visit the  [Odin Website](https://www.odin-lang.org)
for the latest news and features.

### Grammar

When crafting the BNF rules for Odin, I drew a lot of inspiration from Go.

* Go Language resources:
    * [Grammar-Kit grammar for Go](https://github.com/go-lang-plugin-org/go-lang-idea-plugin/blob/master/grammars/go.bnf)
    * [ANTLR4 Grammar for Go](https://github.com/antlr/grammars-v4/blob/master/golang/)

For more info on how to use JetBrain's Grammar-Kit to write custom languages see the official
[Grammar-Kit documentation](https://github.com/JetBrains/Grammar-Kit/blob/master/HOWTO.md).

## Important Note

This is _not_ an official Odin project.
This plugin is not affiliated with or endorsed by the Odin team, so please
don't bother them with issues relating to this plugin. Instead, use the
**GitHub issue tracker** of this repository to submit a bug report or feature request.
