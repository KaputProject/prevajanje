class Parser(private val tokens: List<Token>) {
    private var index = 0

    fun match(tokenType: TokenType): Boolean {
        if (index < tokens.size && tokens[index].type == tokenType) {
            index++
            return true
        }

        return false
    }

    fun primary(): Boolean {
        return if (match(TokenType.INT) || match(TokenType.REAL) || match(TokenType.VARIABLE) || match(TokenType.STRING)) {
            true
        } else if (match(TokenType.LPAREN) && bitwise() && match(TokenType.RPAREN)) {
            true
        } else if (getSpend()) {
            true;
        } else false
    }

    private fun unary(): Boolean {
        return when {
            match(TokenType.MINUS) -> primary()
            match(TokenType.PLUS) -> primary()
            else -> primary()
        }
    }

    fun multiplicative(): Boolean {
        if (unary() && multiplicativePrime()) return true
        else return false
    }

    fun multiplicativePrime(): Boolean {
        if (match(TokenType.MUL) || match(TokenType.DIV)) {
            if (!unary()) {
                return false
            }
            return multiplicativePrime()
        }
        return true // ε
    }

    fun additive(): Boolean {
        if (multiplicative() && additivePrime()) return true
        else return false
    }

    fun additivePrime(): Boolean {
        if (match(TokenType.PLUS) || match(TokenType.MINUS)) {
            if (!multiplicative()) {
                return false
            }
            return additivePrime()
        }
        return true  // ε
    }

    fun bitwise(): Boolean {
        val start = index
        if (additive() && bitwisePrime()) return true
        index = start
        return false
    }

    fun bitwisePrime(): Boolean {
        if (match(TokenType.BWAND) || match(TokenType.BWOR)) {
            if (!additive()) {
                return false
            }
            return bitwisePrime()
        }
        return true // ε
    }

    fun expr(): Boolean {
        val start = index
        return when {
            assign() -> true
            For() -> true
            Console() -> true
            If() -> true
            Fun() -> true
            Block() -> true
            setSpend() -> true
            draw() -> true
            else -> {
                index = start
                false
            }
        }
    }

    fun expressions(atLeastOne: Boolean = false): Boolean {
        val start = index
        if (expr()) {
            if (expressions(true)) {
                return true
            }
        }

        index = start // backtrack
        return atLeastOne // ε samo če smo že imeli vsaj en Expr
    }


    private fun getSpend(): Boolean {
        val start = index
        if( match(TokenType.GET_SPENT) && match(TokenType.STRING)){
            return true
        }
        else {
            index = start
            return false
        }
    }

    private fun setSpend(): Boolean {
        val start = index
        if (match(TokenType.SET_SPENT) && match(TokenType.STRING) && bitwise()) return true
        else {
            index = start
            return false
        }
    }

    private fun assign(): Boolean {
        val start = index
        if (match(TokenType.LET) && match(TokenType.VARIABLE) && match(TokenType.ASSIGN) && bitwise()) {
            return true
        }
        index = start
        return false
    }

    private fun For(): Boolean {
        val start = index
        if (match(TokenType.FOR) && match(TokenType.LPAREN) && assign() && match(TokenType.TO) && bitwise() && match(
                TokenType.RPAREN
            ) && match(TokenType.LBRACE) && expressions() && match(TokenType.RBRACE)
        ) {
            return true
        }
        index = start // rollback
        return false
    }

    private fun Console(): Boolean {
        if (match(TokenType.CONSOLE) && bitwise()) {
            return true
        } else return false
    }

    private fun If(): Boolean {
        if (match(TokenType.IF) && match(TokenType.LPAREN) && Comparison() && match(TokenType.RPAREN) && match(TokenType.LBRACE) && expressions() && match(
                TokenType.RBRACE
            ) && Else()
        ) {
            return true
        } else return false
    }

    private fun Else(): Boolean {
        return if (match(TokenType.ELSE)) {
            match(TokenType.LBRACE) && expressions() && match(TokenType.RBRACE)
        } else true // ε
    }


    private fun Comparison(): Boolean {
        if (bitwise() && ComparisonPrime()) return true
        else return false
    }

    private fun ComparisonPrime(): Boolean {
        if (match(TokenType.LT) && bitwise() || match(TokenType.GT) && bitwise() || match(TokenType.EQ) && bitwise() || match(
                TokenType.NEQ
            ) && bitwise()
        ) {
            return true
        } else return false
    }

    private fun Fun(): Boolean {
        if (match(TokenType.FUN) && match(TokenType.STRING) && match(TokenType.LPAREN) && Params() && match(TokenType.RPAREN) && match(
                TokenType.LBRACE
            ) && expressions() && match(TokenType.RBRACE)
        ) {
            return true
        }
        return false
    }

    private fun Params(): Boolean {
        return if (match(TokenType.VARIABLE)) {
            ParamsPrime()
        } else true // ε
    }

    private fun ParamsPrime(): Boolean {
        return if (match(TokenType.COMMA)) {
            match(TokenType.VARIABLE) && ParamsPrime()
        } else true // ε
    }


    private fun Block(): Boolean {
        return when {
            match(TokenType.CITY) && match(TokenType.STRING) && match(TokenType.LBRACE) && expressions() && match(
                TokenType.RBRACE
            ) -> true

            match(TokenType.ROAD) && match(TokenType.STRING) && match(TokenType.LBRACE) && expressions() && match(
                TokenType.RBRACE
            ) -> true

            match(TokenType.BUILDING) && match(TokenType.STRING) && match(TokenType.LBRACE) && expressions() && match(
                TokenType.RBRACE
            ) -> true

            match(TokenType.LOCATION) && match(TokenType.STRING) && match(TokenType.TYPE) && match(TokenType.REAL) && match(
                TokenType.LBRACE
            ) && expressions() && match(TokenType.RBRACE) -> true

            else -> false
        }
    }

    private fun point(): Boolean {
        return match(TokenType.LPAREN) && bitwise() && match(TokenType.COMMA) && bitwise() && match(TokenType.RPAREN)
    }

    private fun draw(): Boolean {
        return when {
            match(TokenType.LINE) && match(TokenType.LPAREN) && point() && match(TokenType.COMMA) && point() && match(
                TokenType.RPAREN
            ) -> true

            match(TokenType.BEND) && match(TokenType.LPAREN) && point() && match(TokenType.COMMA) && point() && match(
                TokenType.COMMA
            ) && bitwise() && match(TokenType.RPAREN) -> true

            match(TokenType.BOX) && match(TokenType.LPAREN) && point() && match(TokenType.COMMA) && point() && match(
                TokenType.RPAREN
            ) -> true

            match(TokenType.CIRCLE) && match(TokenType.LPAREN) && point() && match(TokenType.COMMA) && bitwise() && match(
                TokenType.RPAREN
            ) -> true

            match(TokenType.POINT) && match(TokenType.LPAREN) && point() && match(TokenType.RPAREN) -> true

            else -> false
        }
    }

    fun program(): Boolean {
        return expressions(atLeastOne = true) && index == tokens.size - 1
    }
}
