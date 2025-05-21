data class Token(
    val type: TokenType,
    val text: String,
    val line: Int,
    val column: Int
)
