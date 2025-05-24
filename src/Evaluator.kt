class ParseException(message: String) : RuntimeException(message)

class Evaluator(
    private val tokens: List<Token>,
    private val variables: MutableMap<String, Int> = mutableMapOf()
) {
    private var index = 0

    private fun match(expected: TokenType): Boolean {
        if (index < tokens.size && tokens[index].type == expected) {
            index++
            return true
        }
        return false
    }

    fun primary(): Boolean {
        return when {
            match(TokenType.INT) || match(TokenType.REAL) || match(TokenType.VARIABLE) -> true
            match(TokenType.LPAREN) -> {
                if (!bitwise()) throw ParseException("Expected expression after '(' at index $index")
                if (!match(TokenType.RPAREN)) throw ParseException("Expected ')' at index $index")
                true
            }
            getSpend() -> true
            else -> false
        }
    }

    private fun unary(): Boolean {
        return if (match(TokenType.MINUS) || match(TokenType.PLUS)) {
            if (!primary()) throw ParseException("Expected value after unary operator at index $index")
            true
        } else {
            primary()
        }
    }

    fun multiplicative(): Boolean {
        if (!unary()) return false
        return multiplicativePrime()
    }

    private fun multiplicativePrime(): Boolean {
        if (match(TokenType.MUL) || match(TokenType.DIV)) {
            if (!unary()) throw ParseException("Expected value after '*' or '/' at index $index")
            return multiplicativePrime()
        }
        return true
    }

    fun additive(): Boolean {
        if (!multiplicative()) return false
        return additivePrime()
    }

    private fun additivePrime(): Boolean {
        if (match(TokenType.PLUS) || match(TokenType.MINUS)) {
            if (!multiplicative()) throw ParseException("Expected value after '+' or '-' at index $index")
            return additivePrime()
        }
        return true
    }

    fun bitwise(): Boolean {
        if (!additive()) return false
        return bitwisePrime()
    }

    private fun bitwisePrime(): Boolean {
        if (match(TokenType.BWAND) || match(TokenType.BWOR)) {
            if (!additive()) throw ParseException("Expected value after '&' or '|' at index $index")
            return bitwisePrime()
        }
        return true
    }

    fun expr(): Boolean {
        val start = index
        return when {
            assign() || For() || Console() || If() || Fun() || Block() || setSpend() || draw() -> true
            else -> {
                index = start
                false
            }
        }
    }

    private fun getSpend(): Boolean {
        val start = index
        if (match(TokenType.GET_SPENT)) {
            if (!match(TokenType.STRING)) throw ParseException("Expected string after GET_SPENT at index $index")
            return true
        }
        index = start
        return false
    }

    private fun setSpend(): Boolean {
        val start = index
        if (match(TokenType.SET_SPENT)) {
            if (!match(TokenType.STRING)) throw ParseException("Expected string after SET_SPENT at index $index")
            if (!bitwise()) throw ParseException("Expected expression after SET_SPENT string at index $index")
            return true
        }
        index = start
        return false
    }

    private fun assign(): Boolean {
        val start = index
        if (match(TokenType.LET)) {
            if (!match(TokenType.VARIABLE)) throw ParseException("Expected variable after LET at index $index")
            if (!match(TokenType.ASSIGN)) throw ParseException("Expected '=' after variable at index $index")
            if (!bitwise()) throw ParseException("Expected expression after '=' at index $index")
            return true
        }
        index = start
        return false
    }

    private fun For(): Boolean {
        val start = index
        if (match(TokenType.FOR)) {
            if (!match(TokenType.LPAREN)) throw ParseException("Expected '(' after FOR at index $index")
            if (!assign()) throw ParseException("Expected assignment inside FOR at index $index")
            if (!match(TokenType.TO)) throw ParseException("Expected TO in FOR at index $index")
            if (!bitwise()) throw ParseException("Expected expression after TO in FOR at index $index")
            if (!match(TokenType.RPAREN)) throw ParseException("Expected ')' after FOR range at index $index")
            if (!match(TokenType.LBRACE)) throw ParseException("Expected '{' to start FOR body at index $index")
            if (!expressions()) throw ParseException("Expected expressions inside FOR block at index $index")
            if (!match(TokenType.RBRACE)) throw ParseException("Expected '}' to close FOR block at index $index")
            return true
        }
        index = start
        return false
    }

    private fun Console(): Boolean {
        val start = index
        if (match(TokenType.CONSOLE)) {
            if (!bitwise()) throw ParseException("Expected expression after CONSOLE at index $index")
            return true
        }
        index = start
        return false
    }

    private fun If(): Boolean {
        val start = index
        if (match(TokenType.IF)) {
            if (!match(TokenType.LPAREN)) throw ParseException("Expected '(' after IF at index $index")
            if (!Comparison()) throw ParseException("Expected condition inside IF at index $index")
            if (!match(TokenType.RPAREN)) throw ParseException("Expected ')' after IF condition at index $index")
            if (!match(TokenType.LBRACE)) throw ParseException("Expected '{' to start IF body at index $index")
            if (!expressions()) throw ParseException("Expected expressions in IF block at index $index")
            if (!match(TokenType.RBRACE)) throw ParseException("Expected '}' to close IF block at index $index")
            if (!Else()) throw ParseException("Invalid ELSE block after IF at index $index")
            return true
        }
        index = start
        return false
    }

    private fun Else(): Boolean {
        val start = index
        if (match(TokenType.ELSE)) {
            if (!match(TokenType.LBRACE)) throw ParseException("Expected '{' after ELSE at index $index")
            if (!expressions()) throw ParseException("Expected expressions in ELSE block at index $index")
            if (!match(TokenType.RBRACE)) throw ParseException("Expected '}' to close ELSE block at index $index")
            return true
        }
        return true // epsilon
    }

    private fun Comparison(): Boolean {
        if (!bitwise()) return false
        return when {
            match(TokenType.LT) || match(TokenType.GT) || match(TokenType.EQ) || match(TokenType.NEQ) -> {
                if (!bitwise()) throw ParseException("Expected right-hand side in comparison at index $index")
                true
            }
            else -> true
        }
    }

    private fun Fun(): Boolean {
        val start = index
        if (match(TokenType.FUN)) {
            if (!match(TokenType.STRING)) throw ParseException("Expected function name string after FUN at index $index")
            if (!match(TokenType.LPAREN)) throw ParseException("Expected '(' after function name at index $index")
            if (!Params()) throw ParseException("Invalid parameters in FUN at index $index")
            if (!match(TokenType.RPAREN)) throw ParseException("Expected ')' after parameters at index $index")
            if (!match(TokenType.LBRACE)) throw ParseException("Expected '{' to start FUN body at index $index")
            if (!expressions()) throw ParseException("Expected expressions inside FUN block at index $index")
            if (!match(TokenType.RBRACE)) throw ParseException("Expected '}' to close FUN block at index $index")
            return true
        }
        index = start
        return false
    }

    private fun Params(): Boolean {
        if (match(TokenType.VARIABLE)) {
            return ParamsPrime()
        }
        return true
    }

    private fun ParamsPrime(): Boolean {
        if (match(TokenType.COMMA)) {
            if (!match(TokenType.VARIABLE)) throw ParseException("Expected variable after ',' in parameters at index $index")
            return ParamsPrime()
        }
        return true
    }

    private fun Block(): Boolean {
        val start = index
        return when {
            match(TokenType.CITY) || match(TokenType.ROAD) || match(TokenType.BUILDING) -> {
                if (!match(TokenType.STRING)) throw ParseException("Expected name string after block type at index $index")
                if (!match(TokenType.LBRACE)) throw ParseException("Expected '{' after block name at index $index")
                if (!expressions()) throw ParseException("Expected expressions inside block at index $index")
                if (!match(TokenType.RBRACE)) throw ParseException("Expected '}' to close block at index $index")
                true
            }
            match(TokenType.LOCATION) -> {
                if (!match(TokenType.STRING)) throw ParseException("Expected location name string at index $index")
                if (!match(TokenType.TYPE)) throw ParseException("Expected TYPE keyword after location name at index $index")
                if (!match(TokenType.REAL)) throw ParseException("Expected real number after TYPE at index $index")
                if (!match(TokenType.LBRACE)) throw ParseException("Expected '{' to start LOCATION body at index $index")
                if (!expressions()) throw ParseException("Expected expressions in LOCATION block at index $index")
                if (!match(TokenType.RBRACE)) throw ParseException("Expected '}' to close LOCATION block at index $index")
                true
            }
            else -> {
                index = start
                false
            }
        }
    }

    private fun point(): Boolean {
        if (!match(TokenType.LPAREN)) return false
        if (!bitwise()) throw ParseException("Expected X coordinate in point at index $index")
        if (!match(TokenType.COMMA)) throw ParseException("Expected ',' between point coordinates at index $index")
        if (!bitwise()) throw ParseException("Expected Y coordinate in point at index $index")
        if (!match(TokenType.RPAREN)) throw ParseException("Expected ')' to close point at index $index")
        return true
    }

    private fun draw(): Boolean {
        val start = index
        return when {
            match(TokenType.LINE) || match(TokenType.BOX) -> {
                if (!match(TokenType.LPAREN)) throw ParseException("Expected '(' after shape at index $index")
                if (!point()) throw ParseException("Expected first point after '(' at index $index")
                if (!match(TokenType.COMMA)) throw ParseException("Expected ',' after first point at index $index")
                if (!point()) throw ParseException("Expected second point after ',' at index $index")
                if (!match(TokenType.RPAREN)) throw ParseException("Expected ')' after second point at index $index")
                true
            }
            match(TokenType.CIRCLE) -> {
                if (!match(TokenType.LPAREN)) throw ParseException("Expected '(' after shape at index $index")
                if (!point()) throw ParseException("Expected first point after '(' at index $index")
                if (!match(TokenType.COMMA)) throw ParseException("Expected ',' after first point at index $index")
                if (!bitwise()) throw ParseException("Expected Bitwise after ',' at index $index")
                if (!match(TokenType.RPAREN)) throw ParseException("Expected ')' after second point at index $index")
                true
            }
            match(TokenType.BEND) -> {
                if (!match(TokenType.LPAREN)) throw ParseException("Expected '(' after BEND at index $index")
                if (!point()) throw ParseException("Expected first point in BEND at index $index")
                if (!match(TokenType.COMMA)) throw ParseException("Expected ',' after first point in BEND at index $index")
                if (!point()) throw ParseException("Expected second point in BEND at index $index")
                if (!match(TokenType.COMMA)) throw ParseException("Expected ',' before radius in BEND at index $index")
                if (!bitwise()) throw ParseException("Expected radius expression in BEND at index $index")
                if (!match(TokenType.RPAREN)) throw ParseException("Expected ')' after BEND parameters at index $index")
                true
            }
            match(TokenType.POINT) -> {
                if (!match(TokenType.LPAREN)) throw ParseException("Expected '(' after POINT at index $index")
                if (!point()) throw ParseException("Expected coordinates in POINT at index $index")
                if (!match(TokenType.RPAREN)) throw ParseException("Expected ')' to close POINT at index $index")
                true
            }
            else -> {
                index = start
                false
            }
        }
    }

    fun expressions(atLeastOne: Boolean = false): Boolean {
        val start = index
        if (expr()) return expressionsPrime()
        index = start
        return !atLeastOne
    }

    private fun expressionsPrime(): Boolean {
        val start = index
        return if (expr()) {
            expressionsPrime()
        } else {
            true
        }
    }

    fun program(): Boolean {
        return expressions(atLeastOne = true) && index == tokens.size - 1
    }
}