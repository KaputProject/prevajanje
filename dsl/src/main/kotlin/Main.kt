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
    """.trimIndent()
    val code2 = """
        box ((0, 0), (1, 1))
    """.trimIndent()
    val code3 = """
    set_spent "Mango" (get_spent "Mango" + 10.5)
    """.trimIndent()
    val code4 = """
    for (let x = 1 to 10 ) {
        let a = 1
    }
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


    val lexer = Lexer(code3)
    val tokens = lexer.tokenize()

    for (token in tokens) {
        println(token)
    }
    val parser = Parser(tokens)
    println(parser.expressions())
}
