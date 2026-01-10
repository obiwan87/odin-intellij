package evaluation

_T :: struct {
    linux: i64
}

test_indirect_import :: proc() {
    s := T { }
    x := s.linux
}