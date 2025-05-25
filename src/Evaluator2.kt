import classes.*
import java.math.BigDecimal

open class Block(
    val type: String,
    val name: String,
    val body: List<Statement> = listOf(),
)

class Location(
    type: String,
    name: String,
    body: List<Statement>,
    val locationType: String? = null,
    var locationValue: BigDecimal? = null,
) : Block(type, name, body)


class Evaluator2(private val tokens: List<Token>, private val variables: MutableMap<String, BigDecimal> = mutableMapOf()) {
    private var index = 0
    private var blocks: MutableMap<String, Block> = mutableMapOf()
    private var currentBlock: String? = null

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

    fun program(): MutableMap<String, Block> {
        expressions()
        consume(TokenType.EOF, "Expected end of file, index ${index}")

        return blocks
    }

    private fun expressions() {
        if (index >= tokens.size || tokens[index].type == TokenType.EOF) return

        if (expr()) {
            expressions()
        }
    }

    private fun expr(): Boolean {
        if (match(TokenType.LET)) {
            assign()
        } else if (match(TokenType.CONSOLE)) {
            console()
        } else if (match(TokenType.IF)) {
            ifStmt()
        } else if (match(TokenType.FOR)) {
            forLoop()
        } else if (match(TokenType.CITY) || match(TokenType.ROAD) || match(TokenType.BUILDING) || match(TokenType.LOCATION)) {
            block()
        } else if (match(TokenType.SET_SPENT)){
            setSpent()
        } else {
            return false
        }

        return true
        //return assign() ?: console() ?: ifStmt() ?: forLoop() ?: function() ?: setSpend() ?: draw() ?: block()
    }

    private fun assign(): String {
        val name = consume(TokenType.VARIABLE, "Expected variable after LET").text
        consume(TokenType.ASSIGN, "Expected '=' after variable")

        try {
            val value = bitwise()
            variables[name] = value
            return name
        } catch (e: Exception) {
            throw ParseException("The bitwise call inside assign has failed with message: ${e.message}")
        }
    }

    private fun setSpent() {
        val key = consume(TokenType.STRING, "Expected string after SET_SPENT").text
        val value = bitwise()

        val location = blocks[key]
        if (location is Location) {
            location.locationValue = BigDecimal(value.toDouble())
        } else {
            throw ParseException("SET_SPENT failed: '$key' is not a valid Location.")
        }
    }

    private fun console() {
        try {
            val value = bitwise()

            println("CONSOLE: $value")
        } catch (e: Exception) {
            throw ParseException("The bitwise call inside CONSOLE has failed with message: ${e.message}")
        }
    }

    private fun ifStmt() {
        consume(TokenType.LPAREN, "Expected '(' after IF")

        try {
            val condition = comparison()

            if (condition) {
                consume(TokenType.RPAREN, "Expected ')' after IF")
                consume(TokenType.LBRACE, "Expected '{' after condition in IF")
                expressions()

                consume(TokenType.RBRACE, "Expected '}' after condition in IF")

                if (match(TokenType.ELSE)) {
                    consume(TokenType.LBRACE, "Expected '}' after condition in IF")
                    skip()
                }

            } else {
                consume(TokenType.RPAREN, "Expected ')' after IF")
                consume(TokenType.LBRACE, "Expected '{' after condition in IF")

                skip()

                if (match(TokenType.ELSE)) {
                    elseStmt()
                }
            }
        } catch (e: Exception) {
            throw ParseException("IF has failed with message: ${e.message}")
        }
    }

    private fun skip() {
        var lbrace = 1
        var rbrace = 0
        while (this.index < tokens.size - 1) {
            if (match(TokenType.RBRACE)) {
                rbrace += 1

                if (rbrace == lbrace) {
                    return
                }

            } else if (match(TokenType.LBRACE)) {
                lbrace += 1
            }
            this.index += 1
        }
    }

    private fun elseStmt() {
        consume(TokenType.LBRACE, "Expected { after ELSE")
        expressions()
        consume(TokenType.RBRACE, "Expected } after expressions in ELSE")
    }

    private fun comparison(): Boolean {
        val left = bitwise()

        return comparisonPrime(left)
    }

    private fun comparisonPrime(left: BigDecimal): Boolean {
        if (match(TokenType.GT)) {
            return left > bitwise()
        } else if (match(TokenType.LT)) {
            return left < bitwise()
        } else if (match(TokenType.EQ)) {
            return left == bitwise()
        } else if (match(TokenType.NEQ)) {
            return left != bitwise()
        } else {
            throw ParseException("Unexpected comparison operator")
        }
    }

