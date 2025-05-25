import geojson.*

fun main() {
    val code1 = """
    location "Mango" restaurant 0.0 {
        for (let x = 1 to 10 ) {
            console x
        }
        
        let f = 10
        if (f > 9) {
            box ((0, 0), (1, 1))
        } else {   
            bend ((0, 0), (1, 1), 0.01)
        }

        line ((0, 0), (1, 1))
        circle ((0, 0), 0.5)
        point ((1, 1))
    }
    for (let x = 1 to 10 ) {
            console x
        }
        
        let f = 10
        if (f > 9) {
            box ((0, 0), (1, 1))
        } else {   
            bend ((0, 0), (1, 1), 0.01)
        }
    """.trimIndent()

    val code2 = """
        line ((0, 0), (1, 1))
        circle ((0, 0), 0.5)
        point ((1, 1))
    """.trimIndent()

    val code3 = """
        let f = 10
        if (f > 9) {
            for (let hmm = 1 to 4) {
                console hmm
                if (hmm > 2) {
                    console hmm
                }
            }
        } else {
            console 2
        }
    """.trimIndent()

    val code4 = """
        fun "hmmmm"(a, b) {
            console a
            console (b + a)
        }
    """.trimIndent()

    val code5 = """
        setSpent "hmmm" 30
    """.trimIndent()

    val code6 = """
        let a = 6
        
        console (a + a)
        console 4
    """.trimIndent()

    val code7 = """
    location "Fast Food pri Stuku" restaurant 0.0 {
        set_spent "Fast Food pri Stuku" 16.50
        
        point (( 15.62775921695112 , 46.5635219201632 ))
        point (( 15.627698988270168 , 46.563436029888095 ))
        point (( 15.627939920165602 , 46.56335575998327 ))
        point (( 15.627998791971066 , 46.56344068333098 ))
        point (( 15.62775921695112 , 46.5635219201632 ))
    }
    
    building "Nakupovalni Center" {
        
    }
    
    
    """.trimIndent()

    val code8 = """
    city "Maribor" {
        point ((1,2))
        road "Gosposvetska" {
            box ((1,2),(3,4))
            box ((1,2),(3,4))
        }
    }
    """.trimIndent()

    val lexer = Lexer(code7)
    val tokens = lexer.tokenize()
    var index = 0
    for (token in tokens) {
        println(index.toString() + " " + token)
        index++
    }

    val evaluator = Evaluator2(tokens)
    val featList = evaluator.program().values.toList()

    val geoJson = Features(featList).toGeoJson()
    println(geoJson)

//    println("Parsed blocks:")
//    for ((key, block) in blocks) {
//        println("Name: ${block.name}, Type: ${block.type}")
//        for (value in block.body) {
//            println(value.toString())
//        }
//        if (block is Location) {
//            println("  Location type: ${block.locationType}, Value: ${block.locationValue}")
//        }
//        println(block.toGeoJson())
//    }
}
