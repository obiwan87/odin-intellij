package ols_builtin

// Procedures

@builtin len :: proc(array: Array_Type) -> int ---
@builtin cap :: proc(array: Array_Type) -> int ---

size_of      :: proc($T: typeid) -> int ---
@builtin align_of     :: proc($T: typeid) -> int ---
@builtin type_of      :: proc(x: expr) -> type ---
@builtin type_info_of :: proc($T: typeid) -> ^runtime.Type_Info ---
@builtin typeid_of    :: proc($T: typeid) -> typeid ---

offset_of_selector :: proc(selector: $T) -> uintptr ---
offset_of_member   :: proc($T: typeid, member: $M) -> uintptr ---

@builtin offset_of :: proc{offset_of_selector, offset_of_member}

@builtin offset_of_by_string :: proc($T: typeid, member: string) -> uintptr ---

@builtin swizzle :: proc(x: [N]T, indices: ..int) -> [len(indices)]T ---

complex    :: proc(real, imag: Float) -> Complex_Type ---
quaternion :: proc(real, imag, jmag, kmag: Float) -> Quaternion_Type ---
real       :: proc(value: Complex_Or_Quaternion) -> Float ---
imag       :: proc(value: Complex_Or_Quaternion) -> Float ---
jmag       :: proc(value: Quaternion) -> Float ---
kmag       :: proc(value: Quaternion) -> Float ---
conj       :: proc(value: Complex_Or_Quaternion) -> Complex_Or_Quaternion ---

@builtin min   :: proc(values: ..T) -> T ---
@builtin max   :: proc(values: ..T) -> T ---
@builtin abs   :: proc(value: T) -> T ---
@builtin clamp :: proc(value, minimum, maximum: T) -> T ---

/*
	This is interally from the compiler
*/

Odin_OS_Type :: enum int {
    Unknown,
    Windows,
    Darwin,
    Linux,
    Essence,
    FreeBSD,
    Haiku,
    OpenBSD,
    WASI,
    JS,
    Freestanding,
}

@builtin
ODIN_OS: Odin_OS_Type

Odin_Arch_Type :: enum int {
    Unknown,
    amd64,
    i386,
    arm64,
    wasm32,
    wasm64p32,
}

@builtin
ODIN_ARCH:  Odin_Arch_Type

Odin_Build_Mode_Type :: enum int {
    Executable,
    Dynamic,
    Object,
    Assembly,
    LLVM_IR,
}

@builtin
ODIN_BUILD_MODE: Odin_Build_Mode_Type

Odin_Endian_Type :: enum int {
    Unknown,
    Little,
    Big,
}

@builtin
ODIN_ENDIAN: Odin_Endian_Type

Odin_Platform_Subtarget_Type :: enum int {
    Default,
    iOS,
}

@builtin
ODIN_PLATFORM_SUBTARGET: Odin_Platform_Subtarget_Type

Odin_Sanitizer_Flag :: enum u32 {
    Address,
    Memory,
    Thread,
}

@builtin
ODIN_SANITIZER_FLAGS :: distinct bit_set[Odin_Sanitizer_Flag; u32]

@builtin
ODIN_DEBUG: bool

@builtin
ODIN_NO_RTTI: bool

@builtin
ODIN_DISABLE_ASSERT: bool

@builtin
true: bool

@builtin
false: bool

@builtin
ODIN_VERSION : string

@builtin
ODIN_OS_STRING : string

@builtin
ODIN_ENDIAN_STRING : string

@builtin
ODIN_ARCH_STRING: string

Error_Pos_Style :: enum {
    Default,
    Unix
}

@builtin
ODIN_ERROR_POS_STYLE : Error_Pos_Style

@builtin
ODIN_DEFAULT_TO_NIL_ALLOCATOR: bool

@builtin
ODIN_NO_DYNAMIC_LITERALS: bool

@builtin
ODIN_NO_CRT: bool

@builtin
ODIN_USE_SEPARATE_MODULES: bool

@builtin
ODIN_TEST: bool

@builtin
ODIN_NO_ENTRY_POINT: bool

@builtin
ODIN_FOREIGN_ERROR_PROCEDURES: bool

@builtin
ODIN_NO_RTTI: bool

@builtin
ODIN_ROOT: string

@builtin
ODIN_BUILD_PROJECT_NAME: string

@builtin
ODIN_VENDOR:  string

@builtin
ODIN_VALGRIND_SUPPORT: bool

@builtin
Any_Type :: struct {
    data: rawptr,
    id: typeid
}

@builtin
any :: distinct Any_Type

@builtin
unreachable :: proc() -> ! ---

@builtin
Odin_Optimization_Mode :: enum int {
    None       = -1,
    Minimal    =  0,
    Size       =  1,
    Speed      =  2,
    Aggressive =  3,
}

@builtin
ODIN_OPTIMIZATION_MODE : Odin_Optimization_Mode

@builtin
ODIN_DEFAULT_TO_PANIC_ALLOCATOR : bool