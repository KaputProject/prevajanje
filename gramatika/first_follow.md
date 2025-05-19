### Terminali
```
'line' 'bend' 'box' 'circle' 'point' 'set_spent' STRING INT REAL VARIABLE 
'+' '-' '*' '/' 'bwand' 'bwor', plus, minus
'let' '=' 'for' 'to' 'console' 'if' 'else' 
'>' '<' '==' '!='
'fun' '(' ')' '{' '}' ',' 
'city' 'road' 'building' 'location' TYPE
```

### Neterminali:
```
Point, Block, Params', Params, Fun, Comparison', Comparison, Else, If, 
Console, For, Assign, GetSpent, SetSpent, Draw
Primary, Unary, Multiplicative, Multiplicative', Additive, Additive, Bitwise, Bitwise'
Expr, Expressions
```

### First:
```
First(Primary) = { INT, REAL, VARIABLE, (, get_spent }
First(Unary) = { +, -, INT, REAL, VARIABLE, (, get_spent }

First(Multiplicative) = { +, -, INT, REAL, VARIABLE, (, get_spent }
First(Multiplicative') = { *, /, ε}

First(Additive) = { +, -, INT, REAL, VARIABLE, (, get_spent }
First(Additive') = { 'plus', 'minus', ε}

First(Bitwise) = { +, -, INT, REAL, VARIABLE, (, get_spent }
First(Bitwise') = { bwand, bwor, ε }

First(Draw) = { line, bend, box, circle, point }

First(SetSpent) = { set_spent }
First(GetSpent) = { get_spent }

First(Assign) = { let }
First(For) = { for }
First(Console) = { console }

First(If) = { if }
First(Else) = { else, ε }
First(Comparison) = { +, -, INT, REAL, VARIABLE, (, get_spent }
First(Comparison') = { >, <, ==, !=, ε }

First(Fun) = { fun }
First(Params) = { VARIABLE, ε }
First(Params') = { ',', ε }

First(Block) = { city, road, building, location }

First(Point) = { '(' }

First(Expr) = First(Bitwise) ∪ First(Assign) ∪ First(For) ∪ First(Console) ∪ First(If) ∪ First(Fun) ∪ First(Block) ∪ First(SetSpent) ∪ First(Draw)
= { +, -, INT, REAL, VARIABLE, (, get_spent, let, for, console, if, fun, city, road, building, location, 
    set_spent, line, bend, box, circle, point }

First(Expressions) = First(Expr) ∪ { ε }
= { +, -, INT, REAL, VARIABLE, (, get_spent, let, for, console, if, fun, city, road, building, location, 
    set_spent, line, bend, box, circle, point, ε}
```

### Follow:
```
FOLLOW(Expressions) = { $, '}' }

FOLLOW(Expr) = FIRST(Expr) ∪ FOLLOW(Expressions)
= { '+', '-', INT, REAL, VARIABLE, '(', 'get_spent', 'let', 'for', 'console', 'if',
    'fun', 'city', 'road', 'building', 'location', 'set_spent', 'line', 'bend', 'box',
    'circle', 'point', $, '}' }

FOLLOW(Bitwise) = FOLLOW(Expr) ∪ { ')', ',', 'to' } ∪ FIRST(Comparison') ∪ FOLLOW(Comparison') ∪ FOLLOW(SetSpent) ∪ FOLLOW(Console) ∪ FOLLOW(Assign)
= { '+', '-', INT, REAL, VARIABLE, '(', 'get_spent', 'let', 'for', 'console', 'if',
    'fun', 'city', 'road', 'building', 'location', 'set_spent', 'line', 'bend', 'box',
    'circle', 'point', $, '}', ')', ',', 'to', '>', '<', '==', '!=', }

FOLLOW(Bitwise') = FOLLOW(Bitwise)

FOLLOW(Additive) = FIRST(Bitwise') ∪ FOLLOW(Bitwise)
= { 'bwand', 'bwor', '+', '-', INT, REAL, VARIABLE, '(', 'get_spent', 'let', 'for',
    'console', 'if', 'fun', 'city', 'road', 'building', 'location', 'set_spent',
    'line', 'bend', 'box', 'circle', 'point', $, '}', ')', ',', 'to', '>', '<', '==', '!=', }

FOLLOW(Additive') = FOLLOW(Additive)

FOLLOW(Multiplicative) = FIRST(Additive') ∪ FOLLOW(Additive)
= { '+', '-', 'bwand', 'bwor', INT, REAL, VARIABLE, '(', 'get_spent', 'let', 'for',
    'console', 'if', 'fun', 'city', 'road', 'building', 'location', 'set_spent',
    'line', 'bend', 'box', 'circle', 'point', $, '}', ')', ',', 'to', '>', '<', '==', '!=', }

FOLLOW(Multiplicative') = FOLLOW(Multiplicative)

FOLLOW(Unary) = FIRST(Multiplicative') ∪ FOLLOW(Multiplicative)
= { '*', '/', 'bwand', 'bwor', '+', '-', INT, REAL, VARIABLE, '(', 'get_spent', 'let',
    'for', 'console', 'if', 'fun', 'city', 'road', 'building', 'location', 'set_spent',
    'line', 'bend', 'box', 'circle', 'point', $, '}', ')', ',', 'to', '>', '<', '==', '!=', }

FOLLOW(Primary) = FOLLOW(Unary)

FOLLOW(Draw) = FOLLOW(Expr)

FOLLOW(SetSpent) = FOLLOW(Expr)
FOLLOW(GetSpent) = FOLLOW(Primary)

FOLLOW(Assign) = FOLLOW(Expr) ∪ { 'to' }

FOLLOW(For) = FOLLOW(Expr)

FOLLOW(Console) = FOLLOW(Expr)

FOLLOW(If) = FOLLOW(Expr)
FOLLOW(Else) = FOLLOW(If)

FOLLOW(Comparison) = { ')' }
FOLLOW(Comparison') = FOLLOW(Comparison)

FOLLOW(Fun) = FOLLOW(Expr)
FOLLOW(Params) = { ')' }
FOLLOW(Params') = FOLLOW(Params)

FOLLOW(Block) = FOLLOW(Expr)

FOLLOW(Point) = { ',', ')' }

```

