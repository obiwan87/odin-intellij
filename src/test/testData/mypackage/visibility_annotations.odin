package mypackage

a_mypublic_proc :: proc() -> a_ret {
    return a_ret {}
}

@(private)
b_mypackage_private_proc :: proc() {

}

@(private="file")
c_myfile_private_proc :: proc() {

}

a_ret :: struct {}