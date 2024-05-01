package com.lasagnerd.odin.insights;

/**
 *
 * @param packageName The name of the package
 * @param path The path to the directory containing the package
 * @param library Special case: Name of the library that is defined in the SDK folder
 *
 * Example
 *
 * import "core:fmt" -> library=core, package=fmt, path=.
 * import "../other" -> library=null, package=other, path=../
 *
 */
public record OdinImportInfo(String packageName, String path, String library) {
}
