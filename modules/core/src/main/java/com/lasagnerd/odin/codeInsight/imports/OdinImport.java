package com.lasagnerd.odin.codeInsight.imports;

/**
 * Represents either an import destination or an import source.
 *
 * @param packageName The name of the package
 * @param path        The relative path to the directory containing the package
 * @param collection  Special case: Name of the collection that is defined in the SDK folder
 *
 *                    <h2>Example</h2>
 *
 *                    <pre>
 *                    import "core:fmt" -> collection=core, package=fmt, path=. <br/>
 *                    import "../other" -> collection=null, package=other, path=../
 *                    </pre>
 * @param alias The alias of the import
 */
public record OdinImport(
        String fullImportPath,
        String canonicalName,
        String path,
        String collection,
        String alias) {

    public String packageName() {
        if (alias != null) {
            return alias;
        }
        return canonicalName;
    }
}
