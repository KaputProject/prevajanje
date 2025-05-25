enum class TokenType {
    // Keywords
    LET, IF, ELSE, FOR, IN,
    TRANSACTION, ACCOUNT, LINK, SET, SHOW, ALERT, HIGHLIGHT, NEIGH,
    FST, SND,

    // Literals
    INT, REAL, STRING, DATE, NIL,

    // Symbols
    LBRACE, RBRACE, LPAREN, RPAREN,
    COLON, SEMICOLON, COMMA, ASSIGN,
    PLUS, MINUS, MUL, DIV,
    BWAND, BWOR,

    // Operators
    LT, GT, EQ, NEQ, NOT,

    // Get
    GET_SPENT, SET_SPENT,

    // Graph
    LINE, BEND, BOX, CIRCLE, POINT,

    // Functions
    FUN, CALL, TO, CONSOLE, CITY, ROAD, BUILDING, LOCATION,

    // Others
    VARIABLE, EOF, ERROR, TYPE
}
