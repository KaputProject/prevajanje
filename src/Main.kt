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
    }
    for (let x = 1 to 10 ) {
            console x
        }
        
        let f = 10
        if (f > 9) {
            box ((0, 0), (1, 1))
        } else {   
            bend ((0, 0), (1, 1), 0.01)
        }
    """.trimIndent()

    val code2 = """
        line ((0, 0), (1, 1))
        circle ((0, 0), 0.5)
        point ((1, 1))
    """.trimIndent()

    val code3 = """
        if (f > 9) {
            box ((0, 0), (1, 1))
        } else {   
            bend ((0, 0), (1, 1), 0.01)
        }
    """.trimIndent()

    val code4 = """
        fun "hmmmm"(a, b) {
            console a
            console (b + a)
        }
    """.trimIndent()

    val code5 = """
        setSpent "hmmm" 30
    """.trimIndent()

    val lexer = Lexer(code1)
    val tokens = lexer.tokenize()
    var index = 0
    for (token in tokens) {
        println(index.toString() + " " + token)
        index++
    }

    val evaluator = Evaluator2(tokens, mutableMapOf())
    val program = evaluator.program()
    for (statement in program.body) {
        println(statement)
    }
}
