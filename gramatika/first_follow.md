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

### First in Follow:
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

FIRST(Expr) = FIRST(Bitwise) ∪ FIRST(Assign) ∪ FIRST(For) ∪ FIRST(Console) ∪ FIRST(If) ∪ FIRST(Fun) ∪ FIRST(Block) ∪ FIRST(SetSpent) ∪ FIRST(Draw)
= { +, -, INT, REAL, VARIABLE, (, get_spent, let, for, console, if, fun, city, road, building, location, set_spent, line, bend, box, circle, point }

FIRST(Expressions) = FIRST(Expr) ∪ { ε }
= { +, -, INT, REAL, VARIABLE, (, get_spent, let, for, console, if, fun, city, road, building, location, set_spent, line, bend, box, circle, point, ε}

```