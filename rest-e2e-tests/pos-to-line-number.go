// Some programs report error position by reporting the character counter (no line numbers). This programs read position number
// as an command line parameter and converts it to line:position pair.
//
// Usage: cat file.json | pos-to-line 6536
// Output: 254:8

package main

import (
	"bufio"
	"fmt"
	"io"
	"log"
	"os"
	"strconv"
)

func main() {
	seek, err := strconv.Atoi(os.Args[1])
	if err != nil {
		log.Fatalf("error parsing position integer")
	}
	r := bufio.NewReader(os.Stdin)
	line := 0
	pos := 0
	linepos := 0
	for {
		if c, _, err := r.ReadRune(); err != nil {
			if err == io.EOF {
				break
			} else {
				log.Fatalf("i/o read error: %v", err)
			}
		} else {
			if pos == seek {
				fmt.Printf("%v:%v\n", line, linepos)
				break
			}
			if c == '\n' {
				line++
				linepos = 0
			}
			linepos++
			pos++
		}
	}
}
