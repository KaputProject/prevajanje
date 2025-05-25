import classes.*
import java.math.BigDecimal

class Block(
    val type: String,
    val name: String,
    val body: List<Statement>,
    val locationType: String? = null,
    val locationValue: BigDecimal? = null,
)

class Evaluator2(private val tokens: List<Token>, private val variables: MutableMap<String, BigDecimal> = mutableMapOf()) {
    private var index = 0

    private var locations: MutableMap<String, Block> = mutableMapOf()

    private fun match(vararg expected: TokenType): Boolean {
        if (index < tokens.size && expected.contains(tokens[index].type)) {
            index++
            return true
        }
        return false
    }

    private fun consume(expected: TokenType, message: String): Token {
        if (match(expected)) return tokens[index - 1]

        throw ParseException(message)
    }

    fun program(): Program {
        val stmts = expressions()
        consume(TokenType.EOF, "Expected end of file, index ${index}")

        return Program(stmts)
    }

    private fun expressions(): List<Statement> {
        val statement = expr() ?: throw ParseException("Expected at least one expression in expressions")
        val rest = expressionsPrime()

        return listOf(statement) + rest
    }

    private fun expressionsPrime(): List<Statement> {
        val statement = expr() ?: return emptyList()

        return listOf(statement) + expressionsPrime()
    }

    private fun expr(): Statement? {
        return assign() ?: console() ?: ifStmt() ?: forLoop() ?: function() ?: setSpend() ?: draw() ?: block()
    }

    private fun assign(): Statement.Assign? {
        val start = index
        if (match(TokenType.LET)) {
            val name = consume(TokenType.VARIABLE, "Expected variable after LET").text
            consume(TokenType.ASSIGN, "Expected '=' after variable")
            val expr = bitwise() ?: throw ParseException("Expected expression after '='")

            return Statement.Assign(name, expr)
        }
        index = start
        return null
    }

    private fun setSpend(): Statement.SetSpend? {
        val start = index
        if (match(TokenType.SET_SPENT)) {
            val key = consume(TokenType.STRING, "Expected string after SET_SPENT").text
            val expr = bitwise() ?: throw ParseException("Expected expression after string")
            return Statement.SetSpend(key, expr)
        }
        index = start
        return null
    }

    private fun console(): Statement.Console? {
        val start = index
        if (match(TokenType.CONSOLE)) {
            val expr = bitwise() ?: throw ParseException("Expected expression after CONSOLE")
            return Statement.Console(expr)
        }
        index = start
        return null
    }

    private fun ifStmt(): Statement.If? {
        val start = index
        if (match(TokenType.IF)) {
            consume(TokenType.LPAREN, "Expected '(' after IF")
            val condition = comparison() ?: throw ParseException("Expected condition in IF")
            consume(TokenType.RPAREN, "Expected ')' after IF condition")
            consume(TokenType.LBRACE, "Expected '{' after IF")
            val thenBranch = expressions()
            consume(TokenType.RBRACE, "Expected '}' after IF block")
            val elseBranch = if (match(TokenType.ELSE)) {
                consume(TokenType.LBRACE, "Expected '{' after ELSE")
                val elseStmts = expressions()
                consume(TokenType.RBRACE, "Expected '}' after ELSE block")
                elseStmts
            } else null
            return Statement.If(condition, thenBranch, elseBranch)
        }
        index = start
        return null
    }

    private fun comparison(): Expr? {
        val left = bitwise() ?: return null
        return comparisonPrime(left)
    }

    private fun comparisonPrime(left: Expr): Expr {
        val start = index
        return when {
            match(TokenType.GT, TokenType.LT, TokenType.EQ, TokenType.NEQ) -> {
                val op = tokens[index - 1].type
                val right = bitwise() ?: throw ParseException("Expected right-hand side of comparison")
                Expr.Binary(left, op, right)
            }
            else -> {
                index = start
                left
            }
        }
    }

    private fun forLoop(): Statement.For? {
        val start = index
        if (match(TokenType.FOR)) {
            consume(TokenType.LPAREN, "Expected '(' after FOR")
            val init = assign() ?: throw ParseException("Expected assignment in FOR")
            consume(TokenType.TO, "Expected TO in FOR")
            val limit = bitwise() ?: throw ParseException("Expected limit expression in FOR")
            consume(TokenType.RPAREN, "Expected ')' after FOR")
            consume(TokenType.LBRACE, "Expected '{' to start FOR body")
            val body = expressions()
            consume(TokenType.RBRACE, "Expected '}' to close FOR")
            return Statement.For(init, limit, body)
        }
        index = start
        return null
    }

    private fun function(): Statement.Fun? {
        val start = index
        if (match(TokenType.FUN)) {
            val name = consume(TokenType.STRING, "Expected function name").text
            consume(TokenType.LPAREN, "Expected '(' after function name")
            val params = parseParameters()
            consume(TokenType.RPAREN, "Expected ')' after parameters")
            consume(TokenType.LBRACE, "Expected '{' before function body")
            val body = expressions()
            consume(TokenType.RBRACE, "Expected '}' to close function")
            return Statement.Fun(name, params, body)
        }
        index = start
        return null
    }

    private fun parseParameters(): List<String> {
        val param = consume(TokenType.VARIABLE, "Expected parameter name").text
        val rest = parseParametersRest()
        return listOf(param) + rest
    }

