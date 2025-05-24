fun main() {
    val code1 = """
    location "Mango" restaurant 0.0 {
        for (let x = 1 to 10 ) {
            console x
        }
        
        let f = 10
        if (f > 9) {
            box ((0, 0), (1, 1))
        } else {   
            bend ((0, 0), (1, 1), 0.01)
        }

        line ((0, 0), (1, 1))
        circle ((0, 0), 0.5)
        point ((1, 1))
    }""".trimIndent()

    val code2 = """
        if ( f > 9 ) {
            console f
        } else {   
            console f
        }
    """.trimIndent()

    val lexer = Lexer(code2)
    val tokens = lexer.tokenize()
    var index = 0
    for (token in tokens) {
        println(index.toString() + " " + token)
        index++
    }
    val parser = Evaluator(tokens)
    println(parser.program())
}
