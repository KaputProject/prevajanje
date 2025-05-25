package geojson

import java.math.BigDecimal

sealed class Shape {
    abstract fun toGeoJson(): String
}

class Point(var x: BigDecimal, var y: BigDecimal) : Shape() {
    override fun toString() = "( $x , $y )"

    override fun toGeoJson(): String {
        return """
            [ $x, $y ]
        """.trimIndent()
    }
}

class Line(var p1: Point, var p2: Point) : Shape() {
    override fun toString() = "($p1 , $p2)"

    override fun toGeoJson(): String {
        return """
            ${p1.toGeoJson()},
            ${p2.toGeoJson()}
        """.trimIndent()
    }
}

class Box(var p1: Point, var p2: Point) : Shape() {
    override fun toString() = "($p1 , $p2)"

    override fun toGeoJson(): String {
        val x1 = p1.x.toDouble()
        val y1 = p1.y.toDouble()
        val x2 = p2.x.toDouble()
        val y2 = p2.y.toDouble()
        return """
            
        """.trimIndent()
    }
}

class Bend(var p1: Point, var p2: Point, var factor: BigDecimal) : Shape() {
    override fun toString() = "($p1 , $p2 , $factor)"

    override fun toGeoJson(): String {
        return """
            
        """.trimIndent()
    }
}

class Circle(var p1: Point, var factor: BigDecimal) : Shape() {
    override fun toString() = "($p1 , $factor)"

    override fun toGeoJson(): String {
       return """
           
       """.trimIndent()
    }
}

class Features(
    val blocks: List<Block> = listOf()
) {
    fun toGeoJson(): String {
        var output = "{\n" + "   \"type\": \"FeatureCollection\",\n" + "   \"features\": [\n"

        output += blocks.joinToString(",\n") { block -> block.toGeoJson() }

        output += "\n   ]\n" + "}"

        return output
    }
}

open class Block(
    val type: String,
    val name: String,
    val body: MutableList<Shape> = mutableListOf()
) {
    open fun toGeoJson(): String {
        val geometryType = if (type == "road") {
            "LineString"
        } else if (body.size == 1) {
            "Point"
        } else {
            "Polygon"
        }

        val coordinates = if (geometryType == "Polygon") {
            """
                [
                    ${body.joinToString(",\n") { it.toGeoJson() }}
                ]
            """
        } else if (geometryType == "LineString") {
            """
                ${body.joinToString(",\n") { it.toGeoJson() }}
            """.trimIndent()
        } else {
            body[0].toGeoJson()
        }

        return """
            {
                "type": "Feature",
                "properties": {
                    "name": "$name",
                    "type": "$type"
                },
                "geometry": {
                    "type": "$geometryType",
                    "coordinates": [
                        $coordinates
                    ]
                }
            }
        """.trimIndent()
    }
}

class Location(
    type: String,
    name: String,
    body: MutableList<Shape>,
    val locationType: String? = null,
    var locationValue: BigDecimal? = null
) : Block(type, name, body) {
    override fun toGeoJson(): String {
        val geometryType = if (body.size == 1 && body[0] is Point) "Point" else "Polygon"

        val coordinates = if (geometryType == "Polygon") {
            """
                [
                    ${body.joinToString(",\n") { block -> block.toGeoJson() }}
                ]
            """
        } else {
            body[0].toGeoJson()
        }

        return """
            {
            "type" : "Feature",
                "properties": {
                    "name": "$name",
                    "type": "$locationType",
                    "amount": "$locationValue"
                },
                "geometry": {
                    "type": "$geometryType",
                    "coordinates": [
                        $coordinates
                    ]
                }
            }
        """.trimIndent()
    }
}