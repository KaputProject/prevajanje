class Parser(private val tokens: List<Token>) {
    private var index = 0
    fun match(tokenType: TokenType): Boolean {
        if (index < tokens.size && tokens[index].type == tokenType) {
            index++
            return true
        }
        //println("napaka" + tokens[index])
        return false
    }

    fun primary(): Boolean {
        if (match(TokenType.INT) || match(TokenType.REAL) || match(TokenType.VARIABLE)) {
            return true
        } else if (match(TokenType.LPAREN) && bitwise() && match(TokenType.RPAREN)) {
            return true
        } else if (match(TokenType.GET_SPENT)) {
            return getSpend();
        } else return false
    }


    fun unary(): Boolean {
        if ((match(TokenType.PLUS) && primary()) || (match(TokenType.MINUS) && primary()) || primary()) {
            return true
        } else return false
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
        if (multiplicative() && additivePrime())
            return true
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
        if (additive() && bitwisePrime()) return true
        else return false
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
            bitwise() -> true
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
        return match(TokenType.GET_SPENT) && match(TokenType.STRING)
    }

    private fun setSpend(): Boolean {
        return match(TokenType.SET_SPENT) && match(TokenType.STRING) && bitwise()
    }


//    private fun assign(): Boolean {
//        if (match(TokenType.LET) && match(TokenType.IDENTIFIER) && match(TokenType.ASSIGN) && bitwise()) {
//            retur     n true
//        } else return false
//    }
private fun assign(): Boolean {
    if (match(TokenType.LET) && match(TokenType.VARIABLE) && match(TokenType.ASSIGN) && bitwise()) {
        return true
    } else return false
}

    private fun For(): Boolean {
        if (match(TokenType.FOR) && match(TokenType.LPAREN) && assign() && match(TokenType.TO) && bitwise() && match(
                TokenType.RPAREN
            ) && match(TokenType.LBRACE) && expressions() && match(TokenType.RBRACE)
        ) {
            return true
        } else return false
    }

    private fun Console(): Boolean {
        if (match(TokenType.CONSOLE) && bitwise()) {
            return true
        } else return false
    }

    private fun If(): Boolean {
        if (match(TokenType.IF) && match(TokenType.LPAREN) && Comparison() && match(TokenType.RPAREN) && match(TokenType.LBRACE) && expressions() && match(TokenType.RBRACE) && Else()
        ) {
            return true
        } else return false
    }

    private fun Else(): Boolean {
        if (match(TokenType.ELSE) && match(TokenType.LBRACE) && expressions() && match(TokenType.RBRACE)) {
            return true
        } else return false
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
        if (match(TokenType.VARIABLE) && ParamsPrime()) {
            return true
        }
        return false
    }

    private fun ParamsPrime(): Boolean {
        if (match(TokenType.COMMA) && match(TokenType.VARIABLE) && ParamsPrime()) {

            return true
        }
        return false
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

            match(TokenType.LOCATION) && match(TokenType.STRING) && match(TokenType.TYPE) && match(TokenType.REAL) && match(TokenType.LBRACE) && expressions() && match(
                TokenType.RBRACE
            ) -> true

            else -> false
        }
    }


    private fun point(): Boolean {
        return match(TokenType.LPAREN) && bitwise() && match(TokenType.COMMA) && bitwise() && match(TokenType.RPAREN)
    }


    private fun TYPE(): Boolean {
        return when {
            match(TokenType.CITY) -> true
            match(TokenType.ROAD) -> true
            match(TokenType.BUILDING) -> true
            match(TokenType.LOCATION) -> true
            else -> false
        }
    }




}
