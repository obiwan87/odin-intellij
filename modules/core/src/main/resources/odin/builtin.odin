package builtin

import "base:runtime"

// Procedures

@builtin len :: proc(array: Array_Type) -> int ---
@builtin cap :: proc(array: Array_Type) -> int ---
@builtin soa_zip :: proc(slices: ...) -> #soa[]Struct ---
@builtin soa_unzip :: proc(value: $S/#soa[]$E) -> (slices: ...) ---
@builtin size_of      :: proc($T: typeid) -> int ---
@builtin align_of     :: proc($T: typeid) -> int ---
@builtin type_of      :: proc(x: expr) -> type ---
@builtin type_info_of :: proc($T: typeid) -> ^runtime.Type_Info ---
@builtin typeid_of    :: proc($T: typeid) -> typeid ---

@builtin offset_of_selector :: proc(selector: $T) -> uintptr ---
@builtin offset_of_member   :: proc($T: typeid, #member member: $M) -> uintptr ---

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

@builtin
Odin_OS_Type :: enum int {
    Unknown,
    Windows,
    Darwin,
    Linux,
    Essence,
    FreeBSD,
    OpenBSD,
    NetBSD,
    Haiku,
    WASI,
    JS,
    Orca,
    Freestanding,
}

@builtin
ODIN_OS : Odin_OS_Type : .Unknown

@builtin
Odin_Arch_Type :: enum int {
    Unknown,
    amd64,
    i386,
    arm32,
    arm64,
    wasm32,
    wasm64p32,
    riscv64,
}

@builtin
ODIN_ARCH:  Odin_Arch_Type : .i386

@builtin
Odin_Build_Mode_Type :: enum int {
    Executable,
    Dynamic,
    Object,
    Assembly,
    LLVM_IR,
}

@builtin
ODIN_BUILD_MODE: Odin_Build_Mode_Type : .Executable

@builtin
Odin_Endian_Type :: enum int {
    Unknown,
    Little,
    Big,
}

@builtin
ODIN_ENDIAN: Odin_Endian_Type : .Little

@builtin
Odin_Platform_Subtarget_Type :: enum int {
    Default,
    iOS,
}

@builtin
ODIN_PLATFORM_SUBTARGET: Odin_Platform_Subtarget_Type : .Default

@builtin
Odin_Sanitizer_Flag :: enum u32 {
    Address,
    Memory,
    Thread,
}

@builtin
ODIN_SANITIZER_FLAGS :: distinct bit_set[Odin_Sanitizer_Flag; u32]

@builtin
ODIN_DEBUG: bool : true // TODO add to builtin value collection

@builtin
ODIN_NO_RTTI: bool : true // TODO add to builtin value collection

@builtin
ODIN_DISABLE_ASSERT: bool : true // TODO add to builtin value collection

@builtin
true: bool : true // TODO add to builtin value collection

@builtin
false: bool : false // TODO add to builtin value collection

@builtin
ODIN_VERSION : string : "" // TODO add to builtin value collection

@builtin
ODIN_OS_STRING : string : "" // TODO add to builtin value collection

@builtin
ODIN_ENDIAN_STRING : string : "" // TODO add to builtin value collection

@builtin
ODIN_ARCH_STRING: string : "" // TODO add to builtin value collection

@builtin
Error_Pos_Style :: enum {
    Default,
    Unix
}

@builtin
ODIN_ERROR_POS_STYLE : Error_Pos_Style : Error_Pos_Style.Default

@builtin
ODIN_DEFAULT_TO_NIL_ALLOCATOR: bool : true // TODO add to builtin value collection

@builtin
ODIN_NO_DYNAMIC_LITERALS: bool : true // TODO add to builtin value collection

@builtin
ODIN_NO_CRT: bool : true // TODO add to builtin value collection

@builtin
ODIN_USE_SEPARATE_MODULES: bool : true // TODO add to builtin value collection

@builtin
ODIN_TEST: bool : false // TODO add to builtin value collection

@builtin
ODIN_NO_ENTRY_POINT: bool : true // TODO add to builtin value collection

@builtin
ODIN_FOREIGN_ERROR_PROCEDURES: bool : true // TODO add to builtin value collection

@builtin
ODIN_NO_RTTI: bool : true // TODO add to builtin value collection

@builtin
ODIN_ROOT: string : "" // TODO add to builtin value collection

@builtin
ODIN_BUILD_PROJECT_NAME: string : "" // TODO add to builtin value collection

@builtin
ODIN_VENDOR:  string : "odin" // TODO add to builtin value collection

@builtin
ODIN_VALGRIND_SUPPORT: bool : true // TODO add to builtin value collection

@(private="file", builtin)
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
ODIN_OPTIMIZATION_MODE : Odin_Optimization_Mode : Odin_Optimization_Mode.None

@builtin
ODIN_DEFAULT_TO_PANIC_ALLOCATOR : bool : true // TODO add to builtin value collection

@builtin
ODIN_NO_BOUNDS_CHECK: bool : false // TODO add to builtin value collection

@builtin
ODIN_MINIMUM_OS_VERSION : int : 0 // TODO

/*
ODIN_OS = Windows
ODIN_ARCH = amd64
ODIN_BUILD_MODE = Executable
ODIN_ENDIAN = Little
ODIN_PLATFORM_SUBTARGET = Default
ODIN_SANITIZER_FLAGS = Odin_Sanitizer_Flags{}
ODIN_DEBUG = false
ODIN_NO_RTTI = false
ODIN_DISABLE_ASSERT = false
ODIN_OS_STRING = windows
ODIN_ENDIAN_STRING = little
ODIN_ARCH_STRING = amd64
ODIN_ERROR_POS_STYLE = Default
ODIN_DEFAULT_TO_NIL_ALLOCATOR = false
ODIN_NO_DYNAMIC_LITERALS = false
ODIN_NO_CRT = false
ODIN_USE_SEPARATE_MODULES = true
ODIN_TEST = false
ODIN_NO_ENTRY_POINT = false
ODIN_FOREIGN_ERROR_PROCEDURES = false
ODIN_NO_RTTI = false
ODIN_VERSION = dev-2024-10
ODIN_ROOT = D:\dev\code\Odin\
ODIN_BUILD_PROJECT_NAME = src
ODIN_VENDOR = odin
ODIN_VALGRIND_SUPPORT = false
ODIN_OPTIMIZATION_MODE = Minimal
ODIN_DEFAULT_TO_PANIC_ALLOCATOR = false
ODIN_NO_BOUNDS_CHECK = false
ODIN_MINIMUM_OS_VERSION = 0
*/