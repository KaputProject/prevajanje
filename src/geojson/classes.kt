package geojson

import java.math.BigDecimal
import kotlin.math.*

sealed class Shape {
    abstract fun toGeoJson(): String
}

class Point(var x: BigDecimal, var y: BigDecimal) : Shape() {
    override fun toString() = "( $x , $y )"

    override fun toGeoJson(): String {
        return """
            [
                $x, 
                $y 
            ]
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

        // p1 in p2 moreta biti nasprotna kota
        return """
            [$x1, $y1],
            [$x1, $y2],
            [$x2, $y2],
            [$x2, $y1],
            [$x1, $y1]
        """.trimIndent()
    }
}

class Bend(var p1: Point, var p2: Point, var factor: BigDecimal) : Shape() {
    override fun toString() = "($p1 , $p2 , $factor)"

    override fun toGeoJson(): String {
        val midpoints = 10
        val x1 = p1.x.toDouble()
        val y1 = p1.y.toDouble()
        val x2 = p2.x.toDouble()
        val y2 = p2.y.toDouble()
        val bulge = factor.toDouble()

        // Calculated center
        val mx = (x1 + x2) / 2
        val my = (y1 + y2) / 2

        val dx = x2 - x1
        val dy = y2 - y1
        val length = sqrt(dx * dx + dy * dy)

        val nx = -dy / length
        val ny = dx / length

        val h = (bulge * length) / 4

        val cx = mx + nx * h
        val cy = my + ny * h

        val radius = sqrt((x1 - cx).pow(2) + (y1 - cy).pow(2))
        val angle1 = atan2(y1 - cy, x1 - cx)
        val angle2 = atan2(y2 - cy, x2 - cx)

        val sweep = if (bulge >= 0) {
            if (angle2 < angle1) angle2 + 2 * PI - angle1 else angle2 - angle1
        } else {
            if (angle2 > angle1) angle2 - 2 * PI - angle1 else angle2 - angle1
        }

        val coordinates = mutableListOf<String>()
        for (i in 0..midpoints + 1) {
            val t = i.toDouble() / (midpoints + 1)
            val angle = angle1 + sweep * t
            val px = cx + radius * cos(angle)
            val py = cy + radius * sin(angle)
            coordinates.add("[$px, $py]")
        }

        return coordinates.joinToString(",\n")
    }
}

class Circle(var p1: Point, var factor: BigDecimal) : Shape() {
    override fun toString() = "($p1 , $factor)"

    override fun toGeoJson(): String {
        val centerLon = p1.x.toDouble()
        val centerLat = p1.y.toDouble()
        val radius = factor.toDouble()
        val earthRadius = 6378137.0
        val numberOfEdges = 64

        fun toRadians(deg: Double): Double = Math.toRadians(deg)
        fun toDegrees(rad: Double): Double = Math.toDegrees(rad)

        fun offset(center: Pair<Double, Double>, distance: Double, bearing: Double): Pair<Double, Double> {
            val lat1 = toRadians(center.second)
            val lon1 = toRadians(center.first)
            val dByR = distance / earthRadius

            val lat = asin(
                sin(lat1) * cos(dByR) + cos(lat1) * sin(dByR) * cos(bearing)
            )

            val lon = lon1 + atan2(
                sin(bearing) * sin(dByR) * cos(lat1),
                cos(dByR) - sin(lat1) * sin(lat)
            )

            return Pair(toDegrees(lon), toDegrees(lat))
        }

        val coordinates = mutableListOf<String>()
        val start = 0.0
        val center = Pair(centerLon, centerLat)

        for (i in 0 until numberOfEdges) {
            val angle = start + (2 * Math.PI * i / numberOfEdges)
            val (lon, lat) = offset(center, radius, angle)
            coordinates.add("[$lon, $lat]")
        }

        coordinates.add(coordinates[0])

        return coordinates.joinToString(",\n")
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
        val geometryType = if (type == "ROAD") {
            "LineString"
        } else if (body.size == 1 && body[0] is Point) {
            "Point"
        } else {
            "Polygon"
        }

        val coordinates = if (geometryType == "Polygon") {
            """
            [
                [
                    ${body.joinToString(",\n") { it.toGeoJson() }}
                ]
            ]
            """
        } else if (geometryType == "LineString") {
            """
            [
                ${body.joinToString(",\n") { it.toGeoJson() }}
            ]
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
                    "coordinates": $coordinates
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
    private fun getColor(type: String): String {
        return when (type.lowercase()) {
            "restaurant" -> "#bb00ff"
            "park" -> "#00bb00"
            "shop" -> "#ffa500"
            "gas_station" -> "#ff0000"
            "hospital" -> "#ff69b4"
            "cafe" -> "#8b4513"
            "bar" -> "#1e90ff"
            "nightclub" -> "#4b0082"
            "club" -> "#800080"
            else -> "#808080"
        }
    }

    override fun toGeoJson(): String {
        val geometryType = if (body.size == 1 && body[0] is Point) "Point" else "Polygon"
        val color = getColor(locationType ?: "")

        val coordinates = if (geometryType == "Polygon") {
            """
            [
                [
                    ${body.joinToString(",\n") { it.toGeoJson() }}
                ]
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
                    "amount": "$locationValue",
                    "fill": "$color",
                    "fill-opacity": 0.8,
                    "marker-color": "$color"
                },
                "geometry": {
                    "type": "$geometryType",
                    "coordinates": $coordinates
                }
            }
        """.trimIndent()
    }
}