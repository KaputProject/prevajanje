//import kotlin.coroutines.Continuation
//
//class kParser(private val tokens: List<Token>) {
//    private var index = 0
//
//    private val currentToken: Token
//        get() = tokens.getOrElse(index) { Token(TokenType.UNKNOWN, "") }
//
//    private fun acceptToken(tokenType: TokenType): Boolean {
//        return if (currentToken.type == tokenType) {
//            index++
//            true
//        } else {
//            false
//        }
//    }
//
//    fun parse(): Boolean {
//        val result = program()
//        return result && index == tokens.size
//    }
//
//    private fun program(): Boolean {
//        while (index < tokens.size) {
//            if (!statement()) return false
//            if (index < tokens.size && !acceptToken(TokenType.SEMI)) return false
//        }
//        return true
//    }
//
//    private fun statement(): Boolean {
//        return assignment() || forLoop() || console()
//    }
//
//    /*
//        for (i := 1 to stevilo - 1) begin
//        fakulteta:= fakulteta * i;
//        end;
//    */
//    private fun forLoop(): Boolean {
//        if (!(acceptToken(TokenType.FOR) &&
//                    acceptToken(TokenType.LPAREN) &&
//                    acceptToken(TokenType.VARIABLE) &&
//                    acceptToken(TokenType.ASSIGN) &&
//                    expr() &&
//                    acceptToken(TokenType.TO) &&
//                    expr() &&
//                    acceptToken(TokenType.RPAREN) &&
//                    acceptToken(TokenType.BEGIN))) return false
//
//        while (!acceptToken(TokenType.END)) {
//            if (!statement()) return false
//            if (currentToken.type != TokenType.END && !acceptToken(TokenType.SEMI)) return false
//        }
//
//        return true
//    }
//
//    private fun assignment(): Boolean {
//        return acceptToken(TokenType.VARIABLE) &&
//                acceptToken(TokenType.ASSIGN) &&
//                expr()
//    }
//
//    private fun console(): Boolean {
//        return acceptToken(TokenType.CONSOLE) &&
//                acceptToken(TokenType.VARIABLE)
//    }
//
//    private fun expr(): Boolean {
//        return bitwise()
//    }
//
//    private fun bitwise(): Boolean {
//        return additive() && bitwiseP()
//    }
//
//    private fun bitwiseP(): Boolean {
//        return when {
//            acceptToken(TokenType.BWAND) -> {
//                additive() && bitwiseP()
//            }
//            acceptToken(TokenType.BWOR) -> {
//                additive() && bitwiseP()
//            }
//            else -> true
//        }
//    }
//
//    private fun additive(): Boolean {
//        return multiplicative() && additiveP()
//    }
//
//    private fun additiveP(): Boolean {
//        return when {
//            acceptToken(TokenType.PLUS) -> {
//                multiplicative() && additiveP()
//            }
//            acceptToken(TokenType.MINUS) -> {
//                multiplicative() && additiveP()
//            }
//            else -> true
//        }
//    }
//
//    private fun multiplicative(): Boolean {
//        return unary() && multiplicativeP()
//    }
//
//    private fun multiplicativeP(): Boolean {
//        return when {
//            acceptToken(TokenType.TIMES) -> {
//                unary() && multiplicativeP()
//            }
//            acceptToken(TokenType.DIVIDE) -> {
//                unary() && multiplicativeP()
//            }
//            else -> true
//        }
//    }
//
//    private fun unary(): Boolean {
//        return when {
//            acceptToken(TokenType.MINUS) -> primary()
//            acceptToken(TokenType.PLUS) -> primary()
//            else -> primary()
//        }
//    }
//
//    private fun primary(): Boolean {
//        return when {
//            acceptToken(TokenType.INT) -> true
//            acceptToken(TokenType.HEX) -> true
//            acceptToken(TokenType.VARIABLE) -> true
//            acceptToken(TokenType.LPAREN) -> {
//                bitwise() && acceptToken(TokenType.RPAREN)
//            }
//            else -> false
//        }
//    }
//}