import classes.*
import geojson.*
import java.math.BigDecimal

class ParseException(message: String) : RuntimeException(message)

data class Function(
    var name: String? = null,
    var start: Int? = null,
    var end: Int? = null,
    var params: List<String> = emptyList(),
)

class Evaluator2(private val tokens: List<Token>, private val variables: MutableMap<String, BigDecimal> = mutableMapOf()) {
    private var index = 0
    private var blocks: MutableMap<String, Block> = mutableMapOf()
    private var currentBlock: String? = null
    private var functions: MutableMap<String, Function> = mutableMapOf()
    private var returnIndex = 0
    private var breakIndex: Int? = null

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

        if (breakIndex != null && index == breakIndex) {
            return
        }

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
        } else if (match(TokenType.SET_SPENT)) {
            setSpent()
        } else if (match(TokenType.LINE) || match(TokenType.BEND) || match(TokenType.BOX) || match(TokenType.CIRCLE) || match(TokenType.POINT)) {
            draw(tokens[index - 1])
        } else if (match(TokenType.CALL)) {
            call()
        } else if (match(TokenType.FUN)) {
            function()
        } else {
            return false
        }

        return true
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

    private fun function() {
        try {
            val newFun: Function = Function()
            val name = consume(TokenType.STRING, "Expected function name").text
            newFun.name = name

            consume(TokenType.LPAREN, "Expected '(' after function name")

            newFun.params = parameters()

            consume(TokenType.RPAREN, "Expected ')' after parameters")
            consume(TokenType.LBRACE, "Expected '{' before function body")

            newFun.start = this.index

            skip()

            newFun.end = this.index - 2

            functions[name] = newFun
        } catch (e: Exception) {
            throw ParseException("Error in function: ${e.message}")
        }
    }

    private fun parameters(): List<String> {
        if (!match(TokenType.VARIABLE)) {
            return emptyList()
        }

        index -= 1
        val param = consume(TokenType.VARIABLE, "Expected parameter name").text
        val rest = parametersPrime()
        return listOf(param) + rest
    }

    private fun parametersPrime(): List<String> {
        if (match(TokenType.COMMA)) {
            val param = consume(TokenType.VARIABLE, "Expected parameter name after ','").text
            return listOf(param) + parametersPrime()
        }
        return emptyList()
    }

    private fun call() {
        val function = functions[consume(TokenType.STRING, "Expected function index").text] ?: throw ParseException("Function not found")

        consume(TokenType.LPAREN, "Expected '(' after function name")
        val passedParams = parameters()
        consume(TokenType.RPAREN, "Expected ')' after parameters")

        if (passedParams.toSet() != function.params.toSet()) {
            throw ParseException("Function '${function.name}' called with incorrect parameters.")
        }

        returnIndex = this.index

        this.index = function.start!!
        this.breakIndex = function.end!!

        expressions()

        this.breakIndex = null
        this.index = returnIndex
    }

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

                newBlock = Location(type, name, mutableListOf(), locationType, locationValue)
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

    private fun draw(token: Token) {
        consume(TokenType.LPAREN, "Expected '(' after ${token.type}")

        val shape = when {
            token.type == TokenType.POINT -> {
                point()
            }
            token.type == TokenType.LINE -> {
                val p1 = point()
                consume(TokenType.COMMA, "Expected ',' between points in  ${token.type}")
                val p2 = point()

                Line(p1,p2)
            }
            token.type == TokenType.BOX -> {
                val p1 = point()
                consume(TokenType.COMMA, "Expected ',' between points in  ${token.type}")
                val p2 = point()

                Box(p1,p2)
            }
            token.type == TokenType.BEND -> {
                val p1 = point()
                consume(TokenType.COMMA, "Expected ',' between points in  ${token.type}")
                val p2 = point()
                consume(TokenType.COMMA, "Expected ',' between points in  ${token.type}")
                val factor = bitwise()

                Bend(p1, p2, factor)
            }
            token.type == TokenType.CIRCLE -> {
                val p1 = point()
                consume(TokenType.COMMA, "Expected ',' between points in  ${token.type}")
                val factor = bitwise()

                Circle(p1, factor)
            }
            else -> throw ParseException("Unexpected draw type: ${token.type}")
        }

        consume(TokenType.RPAREN, "Expected ')' after draw arguments")

        if (currentBlock != null) {
            blocks[currentBlock]?.body?.add(shape)
        }
    }

    private fun point(): Point {
        try {
            if (!match(TokenType.LPAREN)) throw ParseException("Expecting ( at the start of point")

            val x = bitwise()
            consume(TokenType.COMMA, "Expected ',' in point")
            val y = bitwise()
            consume(TokenType.RPAREN, "Expected ')' after point")

            return Point(x, y)
        } catch (e: Exception) {
            throw ParseException("Error in POINT: ${e.message}")
        }
    }

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