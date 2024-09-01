package com.lasagnerd.odin.debugger;

import com.intellij.openapi.fileTypes.FileType;
import com.jetbrains.cidr.execution.debugger.breakpoints.CidrLineBreakpointFileTypesProvider;
import com.lasagnerd.odin.lang.OdinFileType;

import java.util.Collections;
import java.util.Set;

public class OdinLineBreakpointFileTypesProvider implements CidrLineBreakpointFileTypesProvider {
	private static final Set<FileType> ODIN_FILE_TYPES = Collections.singleton(OdinFileType.INSTANCE);
	@Override
	public Set<FileType> getFileTypes() {
		return ODIN_FILE_TYPES;
	}
}