### LL1:
- Expr:
```
FIRST(Expr) = FIRST(Bitwise) ∩ FIRST(Assign) ∩ FIRST(For) ∩ FIRST(Console) ∩ FIRST(If) ∩ FIRST(Fun) ∩ FIRST(Block) ∩ FIRST(SetSpent) ∩ FIRST(Draw) = 
            = { +, -, INT, REAL, VARIABLE, (, get_spent } ∩ { let } ∩ { for } ∩ { console } ∩ { if } ∩ { fun } ∩ { city, road, building, location } ∩ { set_spent } ∪ { line, bend, box, circle, point }
            -> Presek produkcij je prazen, torej ustreza prvemu pogoju
Ker nimamo nobene produkcije ki slika v ε, je to dovolj
```

- Bitwise:
```
FIRST(Bitwise) = FIRST(Additive) = { +, -, INT, REAL, VARIABLE, (, get_spent }
               -> Samo ena produkcija, presek je prazen, torej ustreza prvemu pogoju  
Ker nimamo nobene produkcije ki slika v ε, je to dovolj
```

- Bitwise':
```
FIRST(Bitwise') = { bwand, bwor, ε } 
                -> Ustreza prvemu pogoju, saj je presek vseh produkcij prazen
FOLLOW(Bitwise') = { +, -, INT, REAL, VARIABLE, (, get_spent }  
FIRST(Bitwise') ∩ FOLLOW(Bitwise') = { bwand, bwor } ∩ { +, -, INT, REAL, VARIABLE, (, get_spent } = { }
            -> Presek je prazen, torej ustreza drugemu pogoju
```

- Additive:
```
FIRST(Additive) = FIRST(Multiplicative) = { +, -, INT, REAL, VARIABLE, (, get_spent }  
            -> Samo ena produkcija, presek je prazen, torej ustreza prvemu pogoju  
Ker nimamo ε-produkcij, je to dovolj
```

- Additive':
```
FIRST(Additive') = { plus, minus, ε }
               -> Ustreza prvemu pogoju, saj je presek vseh produkcij prazen
Ker slika v ε, moramo preveriti še presek:
FIRST(Additive') ∩ FOLLOW(Additive') = { plus, minus, ε } ∩ { bwand, bwor, +, -, INT, REAL, VARIABLE, (, get_spent, let, for, console, if, fun, city, road, building, location, set_spent, line, bend, box, circle, point, $, }, >, <, ==, !=, ), ,, to } = { }
               -> Ustreza drugemu pogoju
```

- Multiplicative:
```
FIRST(Multiplicative) = FIRST(Unary) = { +, -, INT, REAL, VARIABLE, (, get_spent }  
            -> Samo ena produkcija, presek je prazen, torej ustreza prvemu pogoju  
Ker nimamo ε-produkcij, je to dovolj
```

- Multiplicative':
```
FIRST(Multiplicative') = { '*', '/', ε }
               -> Ustreza prvemu pogoju, saj je presek vseh produkcij prazen
Ker slika v ε, moramo preveriti še presek:
FIRST(Multiplicative') ∩ FOLLOW(Multiplicative') = { '*', '/', ε } ∩ { bwand, bwor, +, -, INT, REAL, VARIABLE, (, get_spent, let, for, console, if, fun, city, road, building, location, set_spent, line, bend, box, circle, point, $, }, >, <, ==, !=, ), ,, to } = { }
               -> Ustreza drugemu pogoju
```

- Unary:
```
FIRST(Unary) = { +, -, INT, REAL, VARIABLE, (, get_spent }  
            -> Samo ena produkcija, presek je prazen, torej ustreza prvemu pogoju
Ker nimamo ε-produkcij, je to dovolj
```