    private fun parseParametersRest(): List<String> {
        if (match(TokenType.COMMA)) {
            val param = consume(TokenType.VARIABLE, "Expected parameter name after ','").text
            return listOf(param) + parseParametersRest()
        }
        return emptyList()
    }

    private fun block(): Statement.Block? {
        val start = index
        val type = when {
            match(TokenType.CITY) -> "CITY"
            match(TokenType.ROAD) -> "ROAD"
            match(TokenType.BUILDING) -> "BUILDING"
            match(TokenType.LOCATION) -> "LOCATION"
            else -> null
        } ?: run {
            index = start
            return null
        }

        val name = consume(TokenType.STRING, "Expected name string after block type").text

        var locationType: String? = null
        var locationValue: Double? = null

        if (type == "LOCATION") {
            locationType = consume(TokenType.TYPE, "Expected TYPE after location name").text
            locationValue = consume(TokenType.REAL, "Expected REAL after TYPE in location block").text.toDouble()
        }

        consume(TokenType.LBRACE, "Expected '{' after block declaration")
        val body = expressions()
        consume(TokenType.RBRACE, "Expected '}' to close block")

        return Statement.Block(type, name, body, locationType, locationValue)
    }


    private fun draw(): Statement.DrawCommand? {
        val start = index
        val shape = when {
            match(TokenType.LINE) -> "LINE"
            match(TokenType.BOX) -> "BOX"
            match(TokenType.CIRCLE) -> "CIRCLE"
            match(TokenType.BEND) -> "BEND"
            match(TokenType.POINT) -> "POINT"
            else -> null
        } ?: run {
            index = start
            return null
        }
        consume(TokenType.LPAREN, "Expected '(' after $shape")
        val args = parseDrawArguments()
        consume(TokenType.RPAREN, "Expected ')' after arguments")
        return Statement.DrawCommand(shape, args)
    }

    private fun parseDrawArguments(): List<ASTNode> {
        val arg = point() ?: bitwise() ?: return emptyList()
        val rest = parseDrawArgumentsPrime()
        return listOf(arg) + rest
    }

    private fun parseDrawArgumentsPrime(): List<ASTNode> {
        if (match(TokenType.COMMA)) {
            val arg = point() ?: bitwise() ?: throw ParseException("Invalid argument in draw command")
            return listOf(arg) + parseDrawArgumentsPrime()
        }
        return emptyList()
    }

    private fun point(): Expr? {
        val start = index
        if (!match(TokenType.LPAREN)) return null
        val x = bitwise() ?: throw ParseException("Expected x in point")
        consume(TokenType.COMMA, "Expected ',' in point")
        val y = bitwise() ?: throw ParseException("Expected y in point")
        consume(TokenType.RPAREN, "Expected ')' after point")
        return Expr.Binary(x, TokenType.COMMA, y)
    }

    private fun bitwise(): Expr? = bitwisePrime(additive())

    private fun bitwisePrime(left: Expr?): Expr? {
        if (left == null) return null
        val start = index
        if (match(TokenType.BWAND, TokenType.BWOR)) {
            val op = tokens[index - 1].type
            val right = additive() ?: throw ParseException("Expected RHS after bitwise operator")
            return bitwisePrime(Expr.Binary(left, op, right))
        }
        index = start
        return left
    }

    private fun additive(): BigDecimal {
        val value = multiplicative()

        return additivePrime(value)
    }

    private fun additivePrime(output: BigDecimal): BigDecimal {
        if (match(TokenType.PLUS)) {
            val value = multiplicative()

            return additivePrime(output + value)
        } else if (match(TokenType.MINUS)) {
            val value = multiplicative()

            return additivePrime(output - value)
        }

        return output
    }

    private fun multiplicative(): BigDecimal {
        val value = unary()

        return multiplicativePrime(value)
    }

    private fun multiplicativePrime(output: BigDecimal): BigDecimal {
        if (match(TokenType.MUL)) {
            val value = unary()
            return multiplicativePrime(output.multiply(value))
        } else if (match(TokenType.DIV)) {
            val value = unary()

            if (value == BigDecimal.ZERO) throw ParseException("Division by zero")
            return multiplicativePrime(output.divide(value))
        }

        return output
    }

    private fun unary(): BigDecimal {
        if (match(TokenType.MINUS)) {
            val value = primary()
            return -value
        }
        return primary()
    }

    private fun primary(): BigDecimal {
        if (match(TokenType.INT)) {
            return BigDecimal.valueOf(tokens[index - 1].text.toLong())
        } else if (match(TokenType.REAL)) {
            return BigDecimal.valueOf(tokens[index - 1].text.toDouble())
        } else if (match(TokenType.VARIABLE)) {
            val variable = variables[tokens[index - 1].text]
            if (variable == null) {
                throw ParseException("Variable not found")
            }

            return variables[tokens[index - 1].text]!!
        } else if (match(TokenType.LPAREN)) {
            val expr = bitwise() ?: throw ParseException("Expected expression")
            consume(TokenType.RPAREN, "Expected ')' after expression")

            return expr
        } else if (match(TokenType.GET_SPENT)) {
            val key = consume(TokenType.STRING, "Expected string after GET_SPENT").text

            if (locations[key]?.locationType != null) {
                return locations[key]?.locationValue!!
            }

            throw ParseException("A location with the name of ${key} doesn't exist!")
        } else {
            throw ParseException("Failed in Primary / else")
        }
    }
}