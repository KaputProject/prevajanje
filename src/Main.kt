fun main() {
    val code1 = """
    location "Mango" restaurant 0.0 {
        for (let x = 1 to 10 ) {
            let a = 1
            console "Hello world"
            for (let x = 1 to 10 ) {
                let a = 1
                box ((0, 0), (1, 1))
                bend ((0, 0), (1, 1), 0.01)
                line ((0, 0), (1, 1))
                circle ((0, 0), 0.5)
                point ((1, 1))
            }
        }
    }
    city "maribor" { let a =  2}
    road "maribor" { let a = 1}
    building "maribor" { let a = 1}
    """.trimIndent()
    val code2 = """
    city "maribor" { let a =  }
    road "maribor" { let a = 1}
    building "maribor" { let a = 1}
    """.trimIndent()
    val code3 = """
    set_spent "Mango" (get_spent "Mango" + 10.5)
    """.trimIndent()
    val code4 = """
    1>
    """.trimIndent()
    val code5 = """
     location "Mango" restaurant 0 {
    if (get_spent "Mango" > 0) {
        console get_spent "Mango"
    } else {
        console "No spent"
    }
    box ((0, 0), (1, 1))
    }
    """.trimIndent()


    val lexer = Lexer(code4)
    val tokens = lexer.tokenize()
    var index = 0
    for (token in tokens) {
        println(index.toString() + " " + token)
        index++
    }
    val parser = Parser(tokens)
    println(parser.expressions())
}
