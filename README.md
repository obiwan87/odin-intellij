# Odin Support for IntelliJ

This plugin provides support for the [Odin programming language](https://www.odin-lang.org) in IntelliJ IDEA. You can add the plugin to your JetBrains
IDE via the "Plugin" settings panel or by visiting the [Odin Plugin Page](https://plugins.jetbrains.com/plugin/22933-odin-lang-support).

### Compatibility with JetBrains Products

This table summarizes the features supported by the Odin IntelliJ Plugin across various JetBrains IDEs, including debugging, project creation,
auto-completion, and free usage options.

<table>
<tr>
<th>Product</th>
<th>Debugging</th>
<th>Project creation</th>
<th>Full auto-completion features</th>
<th>Free non-commercial use</th>
</tr>
<tr>
<td>IntelliJ Community</td>
<td>❌</td>
<td>✅</td>
<td>✅</td>
<td>✅</td>
</tr>
<tr>
<td>IntelliJ Ultimate</td>
<td>✅*</td>
<td>✅</td>
<td>✅</td>
<td>❌</td>
</tr>
<tr>
<td>GoLand</td>
<td>✅*</td>
<td>✅</td>
<td>✅</td>
<td>❌</td>
</tr>
<tr>
<td>Rider**</td>
<td>❌</td>
<td>❌</td>
<td>✅</td>
<td>✅</td>
</tr>
<tr>
<td>CLion</td>
<td>✅</td>
<td>✅</td>
<td>✅</td>
<td>✅</td>
</tr>
<tr>
<td><strong>RustRover</strong></td>
<td>✅</td>
<td>✅</td>
<td>✅</td>
<td>✅</td>
</tr>
<tr>
<td><strong>PyCharm Professional</strong></td>
<td>✅*</td>
<td>✅</td>
<td>✅</td>
<td>❌</td>
</tr>
</table>

\* Requires installing the free plugin "Native Debugging Support" (see section **Debugger Settings**).

\*\* Full auto-completion features added for "Rider" in plugin version >= 0.12.0. Rider >= 2025.2 no longer supports debugging
## Getting Started

To begin, ensure you have installed the plugin using the steps mentioned above.



## SDK Setup

Download the Odin SDK [here](https://github.com/odin-lang/Odin/releases) or build it from source by following the instructions on the official
Odin [Install page](https://odin-lang.org/docs/install/).

### Creating a New Project

> [!IMPORTANT]
> Rider does not support creating new projects. If you're using Rider, create a new empty solution and manually add a folder for your Odin "project."

Use the "New Project..." wizard in IntelliJ to create a blank Odin project. The default project structure will look like this:

```
| - bin
| - src
| -- main.odin
```

### Importing a Project from Existing Sources

> [!WARNING]
> If you open an Odin project created with an IDE other than Rider in Rider, you may encounter unexpected behavior related to source and collection
> roots. You can still open such projects in Rider, but first, unmark all source and collection roots in a non-Rider IDE to avoid issues.

If you are importing an existing Odin project, refer to the **Advanced Settings** section below for additional configuration steps.

### Project Settings

To configure the project:

1. Open the settings panel and navigate to **Languages & Frameworks** > **Odin**.
2. Set the following fields:

- **Path to SDK**: The location of the Odin SDK folder, where the Odin binary resides.
- **Checker Arguments**: Command-line arguments for the Odin checker. These arguments control the warnings and errors displayed in the editor. Run
  `odin checker --help` for a list of available options.

### Debugger Settings

Debugger support is available in IntelliJ IDEA Ultimate, GoLand, CLion, PyCharm Professional, and RustRover. If you don't see the
**Debugger Settings** section in the Odin settings, you need to install
the [Native Debugging Support plugin](https://plugins.jetbrains.com/plugin/12775-native-debugging-support) from the JetBrains marketplace.

Here’s an example of what the settings panel will look like when properly configured:

![Debugger Settings](debugger-settings.png)

The following sections outline the setup for various debuggers.

#### LLDB-DAP

To set up LLDB-DAP:

1. Download the [LLVM 18.1.x binaries](https://github.com/llvm/llvm-project/releases/tag/llvmorg-18.1.8) for your platform
2. In the Odin settings page, select the `lldb-dap` executable from the `bin` directory.

> [!IMPORTANT]
> LLDB-DAP requires Python 3.10. Ensure Python 3.10 is installed and accessible by LLDB-DAP. On Windows, the easiest approach is to download the
> embeddable Python 3.10 package and extract its contents into the LLVM `bin` directory.

#### Windows Debugger

To set up the Windows debugger, click the **Download** button. Once the download completes, the path to the debugger will be configured automatically.

#### LLDB (Linux and macOS Only)

On Linux and macOS, LLDB works out-of-the-box without additional configuration.

## Advanced Settings

To unlock the full feature set of this plugin, you must configure **Source Directories** (mandatory) and **Collection Directories** (optional).

### Source Directories

Source directories define the root of your project's source code. These settings are essential for code auto-completion and other IDE features. To
mark a directory as a source root:

1. Right-click the directory in the project view.
2. Select **Mark Directory as > Odin Sources Root**.

![Mark Directory as Sources Root](img/mark_as_source_root.png)

This directory should contain your Odin file with the `main` procedure and your package structure. You can have multiple source roots, but one source
root must not be a subdirectory of another.

### Collection Directories

Odin supports custom collections, which are comparable to library dependencies. To use packages within custom collections, you must mark the directory
as a **Collection Source Root**.

![Mark Directory as Sources Root](img/collection_source_root.png)

After marking a directory as a Collection Source Root, you can reference the packages it contains using the collection’s name.

In the screenshot above, the collection name is the same as the directory name. However, you can rename the collection via the **Rename Refactoring**
option, and the corresponding references in your code will update automatically.

![Rename a collection](img/rename_collection.png)

In the project view, the collection name will appear next to the directory:

![Result of renaming a collection](img/rename_result_collection.png)

### Import Settings from `ols.json`

If you have previously used [ols](https://github.com/DanielGavin/ols), you might have an `ols.json` file containing your LSP settings. You can import
a subset of these settings into your IntelliJ project by:

1. Right-clicking the `ols.json` file.
2. Selecting **Import OLS Config**.

Example:

![Import Ols config](img/import_ols_config.png)

After the import, settings like **checker arguments** (`checker_args`), **checker paths** (`checker_paths`), and **collections** will be applied to
your IntelliJ project.

![Result of importing Ols Config](img/import_ols_result.png)

## Code Regions

You can define foldable code regions in Odin source files using line comments:

```odin
//region Initialization
// Your code here
//endregion
```

Everything between `//region` and `//endregion` is treated as a single foldable block by the IDE, as long
as they don't overlap with other folding regions, such as structs, enums, if-blocks etc. In that case, the latter will take
precedence.

The text following `//region` is used as the region name.

* Region markers must use line comments (`//`).
* Regions may span any number of lines.
* Nested regions are supported if properly balanced.
* Code regions affect editor folding only and have no impact on compilation or runtime behavior.

## Resources

### Odin Language

Odin is under continuous development. Stay updated with the latest news and features on the [Odin Website](https://www.odin-lang.org).

### Grammar

While creating the BNF rules for Odin, inspiration was drawn from the Go programming language.

- Go Language Resources:
  - [Grammar-Kit Grammar for Go](https://github.com/go-lang-plugin-org/go-lang-idea-plugin/blob/master/grammars/go.bnf)
  - [ANTLR4 Grammar for Go](https://github.com/antlr/grammars-v4/blob/master/golang/)

For more details on using JetBrain's Grammar-Kit to create custom language plugins, refer to the
official [Grammar-Kit documentation](https://github.com/JetBrains/Grammar-Kit/blob/master/HOWTO.md).

## Important Note

This plugin is _not_ an official Odin project. It is neither affiliated with nor endorsed by the Odin team. If you encounter issues with this plugin,
please report them using the GitHub issue tracker for this repository, and not to the Odin team directly.
