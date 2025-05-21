fun main() {
    val code = """
    location "Mango" restaurant 0 {
    if (get_spent "Mango" > 0) {
        console get_spent "Mango"
    } else {
        console "No spent"
    }
    
    box ((0, 0), (1, 1))
    }
    
    console "Hello world"
    
    set_spent "Mango" (get_spent "Mango" + 10.5)
    
    console get_spent "Mango"
    """.trimIndent()

    val lexer = Lexer(code)
    val tokens = lexer.tokenize()

    for (token in tokens) {
        println(token)
    }
}
