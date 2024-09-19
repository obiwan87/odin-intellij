package inference_test

import ast "../ast"
import tokenizer "../tokenizer"
import "../parser"

t :: proc() {
    pos1:=tokenizer.Pos{}
    pos2:=tokenizer.Pos{}
    ce := ast.new(ast.Call_Expr, pos1, pos2)

    p := &parser.Parser{}
    flags := ast.Field_Flags {}

    x := parser.parse_field_list(p, tokenizer.Token_Kind.Add, flags)

}