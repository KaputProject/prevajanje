~~## Osnovni tipi:

Enota: nil
Realna števila: 1.2 (amount)
Nizi: "niz"
Koordinate: (X, Y)
TYPE: "restavracija", "park", "trgovina", "crpalka", "bolnišnica", "kavarna", "bar", "diskoteka", "klub"

## Bloki:

city "niz" {
    COMMANDS
}

road "niz" {
    COMMANDS
}

building "niz" {
    COMMANDS
}

location "niz" TYPE AMOUNT {
    COMMANDS
}

## Ukazi:
line(POINT, POINT)

bend(POINT, POINT, ANGLE)

box(POINT, POINT)

circle(POINT, RADIUS)

point(POINT)

## Ostali konstrukti:
ASSIGN: "niz" = Bitwise

spremenljivka: let ASSIGN

if ( CONDITION ) {
    COMMANDS
} else {
    COMMANDS
}

for (ASSIGN to PRIMARY) {
    COMMANDS
}

fun "niz" (PARAMS) {
    COMMANDS
}

## Gramatika:
```
Program ::= Expressions EOF

Expressions     ::= Expr Expressions'
Expressions'    ::= Expr Expressions' | ε

Expr            ::= Assign
                | For
                | Console
                | If
                | Fun
                | Block
                | SetSpent
                | Draw

// Kommande za risanje na zemljevidu
Draw            ::= 'line' '(' Point ',' Point ')'
                | 'bend' '(' Point ',' Point ',' Bitwise ')'
                | 'box' '(' Point ',' Point ')'
                | 'circle' '(' Point ',' Bitwise ')'
                | 'point' '(' Point ')'

// SetSpent nastavi vrednost spent na določeni lokaciji
SetSpent        ::= 'set_spent' STRING Bitwise

Bitwise         ::= Additive Bitwise'
Bitwise'        ::= 'bwand' Additive Bitwise'
                | 'bwor' Additive Bitwise'
                | ε

Additive        ::= Multiplicative Additive'
Additive'       ::= 'plus' Multiplicative Additive'
                | 'minus' Multiplicative Additive'
                | ε

Multiplicative  ::= Unary Multiplicative'
Multiplicative' ::= '*' Unary Multiplicative'
                | '/' Unary Multiplicative'
                | ε

Unary           ::= '+' Primary
                | '-' Primary
                | Primary

Primary         ::= INT
                | REAL
                | VARIABLE
                | '(' Bitwise ')'
                | GetSpent

// GetSpent vrne REAL, ki predstavlja tisto REAL polje, ki pripada lokaciji z določenim imenom
GetSpent       ::= get_spent STRING

Assign          ::= let VARIABLE '=' Bitwise

For             ::= 'for' '(' Assign 'to' Bitwise ')' '{' Expressions '}'

Console         ::= 'console' Bitwise

If              ::= 'if' '(' Comparison ')' '{' Expressions '}' Else
Else        ::= 'else' '{' Expressions '}' | ε

Comparison ::= Bitwise Comparison'
Comparison' ::= '>' Bitwise | '<' Bitwise | '==' Bitwise | '!=' Bitwise | ε

// Tu bo treba handlat scope spremenljik
Fun             ::= 'fun' STRING '(' Params ')' '{' Expressions '}'
Params          ::= VARIABLE Params' | ε
Params'         ::= ',' VARIABLE Params' | ε

Block           ::= 'city' STRING '{' Expressions '}'
                | 'road' STRING '{' Expressions '}'
                | 'building' STRING '{' Expressions '}'
                | 'location' STRING TYPE REAL '{' Expressions '}'

Point           ::= '(' Bitwise ',' Bitwise ')'

TYPE            ::= 'restaurant'
                | 'park'
                | 'shop'
                | 'gas_station'
                | 'hospital'
                | 'cafe'
                | 'bar'
                | 'nightclub'
                | 'club'

REAL            ::= [0-9]+ '.' [0-9]+
STRING          ::= '"' .*? '"'
VARIABLE        ::= [a-zA-Z_][a-zA-Z0-9_]*~~
```