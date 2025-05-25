package classes

import TokenType
import java.math.BigDecimal

sealed class ASTNode

sealed class Expr : ASTNode() {
    data class IntLiteral(val value: Int) : Expr()
    data class RealLiteral(val value: Double) : Expr()
    data class Variable(val name: String, val value: BigDecimal?) : Expr()
    data class Unary(val operator: TokenType, val expr: Expr) : Expr()
    data class Binary(val left: Expr, val operator: TokenType, val right: Expr) : Expr()
    data class GetSpend(val key: String) : Expr()
}

sealed class Statement : ASTNode() {
    data class Assign(val name: String, val expr: Expr) : Statement()
    data class SetSpend(val key: String, val value: Expr) : Statement()
    data class Console(val expr: Expr) : Statement()
    data class If(val condition: Expr, val thenBranch: List<Statement>, val elseBranch: List<Statement>?) : Statement()
    data class For(val from: Assign, val to: Expr, val body: List<Statement>) : Statement()
    data class Fun(val name: String, val params: List<String>, val body: List<Statement>) : Statement()
    data class Block(
        val type: String,
        val name: String,
        val body: List<Statement>,
        val locationType: String? = null,
        val locationValue: Double? = null
    ) : Statement()
    data class DrawCommand(val shape: String, val args: List<ASTNode>) : Statement()
}