    private fun forLoop() {
        try {
            consume(TokenType.LPAREN, "Expected '(' after FOR")
            consume(TokenType.LET, "Expected 'let' after (")
            val from = assign()

            consume(TokenType.TO, "Expected TO in FOR")
            val limit = bitwise().toInt()

            consume(TokenType.RPAREN, "Expected ')' after FOR")
            consume(TokenType.LBRACE, "Expected '{' to start FOR body")

            val start = this.index
            while (variables[from]!!.toInt() <= limit) {
                this.index = start
                expressions()
                variables[from] = variables[from]!! + BigDecimal(1)
            }

            consume(TokenType.RBRACE, "Expected '}' to close FOR")
        } catch (e: Exception) {
            throw ParseException("Error in FOR: ${e.message}")
        }
    }

//    private fun function(): Statement.Fun? {
//        val start = index
//        if (match(TokenType.FUN)) {
//            val name = consume(TokenType.STRING, "Expected function name").text
//            consume(TokenType.LPAREN, "Expected '(' after function name")
//            val params = parseParameters()
//            consume(TokenType.RPAREN, "Expected ')' after parameters")
//            consume(TokenType.LBRACE, "Expected '{' before function body")
//            val body = expressions()
//            consume(TokenType.RBRACE, "Expected '}' to close function")
//            return Statement.Fun(name, params, body)
//        }
//        index = start
//        return null
//    }

//    private fun parseParameters(): List<String> {
//        val param = consume(TokenType.VARIABLE, "Expected parameter name").text
//        val rest = parseParametersRest()
//        return listOf(param) + rest
//    }
//
//    private fun parseParametersRest(): List<String> {
//        if (match(TokenType.COMMA)) {
//            val param = consume(TokenType.VARIABLE, "Expected parameter name after ','").text
//            return listOf(param) + parseParametersRest()
//        }
//        return emptyList()
//    }

    private fun block() {
        try {
            this.index -= 1
            val type = when {
                match(TokenType.CITY) -> "CITY"
                match(TokenType.ROAD) -> "ROAD"
                match(TokenType.BUILDING) -> "BUILDING"
                match(TokenType.LOCATION) -> "LOCATION"
                else -> throw ParseException("Expected block type (CITY, ROAD, BUILDING, LOCATION)")
            }

            val name = consume(TokenType.STRING, "Expected name string after block type").text
            var newBlock: Block? = null

            var locationType: String? = null
            var locationValue: BigDecimal? = null

            if (type == "LOCATION") {
                locationType = consume(TokenType.TYPE, "Expected TYPE after location name").text
                locationValue = BigDecimal(consume(TokenType.REAL, "Expected REAL after TYPE in location block").text)

                newBlock = Location(type, name, listOf(), locationType, locationValue)
            } else {
                newBlock = Block(type, name)
            }

            currentBlock = newBlock.name
            blocks.put(newBlock.name, newBlock)

            consume(TokenType.LBRACE, "Expected '{' after block declaration")
            expressions()
            consume(TokenType.RBRACE, "Expected '}' to close block")

            currentBlock = null
        } catch (e: Exception) {
            throw ParseException("Error in BLOCK: ${e.message}")
        }
    }


//    private fun draw(): Statement.DrawCommand? {
//        val start = index
//        val shape = when {
//            match(TokenType.LINE) -> "LINE"
//            match(TokenType.BOX) -> "BOX"
//            match(TokenType.CIRCLE) -> "CIRCLE"
//            match(TokenType.BEND) -> "BEND"
//            match(TokenType.POINT) -> "POINT"
//            else -> null
//        } ?: run {
//            index = start
//            return null
//        }
//        consume(TokenType.LPAREN, "Expected '(' after $shape")
//        val args = parseDrawArguments()
//        consume(TokenType.RPAREN, "Expected ')' after arguments")
//        return Statement.DrawCommand(shape, args)
//    }

//    private fun parseDrawArguments(): List<ASTNode> {
//        val arg = point() ?: bitwise() ?: return emptyList()
//        val rest = parseDrawArgumentsPrime()
//        return listOf(arg) + rest
//    }

//    private fun parseDrawArgumentsPrime(): List<ASTNode> {
//        if (match(TokenType.COMMA)) {
//            val arg = point() ?: bitwise() ?: throw ParseException("Invalid argument in draw command")
//            return listOf(arg) + parseDrawArgumentsPrime()
//        }
//        return emptyList()
//    }

//    private fun point(): Expr? {
//        val start = index
//        if (!match(TokenType.LPAREN)) return null
//        val x = bitwise()
//        consume(TokenType.COMMA, "Expected ',' in point")
//        val y = bitwise()
//        consume(TokenType.RPAREN, "Expected ')' after point")
//        return Expr.Binary(x, TokenType.COMMA, y)
//    }

    private fun bitwise(): BigDecimal {
        val value = additive()

        return bitwisePrime(value)
    }

    private fun bitwisePrime(output: BigDecimal): BigDecimal {
        if (match(TokenType.BWAND)) {
            val value = additive()

            return bitwisePrime((output.toBigInteger().and(value.toBigInteger())).toBigDecimal())
        } else if (match(TokenType.BWOR)) {
            val value = additive()

            return bitwisePrime((output.toBigInteger().or(value.toBigInteger())).toBigDecimal())
        }

        return output
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
            val expr = bitwise()
            consume(TokenType.RPAREN, "Expected ')' after expression")

            return expr
        } else if (match(TokenType.GET_SPENT)) {
            val key = consume(TokenType.STRING, "Expected string after GET_SPENT").text

            val location = blocks[key]
            if (location is Location && location.locationValue != null) {
                return location.locationValue!!
            }

            throw ParseException("A location with the name of ${key} doesn't exist!")
        } else {
            throw ParseException("Failed in Primary / else")
        }
    }
}