- Primary:
```
FIRST(Primary) = { INT, REAL, VARIABLE, (, get_spent }
            -> Samo ena produkcija, presek je prazen, torej ustreza prvemu pogoju
Ker nimamo ε-produkcij, je to dovolj
```

- Draw:
```
FIRST(Draw) = { line, bend, box, circle, point }
            -> Ustreza prvemu pogoju, saj je presek vseh produkcij prazen
Ker nimamo ε-produkcij, je to dovolj
```

- SetSpent:
```
FIRST(SetSpent) = { set_spent }
            -> Samo ena produkcija, presek je prazen, torej ustreza prvemu pogoju
Ker nimamo ε-produkcij, je to dovolj
```

- GetSpent:
```
FIRST(GetSpent) = { get_spent }
            -> Samo ena produkcija, presek je prazen, torej ustreza prvemu pogoju
Ker nimamo ε-produkcij, je to dovolj
```

- Assign:
```
FIRST(Assign) = { let }
            -> Samo ena produkcija, presek je prazen, torej ustreza prvemu pogoju
Ker nimamo ε-produkcij, je to dovolj
```

- For:
```
FIRST(For) = { for }
            -> Samo ena produkcija, presek je prazen, torej ustreza prvemu pogoju
Ker nimamo ε-produkcij, je to dovolj
```

- Console:
```
FIRST(Console) = { console }
            -> Samo ena produkcija, presek je prazen, torej ustreza prvemu pogoju
Ker nimamo ε-produkcij, je to dovolj
```

- If:
```
FIRST(If) = { if }
            -> Samo ena produkcija, presek je prazen, torej ustreza prvemu pogoju
Ker nimamo ε-produkcij, je to dovolj
```

- Else:
```
FIRST(Else) = { else, ε }
            -> Ustreza prvemu pogoju, saj je presek vseh produkcij prazen
Ker slika v ε, moramo preveriti še presek:
FIRST(Else) ∩ FOLLOW(Else) = { else, ε } ∩ { +, -, INT, REAL, VARIABLE, (, get_spent, let, for, console, if, fun, city, road, building, location, set_spent, line, bend, box, circle, point, $, }, >, <, ==, !=, ), ,, to } = { }
            -> Ustreza drugemu pogoju
```

- Comparison:
```
FIRST(Comparison) = { +, -, INT, REAL, VARIABLE, (, get_spent }
            -> Samo ena produkcija, presek je prazen, torej ustreza prvemu pogoju
Ker nimamo ε-produkcij, je to dovolj
```

- Comparison':
```
FIRST(Comparison') = { >, <, ==, !=, ε }
            -> Ustreza prvemu pogoju, saj je presek vseh produkcij prazen
Ker slika v ε, moramo preveriti še presek:
FIRST(Comparison') ∩ FOLLOW(Comparison') = { >, <, ==, !=, ε } ∩ { +, -, INT, REAL, VARIABLE, (, get_spent, let, for, console, if, fun, city, road, building, location, set_spent, line, bend, box, circle, point, $, }, >, <, ==, !=, ), ,, to } = { }
            -> Ustreza drugemu pogoju
```

- Fun:
```
FIRST(Fun) = { fun }
            -> Samo ena produkcija, presek je prazen, torej ustreza prvemu pogoju
Ker nimamo ε-produkcij, je to dovolj
```

- Params:
```
FIRST(Params) = { VARIABLE, ε }
            -> Ustreza prvemu pogoju, saj je presek vseh produkcij prazen
Ker slika v ε, moramo preveriti še presek:
FIRST(Params) ∩ FOLLOW(Params) = { VARIABLE, ε } ∩ { ) } = { }
            -> Ustreza drugemu pogoju
```

- Params':
```
FIRST(Params') = { ',', ε }
            -> Ustreza prvemu pogoju, saj je presek vseh produkcij prazen
Ker slika v ε, moramo preveriti še presek:
FIRST(Params') ∩ FOLLOW(Params') = { ',', ε } ∩ { ) } = { }
            -> Ustreza drugemu pogoju
```

- Block:
```
FIRST(Block) = { city, road, building, location }
            -> Ustreza prvemu pogoju, saj je presek vseh produkcij prazen
Ker nimamo ε-produkcij, je to dovolj
```

- Point:
```
FIRST(Point) = { '(' }
            -> Samo ena produkcija, presek je prazen, torej ustreza prvemu pogoju
Ker nimamo ε-produkcij, je to dovolj
```

- Expressions:
```
FIRST(Expressions) = FIRST(Expr) ∪ { ε }
= { +, -, INT, REAL, VARIABLE, (, get_spent, let, for, console, if, fun, city, road, building, location, set_spent, line, bend, box, circle, point } ∪ { ε }
            -> Ustreza prvemu pogoju
Ker slika v ε, moramo preveriti še presek:
FIRST(Expressions) ∩ FOLLOW(Expressions) = { +, -, INT, REAL, VARIABLE, (, get_spent, let, for, console, if, fun, city, road, building, location, set_spent, line, bend, box, circle, point } ∩ { $, '}' } = { }
            -> Ustreza drugemu pogoju
```


