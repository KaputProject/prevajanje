class Lexer(private val input: String) {
    private var pos = 0
    private var line = 1
    private var column = 1

    private val keywords = mapOf(
        "let" to TokenType.LET,
        "if" to TokenType.IF,
        "else" to TokenType.ELSE,
        "for" to TokenType.FOR,
        "in" to TokenType.IN,
        "transaction" to TokenType.TRANSACTION,
        "account" to TokenType.ACCOUNT,
        "link" to TokenType.LINK,
        "set" to TokenType.SET,
        "show" to TokenType.SHOW,
        "alert" to TokenType.ALERT,
        "highlight" to TokenType.HIGHLIGHT,
        "neigh" to TokenType.NEIGH,
        "fst" to TokenType.FST,
        "snd" to TokenType.SND,
        "nil" to TokenType.NIL,
        "bwand" to TokenType.BWAND,
        "bwor" to TokenType.BWOR,
        "get_spent" to TokenType.GET_SPENT,
        "set_spent" to TokenType.SET_SPENT,
        "line" to TokenType.LINE,
        "bend" to TokenType.BEND,
        "box" to TokenType.BOX,
        "circle" to TokenType.CIRCLE,
        "point" to TokenType.POINT,
        "fun" to TokenType.FUN,
        "to" to TokenType.TO,
        "console" to TokenType.CONSOLE,
        "city" to TokenType.CITY,
        "road" to TokenType.ROAD,
        "building" to TokenType.BUILDING,
        "location" to TokenType.LOCATION,
        "restaurant" to TokenType.TYPE,
        "park" to TokenType.TYPE,
        "shop" to TokenType.TYPE,
        "gas_station" to TokenType.TYPE,
        "hospital" to TokenType.TYPE,
        "cafe" to TokenType.TYPE,
        "bar" to TokenType.TYPE,
        "nightclub" to TokenType.TYPE,
        "club" to TokenType.TYPE,
    )

    fun tokenize(): List<Token> {
        val tokens = mutableListOf<Token>()
        while (true) {
            skipWhitespace()
            if (pos >= input.length) {
                tokens.add(Token(TokenType.EOF, "", line, column))
                break
            }

            val token = consumeDFA()
            tokens.add(token)
            if (token.type == TokenType.EOF || token.type == TokenType.ERROR) break
        }
        return tokens
    }

    private fun peek(offset: Int = 0): Char =
        if (pos + offset < input.length) input[pos + offset] else '\u0000'

    private fun advance(): Char {
        val ch = peek()
        pos++
        if (ch == '\n') {
            line++
            column = 1
        } else {
            column++
        }
        return ch
    }

    private fun skipWhitespace() {
        while (peek().isWhitespace()) advance()
    }

    private enum class CharCategory {
        DIGIT, LETTER, DOT, QUOTE, EQ, EXCL, LT, GT, PLUS, MINUS, STAR, SLASH,
        UNDERSCORE, LPAREN, RPAREN, LBRACE, RBRACE, COLON, SEMICOLON, COMMA, OTHER
    }

    private fun categorize(ch: Char): CharCategory = when (ch) {
        in '0'..'9' -> CharCategory.DIGIT
        in 'a'..'z', in 'A'..'Z' -> CharCategory.LETTER
        '.' -> CharCategory.DOT
        '"' -> CharCategory.QUOTE
        '=' -> CharCategory.EQ
        '!' -> CharCategory.EXCL
        '<' -> CharCategory.LT
        '>' -> CharCategory.GT
        '+' -> CharCategory.PLUS
        '-' -> CharCategory.MINUS
        '*' -> CharCategory.STAR
        '/' -> CharCategory.SLASH
        '_' -> CharCategory.UNDERSCORE
        '(' -> CharCategory.LPAREN
        ')' -> CharCategory.RPAREN
        '{' -> CharCategory.LBRACE
        '}' -> CharCategory.RBRACE
        ':' -> CharCategory.COLON
        ';' -> CharCategory.SEMICOLON
        ',' -> CharCategory.COMMA
        else -> CharCategory.OTHER
    }

    // STATE TABLE DFA
    private val acceptingStates = mapOf(
        1 to TokenType.INT,
        3 to TokenType.REAL,
        4 to TokenType.VARIABLE,
        6 to TokenType.STRING,
        7 to TokenType.ASSIGN,
        //8 to TokenType.ASSIGN,
        9 to TokenType.EQ,
        10 to TokenType.NOT,
        11 to TokenType.NEQ,
        12 to TokenType.LT,
        13 to TokenType.GT,
        14 to TokenType.PLUS,
        15 to TokenType.MINUS,
        16 to TokenType.MUL,
        17 to TokenType.DIV,
        18 to TokenType.LPAREN,
        19 to TokenType.RPAREN,
        20 to TokenType.LBRACE,
        21 to TokenType.RBRACE,
        22 to TokenType.COLON,
        23 to TokenType.SEMICOLON,
        24 to TokenType.COMMA
    )

    private val dfaTable = mapOf(
        0 to mapOf(
            CharCategory.DIGIT to 1,
            CharCategory.DOT to 99,
            CharCategory.LETTER to 4,
            CharCategory.UNDERSCORE to 4,
            CharCategory.QUOTE to 5,
            CharCategory.EQ to 7,
            CharCategory.EXCL to 10,
            CharCategory.LT to 12,
            CharCategory.GT to 13,
            CharCategory.PLUS to 14,
            CharCategory.MINUS to 15,
            CharCategory.STAR to 16,
            CharCategory.SLASH to 17,
            CharCategory.LPAREN to 18,
            CharCategory.RPAREN to 19,
            CharCategory.LBRACE to 20,
            CharCategory.RBRACE to 21,
            CharCategory.COLON to 22,
            CharCategory.SEMICOLON to 23,
            CharCategory.COMMA to 24
        ),
        1 to mapOf(CharCategory.DIGIT to 1, CharCategory.DOT to 2),
        2 to mapOf(CharCategory.DIGIT to 3),
        3 to mapOf(CharCategory.DIGIT to 3),
        4 to mapOf(CharCategory.LETTER to 4, CharCategory.DIGIT to 4, CharCategory.UNDERSCORE to 4),
        //5 to mapOf(CharCategory.QUOTE to 6, CharCategory.OTHER to 5, CharCategory.LETTER to 5, CharCategory.DIGIT to 5, CharCategory.DOT to 5, CharCategory.UNDERSCORE to 5, CharCategory.MINUS to 5),
        7 to mapOf(CharCategory.EQ to 9),
        10 to mapOf(CharCategory.EQ to 11)
    )

    private fun consumeDFA(): Token {
        val sb = StringBuilder()
        val startLine = line
        val startCol = column
        var state = 0
        var currentPos = pos

        loop@ while (currentPos < input.length) {
            val ch = input[currentPos]

            // Poseben primer: v string literal načinu (state 5)
            if (state == 5) {
                sb.append(ch)
                currentPos++
                if (ch == '"') {
                    state = 6 // zaključen niz
                    break@loop
                }
                continue@loop
            }

            val cat = categorize(ch)
            val next = dfaTable[state]?.get(cat) ?: break@loop
            sb.append(ch)
            state = next
            currentPos++
        }

        // Premakni kazalec (pos) do mesta, kjer smo zaključili z branjem
        while (pos < currentPos) advance()

        val text = sb.toString()

        // Če stanje ni sprejemno, vrni napako
        val type = acceptingStates[state] ?: return Token(TokenType.ERROR, text, startLine, startCol)

        // Če je IDENTIFIER, preveri ali je to dejansko ključna beseda
        val finalType = when (type) {
            TokenType.VARIABLE -> keywords[text] ?: TokenType.VARIABLE
            TokenType.STRING -> TokenType.STRING
            else -> type
        }

        // Če je niz ali datum, odstrani narekovaje
        val value = when (finalType) {
            TokenType.STRING -> text.drop(1).dropLast(1)
            TokenType.DATE -> text.drop(1).dropLast(1)
            else -> text
        }

        return Token(finalType, value, startLine, startCol)
    }
}