### Terminali
```
'line' 'bend' 'box' 'circle' 'point' 'set_spent' STRING INT REAL VARIABLE 
'+' '-' '*' '/' 'bwand' 'bwor' 
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
First(Additive') = { +, -, ε}

First(Bitwise) = { +, -, INT, REAL, VARIABLE, (, get_spent }
First(Bitwise') = { bwand, bwor, ε}

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

First(Expr) = First(Bitwise) ∪ First(Assign) ∪ First(For) ∪ First(Console) ∪ First(If) ∪ First(Fun) ∪ First(Block) ∪ First(SetSpent) ∪ First(Draw)
= { +, -, INT, REAL, VARIABLE, (, get_spent, let, for, console, if, fun, city, road, building, location, set_spent, line, bend, box, circle, point }

First(Expressions) = First(Expr) ∪ { ε }
= { +, -, INT, REAL, VARIABLE, (, get_spent, let, for, console, if, fun, city, road, building, location, set_spent, line, bend, box, circle, point, ε}
```

### Follow:
```
FOLLOW(Expressions) = { $, '}' }

FOLLOW(Expr) = FIRST(Expr) ∪ FOLLOW(Expressions)
FOLLOW(Expr) = { +, -, INT, REAL, VARIABLE, (, get_spent, let, for, console, if, fun, city, road, building, location, set_spent, line, bend, box, circle, point, $, '}' }

FOLLOW(Bitwise) = FOLLOW(Expr) ∪ { ')' } ∪ FOLLOW(SetSpent) ∪ FOLLOW(Console) ∪ FIRST(Comparison') ∪ FOLLOW(Comparison') ∪ { , } ∪ FOLLOW(Assign)
FOLLOW(Bitwise) = { +, -, INT, REAL, VARIABLE, (, get_spent, let, for, console, if, fun, city, road, building, location, set_spent, line, bend, box, circle, point, $, `}`, >, <, ==, !=, ), `,` , to }

FOLLOW(Bitwise') = FOLLOW(Bitwise)
                 = { +, -, INT, REAL, VARIABLE, (, get_spent, let, for, console, if, fun, city, road, building, location, set_spent, line, bend, box, circle, point, $, `}`, >, <, ==, !=, ), `,` , to }

FOLLOW(Additive) = FIRST(Bitwise') ∪ FOLLOW(Bitwise)
FOLLOW(Additive) = { bwand, bwor, +, -, INT, REAL, VARIABLE, (, get_spent, let, for, console, if, fun, city, road, building, location, set_spent, line, bend, box, circle, point, $, `}`, >, <, ==, !=, ), `,` , to }

FOLLOW(Additive') = FOLLOW(Additive)
                  = { bwand, bwor, +, -, INT, REAL, VARIABLE, (, get_spent, let, for, console, if, fun, city, road, building, location, set_spent, line, bend, box, circle, point, $, `}`, >, <, ==, !=, ), `,` , to }

FOLLOW(Multiplicative) = FIRST(Additive') ∪ FOLLOW(Additive)
                       = { +, - } ∪ FOLLOW(Additive)
                       = { bwand, bwor, +, -, INT, REAL, VARIABLE, (, get_spent, let, for, console, if, fun, city, road, building, location, set_spent, line, bend, box, circle, point, $, `}`, >, <, ==, !=, ), `,` , to }

FOLLOW(Multiplicative') = FOLLOW(Multiplicative)
                        = { bwand, bwor, +, -, INT, REAL, VARIABLE, (, get_spent, let, for, console, if, fun, city, road, building, location, set_spent, line, bend, box, circle, point, $, `}`, >, <, ==, !=, ), `,` , to }

FOLLOW(Unary) = FIRST(Multiplicative') ∪ FOLLOW(Multiplicative)
              = { *, / } ∪ FOLLOW(Multiplicative)
              = { /, *, bwand, bwor, +, -, INT, REAL, VARIABLE, (, get_spent, let, for, console, if, fun, city, road, building, location, set_spent, line, bend, box, circle, point, $, `}`, >, <, ==, !=, ), `,` , to }

FOLLOW(Primary) = FOLLOW(Unary)
                = { /, *, bwand, bwor, +, -, INT, REAL, VARIABLE, (, get_spent, let, for, console, if, fun, city, road, building, location, set_spent, line, bend, box, circle, point, $, `}`, >, <, ==, !=, ), `,` , to }


FOLLOW(Draw) = FOLLOW(Expr)
             = { +, -, INT, REAL, VARIABLE, (, get_spent, let, for, console, if, fun, city, road, building, location, set_spent, line, bend, box, circle, point, $, '}' }

FOLLOW(SetSpent) = FOLLOW(Expr)
                 = { +, -, INT, REAL, VARIABLE, (, get_spent, let, for, console, if, fun, city, road, building, location, set_spent, line, bend, box, circle, point, $, '}' }

FOLLOW(GetSpent) = FOLLOW(Primary)
                 = { /, *, bwand, bwor, +, -, INT, REAL, VARIABLE, (, get_spent, let, for, console, if, fun, city, road, building, location, set_spent, line, bend, box, circle, point, $, `}`, >, <, ==, !=, ), `,` , to }


FOLLOW(Assign) = FOLLOW(Expr) ∪ { to }
               = { to, +, -, INT, REAL, VARIABLE, (, get_spent, let, for, console, if, fun, city, road, building, location, set_spent, line, bend, box, circle, point, $, '}' }

FOLLOW(For) = FOLLOW(Expr)
            = { +, -, INT, REAL, VARIABLE, (, get_spent, let, for, console, if, fun, city, road, building, location, set_spent, line, bend, box, circle, point, $, '}' }

FOLLOW(Console) = FOLLOW(Expr)
                = { +, -, INT, REAL, VARIABLE, (, get_spent, let, for, console, if, fun, city, road, building, location, set_spent, line, bend, box, circle, point, $, '}' }

FOLLOW(If) = FOLLOW(Expr)
           = { +, -, INT, REAL, VARIABLE, (, get_spent, let, for, console, if, fun, city, road, building, location, set_spent, line, bend, box, circle, point, $, '}' }
FOLLOW(Else) = FOLLOW(If) = FOLLOW(Expr)
             = { +, -, INT, REAL, VARIABLE, (, get_spent, let, for, console, if, fun, city, road, building, location, set_spent, line, bend, box, circle, point, $, '}' }

FOLLOW(Comparison) = { ) }
FOLLOW(Comparison') = FOLLOW(Comparison)

FOLLOW(Fun) = FOLLOW(Expr)
            = { +, -, INT, REAL, VARIABLE, (, get_spent, let, for, console, if, fun, city, road, building, location, set_spent, line, bend, box, circle, point, $, '}' }
            
FOLLOW(Params) = { ) }
FOLLOW(Params') = FOLLOW(Params) = { ) }

FOLLOW(Block) = FOLLOW(Expr)
              = { +, -, INT, REAL, VARIABLE, (, get_spent, let, for, console, if, fun, city, road, building, location, set_spent, line, bend, box, circle, point, $, '}' }

FOLLOW(Point) = { ',' , ')' }
```

