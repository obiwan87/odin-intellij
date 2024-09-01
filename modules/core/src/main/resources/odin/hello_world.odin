package main

import "core:fmt"

main :: proc() {
	fmt.println("Hellope!");
	when size_of(E) != 0 {
		if n := len(data); n > 1 {
			_quick_sort_general(data, 0, n, _max_depth(n), struct{}{}, .Ordered)
		}
	}
}
