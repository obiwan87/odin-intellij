package com.lasagnerd.odin.codeInsight.imports;

/**
 *
 * @param packageName The name of the package
 * @param path The path to the directory containing the package
 * @param collection Special case: Name of the collection that is defined in the SDK folder
 *
 * Example
 *
 * import "core:fmt" -> collection=core, package=fmt, path=.
 * import "../other" -> collection=null, package=other, path=../
 *
 */
public record OdinImportInfo(String fullImportPath, String packageName, String path, String collection) {
}