### LL1:
- Expr:
```
FIRST(Expr) = FIRST(Bitwise) ∪ FIRST(Assign) ∪ FIRST(For) ∪ FIRST(Console) ∪ FIRST(If) ∪ FIRST(Fun) ∪ FIRST(Block) ∪ FIRST(SetSpent) ∪ FIRST(Draw) = 
            = { +, -, INT, REAL, VARIABLE, (, get_spent } ∪ { let } ∪ { for } ∪ { console } ∪ { if } ∪ { fun } ∪ { city, road, building, location } ∪ { set_spent } ∪ { line, bend, box, circle, point }
            -> Vse so disjunktne, ustreza prvemu pogoju
Ker nimamo nobene produkcije ki slika v ε, je to dovolj
```

- Bitwise:
```
FIRST(Bitwise) = FIRST(Additive) = { +, -, INT, REAL, VARIABLE, (, get_spent }
               -> Vse so disjunktne, ustreza prvemu pogoju
Ker nimamo nobene produkcije ki slika v ε, je to dovolj
```

- Bitwise':
```
FIRST(Bitwise') = { bwand, bwor, ε } ∪ FOLLOW(Bitwise') = { bwand, bwor, ε } ∪ { +, -, INT, REAL, VARIABLE, (, get_spent }
               -> Vse so disjunktne, ustreza prvemu pogoju