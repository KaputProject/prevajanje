fun main() {
//    val code = """
//    for (let i = 1   to 10 ) {
//        console i
//    }
//    """.trimIndent()
    val code = """
    let spremenljivka  10
   

    """.trimIndent()

    val lexer = Lexer(code)
    val tokens = lexer.tokenize()

    for (token in tokens) {
        println(token)
    }
    val parser = Parser(tokens)
    println(parser.expressions())
}
